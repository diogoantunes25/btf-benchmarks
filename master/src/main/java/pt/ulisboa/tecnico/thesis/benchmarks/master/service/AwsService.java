package pt.ulisboa.tecnico.thesis.benchmarks.master.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;

public class AwsService {

    public static final String KEY_NAME = "alea_key";
    public static final String SECURITY_GROUP_NAME = "alea_security_group";
    public static final String SECURITY_GROUP_DESC = "No description.";

    private final Map<Region, String> AMI_ID_MAP = Map.of(
            // Asia Pacific
            Region.AP_NORTHEAST_1, "ami-09ebacdc178ae23b7", // Tokyo
            Region.AP_SOUTHEAST_1, "ami-082105f875acab993", // Singapore
            Region.AP_SOUTH_1, "ami-0a23ccb2cdd9286bb",     // Mumbai

            // Europe
            Region.EU_WEST_3, "ami-0d49cec198762b78c",      // Paris
            // Region.EU_NORTH_1, "ami-0f0b4cb72cf3eadf3",      // Stockholm
            Region.EU_WEST_2, "ami-0dbec48abfe298cab",      // London
            Region.EU_CENTRAL_1, "ami-07df274a488ca9195",   // Frankfurt

            // North America
            Region.US_WEST_1, "ami-04b6c97b14c54de18",      // N. California
            Region.US_EAST_1, "ami-087c17d1fe0178315",      // Virginia
            Region.CA_CENTRAL_1, "ami-0e2407e55b9816758",   // Central Canada

            // South America
            Region.SA_EAST_1, "ami-0f8243a5175208e08"      // Sao Paulo
    );

    private final AwsBasicCredentials credentials;

    public AwsService(String accessKeyId, String accessKeySecret) {
        this.credentials = AwsBasicCredentials.create(accessKeyId, accessKeySecret);
    }

    public Ec2Client getClient(Region region) {
        return Ec2Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public Collection<Region> getRegions() {
        return AMI_ID_MAP.keySet();
    }

    public String getAmiId(Region region) {
        return AMI_ID_MAP.get(region);
    }

    public List<Instance> getInstances(Ec2Client client) {
        List<Instance> instances = new ArrayList<>();

        String nextToken = null;
        try {
            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(6).nextToken(nextToken).build();
                DescribeInstancesResponse response = client.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    instances.addAll(reservation.instances());
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }

        return instances;
    }

    public Instance createInstance(Ec2Client client, String name, String amiId, String securityGroup, String script) {
        // assign a public IPv4 address to an instance
        InstanceNetworkInterfaceSpecification networkInterface = InstanceNetworkInterfaceSpecification.builder()
                .associatePublicIpAddress(true)
                .deviceIndex(0)
                .build();

        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.T2_MEDIUM)
                .keyName(KEY_NAME)
                .securityGroups(securityGroup)
                .maxCount(1)
                .minCount(1)
                //.networkInterfaces(networkInterface)
                .userData(Base64.getEncoder().encodeToString(script.getBytes()))
                .build();

        RunInstancesResponse response = client.runInstances(runRequest);
        Instance instance = response.instances().get(0);
        String instanceId = instance.instanceId();

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
            return instance;

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }

        return instance;
    }

    public void startInstance(Ec2Client client, Instance instance) {
        StartInstancesRequest request = StartInstancesRequest.builder()
                .instanceIds(instance.instanceId())
                .build();

        client.startInstances(request);

        System.out.printf("Successfully started instance %s\n", instance.instanceId());
    }

    public void stopInstance(Ec2Client client, Instance instance) {
        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instance.instanceId())
                .build();

        client.stopInstances(request);

        System.out.printf("Successfully stopped instance %s", instance.instanceId());
    }

    public void terminateInstance(Ec2Client client, Instance instance) {
        TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                .instanceIds(instance.instanceId())
                .build();

       client.terminateInstances(request);

        // System.out.printf("Successfully terminated instance %s", instance.instanceId());
    }

    public List<Vpc> getVpcs(Ec2Client client) {
        try {
            DescribeVpcsResponse response = client.describeVpcs();
            return response.vpcs();
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return new ArrayList<>();
    }

    public List<SecurityGroup> getSecurityGroups(Ec2Client client) {
        try {
            DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().build();
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(request);
            return response.securityGroups();

        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return new ArrayList<>();
    }

    public Optional<SecurityGroup> getSecurityGroup(Ec2Client client, String groupName) {
        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                .groupNames(groupName).build();
        try {
            DescribeSecurityGroupsResponse response = client.describeSecurityGroups(request);
            if (response.hasSecurityGroups()) return Optional.of(response.securityGroups().get(0));
        } catch (Ec2Exception e) {
            System.err.println(e.awsErrorDetails());
        }
        return Optional.empty();
    }

    public String createSecurityGroup(Ec2Client client, String name, String description, String vpcId) {
        CreateSecurityGroupRequest request = CreateSecurityGroupRequest.builder()
                .groupName(name)
                .description(description)
                .vpcId(vpcId)
                .build();

        CreateSecurityGroupResponse response = client.createSecurityGroup(request);
        return response.groupId();
    }

    public void addSecurityRules(Ec2Client client, String groupId, IpPermission... rules) {
        AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                .groupId(groupId)
                .ipPermissions(rules)
                .build();

        AuthorizeSecurityGroupIngressResponse authResponse = client.authorizeSecurityGroupIngress(authRequest);
        System.out.printf("Successfully added ingress policy to Security Group %s", authRequest.groupId());
    }

    public List<KeyPairInfo> getKeyPairs(Ec2Client client) {
        DescribeKeyPairsResponse response = client.describeKeyPairs();
        return response.keyPairs();
    }

    public void importKeyPair(Ec2Client client, String keyName, byte[] publicKey) {
        ImportKeyPairRequest request = ImportKeyPairRequest.builder()
                .keyName(keyName)
                .publicKeyMaterial(SdkBytes.fromByteArray(publicKey))
                .build();
        client.importKeyPair(request);
    }
}
