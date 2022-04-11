package pt.ulisboa.tecnico.thesis.benchmarks.master;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class AwsDeployer {

    public static final String KEY_NAME = "alea_key";
    public static final String SECURITY_GROUP_NAME = "alea_security_group";
    public static final String SECURITY_GROUP_DESC = "No description.";

    private static final Integer PORT = 8080;
    private static final Integer CPORT = 8080;

    public static void main(String[] args) {
        // Ec2Client.serviceMetadata().regions().forEach(System.out::println);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                "AKIASLIZXB7USNISCZFC",
                "H+JhJmilE1BzeUI8JDNrEM2jpiHUXA7Dr9IwgxEc"
        );

        List<Region> regions = List.of(
                //Region.AP_NORTHEAST_1,  // Tokyo*
                // Region.AP_SOUTHEAST_1,  // Singapore
                // Region.AP_SOUTH_1,      // Mumbai
                // Region.EU_NORTH_1,      // Stockholm
                Region.EU_WEST_3       // Paris
                //Region.EU_CENTRAL_1,    // Frankfurt
                //Region.SA_EAST_1,       // Sao Paulo*
                //Region.US_WEST_1       // California*
                //Region.US_EAST_1,       // Virginia
                //Region.CA_CENTRAL_1     // Central Canada (why?)
        );

        Map<Region, String> amis = new HashMap<>();
        amis.put(Region.AP_NORTHEAST_1, "ami-09ebacdc178ae23b7");
        amis.put(Region.EU_WEST_3, "ami-0d49cec198762b78c");
        amis.put(Region.SA_EAST_1, "ami-0f8243a5175208e08");
        amis.put(Region.US_WEST_1, "ami-04b6c97b14c54de18");

        // init clients
        Map<Region, Ec2Client> clients = new HashMap<>();
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        for (Region region: regions) {
            Ec2Client client = Ec2Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();
            clients.put(region, client);
        }

        // read public key
        Ec2Client parisClient = clients.get(Region.EU_WEST_3);
        byte[] publicKey = null;
        try {
            publicKey = Files.readAllBytes(new File("alea_key.pub").toPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // main loop
        for (Region region: regions) {
            System.out.println(region.metadata().description() + ":");
            Ec2Client client = clients.get(region);

            // import key
            List<KeyPairInfo> keyPairs = getEC2KeyPairs(client);
            if (keyPairs.stream().noneMatch(k -> k.keyName().equals(KEY_NAME))) {
                importEC2KeyPair(client, KEY_NAME, publicKey);
                // createEC2KeyPair(client, KEY_NAME); // create key pair
            }
            for (KeyPairInfo keyPair: keyPairs) {
                System.out.println(keyPair);
            }

            // spawn instance
            List<Instance> instances = getEc2Instances(client);
            if (instances.stream().allMatch(i -> i.state().name().equals(InstanceStateName.TERMINATED))) {
                createEc2Instance(client, "alea-replica", amis.get(region));
            }

            break; // TODO remove
        }
        System.out.println("Done.");
        System.exit(0);


        // list all security groups
        /*for (Region region: regions) {
            System.out.println(region.metadata().description() + ":");

            Ec2Client client = clients.get(region);

            List<Vpc> vpcs = getEC2Vpcs(client);
            for (Vpc vpc : vpcs) {
                System.out.printf("Found VPC with id %s vpc state %s and tennancy %s\n",
                        vpc.vpcId(),
                        vpc.stateAsString(),
                        vpc.instanceTenancyAsString()
                );
            }
            Optional<Vpc> defaultVpc = vpcs.stream().filter(Vpc::isDefault).findAny();
            if (defaultVpc.isEmpty()) System.exit(0);

            List<SecurityGroup> securityGroups = getEC2SecurityGroups(client);
            if (securityGroups.stream().noneMatch(g -> g.groupName().equals(SECURITY_GROUP_NAME))) {
                createEC2SecurityGroup(client, SECURITY_GROUP_NAME, SECURITY_GROUP_DESC, defaultVpc.get().vpcId());
            }
            for (SecurityGroup group : securityGroups) {
                System.out.printf("Found Security Group with id %s, vpc id %s and description %s\n",
                        group.groupId(),
                        group.vpcId(),
                        group.description()
                );
            }
        }*/
    }

    public static List<Instance> getEc2Instances(Ec2Client client) {
        List<Instance> instances = new ArrayList<>();

        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = client.describeInstances(request);

                for (Reservation reservation : response.reservations()) {
                    instances.addAll(reservation.instances());
                    for (Instance instance : reservation.instances()) {
                        System.out.println("Instance Id is " + instance.instanceId());
                        System.out.println("Image id is "+  instance.imageId());
                        System.out.println("Instance type is "+  instance.instanceType());
                        System.out.println("Instance state name is "+  instance.state().name());
                        System.out.println("monitoring information is "+  instance.monitoring().state());
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return instances;
    }

    public static String createEc2Instance(Ec2Client client, String name, String amiId) {
        // assign a public IPv4 address to an instance
        InstanceNetworkInterfaceSpecification networkInterface = InstanceNetworkInterfaceSpecification.builder()
                .associatePublicIpAddress(true)
                .deviceIndex(0)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.T2_MICRO)
                .keyName(KEY_NAME)
                //.securityGroupIds(securityGroups)
                .maxCount(1)
                .minCount(1)
                .networkInterfaces(networkInterface)
                .build();

        RunInstancesResponse response = client.runInstances(runRequest);
        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key("Name")
                .value(name)
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            client.createTags(tagRequest);
            System.out.printf("Successfully started EC2 Instance %s based on AMI %s\n", instanceId, amiId);
            return instanceId;

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }

        return instanceId;
    }

    public static void createEC2KeyPair(Ec2Client client, String keyName) {
        try {
            CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                    .keyName(keyName)
                    .build();
            CreateKeyPairResponse response = client.createKeyPair(request);
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        System.out.printf("Successfully created key pair named %s\n", keyName);
    }

    public static void importEC2KeyPair(Ec2Client client, String keyName, byte[] publicKey) {
        try {
            ImportKeyPairRequest request = ImportKeyPairRequest.builder()
                    .keyName(keyName)
                    .publicKeyMaterial(SdkBytes.fromByteArray(publicKey))
                    .build();
            ImportKeyPairResponse response = client.importKeyPair(request);
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    public static void deleteEC2KeyPair(Ec2Client client, String keyName) {
        try {
            DeleteKeyPairRequest request = DeleteKeyPairRequest.builder()
                    .keyName(keyName).build();
            DeleteKeyPairResponse response = client.deleteKeyPair(request);
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        System.out.printf("Successfully deleted key pair named %s\n", keyName);
    }

    public static List<KeyPairInfo> getEC2KeyPairs(Ec2Client client) {
        List<KeyPairInfo> keyPairs = new ArrayList<>();
        try {
            DescribeKeyPairsResponse response = client.describeKeyPairs();
            keyPairs.addAll(response.keyPairs());
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return keyPairs;
    }

    public static String createEC2SecurityGroup(Ec2Client client, String groupName, String groupDesc, String vpcId) {
        try {
            CreateSecurityGroupRequest createRequest = CreateSecurityGroupRequest.builder()
                    .groupName(groupName)
                    .description(groupDesc)
                    .vpcId(vpcId)
                    .build();

            CreateSecurityGroupResponse createResponse = client.createSecurityGroup(createRequest);

            // ip range (no filter)
            IpRange ipRange = IpRange.builder().cidrIp("0.0.0.0/0").build();

            // protocol port
            IpPermission protocolPerm = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(PORT)
                    .fromPort(PORT)
                    .ipRanges(ipRange)
                    .build();

            // control port
            IpPermission controlPerm = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(CPORT)
                    .fromPort(CPORT)
                    .ipRanges(ipRange)
                    .build();

            AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupName(SECURITY_GROUP_NAME)
                    .ipPermissions(protocolPerm, controlPerm)
                    .build();

            AuthorizeSecurityGroupIngressResponse authResponse = client.authorizeSecurityGroupIngress(authRequest);
            System.out.printf("Successfully added ingress policy to Security Group %s", groupName);
            return createResponse.groupId();

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }

        return "";
    }

    public static List<SecurityGroup> getEC2SecurityGroups(Ec2Client client) {
        List<SecurityGroup> securityGroups = new ArrayList<>();
        try {
            DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().build();
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(request);
            securityGroups.addAll(response.securityGroups());

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return securityGroups;
    }

    public static List<Vpc> getEC2Vpcs(Ec2Client client) {
        List<Vpc> vpcs = new ArrayList<>();
        try {
            DescribeVpcsResponse response = client.describeVpcs();
            vpcs.addAll(response.vpcs());
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return vpcs;
    }
}
