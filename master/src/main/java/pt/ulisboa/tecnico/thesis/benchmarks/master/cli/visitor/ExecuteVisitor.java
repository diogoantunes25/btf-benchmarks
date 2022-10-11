package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hbbft.utils.ThreshsigUtils;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.Dealer;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.ProcessCreationServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.ProcessCreationServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.Config;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.CommandParser;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.AwsCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.AwsTerminateCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.Command;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.ExitCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.ListCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.PingCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.RunBenchmarkCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.RunScriptCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.SetProtocolCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.SetTopologyCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.ShutdownCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.SleepCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.SpawnPcsCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.SpawnServerCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StartCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StopCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.util.NopCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Pcs;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.master.exception.InvalidCommandException;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.PcsRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ReplicaRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.AwsService;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.local.BenchmarkService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.Vpc;

public class ExecuteVisitor implements CommandVisitor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Config config;

    private final PcsRepository pcsRepository;
    private final ReplicaRepository replicaRepository;
    private final BenchmarkService benchmarkService;
    private final AwsService awsService;

    private final int PCS_DEFAULT_PORT = 8500;
    private final int REPLICA_BASE_PORT = 9000;
    private final int REPLICA_BASE_CONTROL_PORT = 10000;

    public ExecuteVisitor(
            Config config,
            PcsRepository pcsRepository,
            ReplicaRepository replicaRepository,
            BenchmarkService benchmarkService
    ) {
        this.config = config;

        this.pcsRepository = pcsRepository;
        this.replicaRepository = replicaRepository;

        this.benchmarkService = benchmarkService;
        this.awsService = new AwsService(
                "AKIASLIZXB7USNISCZFC",
                "H+JhJmilE1BzeUI8JDNrEM2jpiHUXA7Dr9IwgxEc"
        );
    }

    @Override
    public boolean visit(SpawnPcsCommand cmd) {
        /**
         *  We adopt the following convention:
         *       - Lauching of PCS main class has to be done manually on the remote
         *  machine (thus this command only registers the PCS on the master process)
         *       - The PCS within a machine will use port 8500.
         *       - Each replica with id i will use port 9000 + i for regular port and 10000 + i for security port.
         */

        InetAddress nodeAddress = null;

        try {
            nodeAddress = InetAddress.getByName(cmd.getNode());
        }
        catch (UnknownHostException e) {
            System.out.println("PCS Spawning failed. Unknwon host.");
        }

        Pcs pcs = new Pcs(cmd.getId(), nodeAddress.getHostAddress(), PCS_DEFAULT_PORT);
        System.out.println("PCS IP: " + pcs.getAddress() + ", port: " + pcs.getPort());
        this.pcsRepository.add(pcs);

        return true;
    }

    @Override
    public boolean visit(SpawnServerCommand cmd) {
        Pcs pcs = pcsRepository.get(cmd.getPcs());
        if (pcs == null) return false;
        // if (pcs == null || !pcs.getStatus().equals(Pcs.Status.ONLINE)) return false;

        // Create pcs channel and stub
        ManagedChannel channel = pcs.getChannel();
        ProcessCreationServiceGrpc.ProcessCreationServiceBlockingStub stub = ProcessCreationServiceGrpc
                .newBlockingStub(channel);

        ProcessCreationServiceOuterClass.CreateReplicaRequest request = ProcessCreationServiceOuterClass.CreateReplicaRequest.newBuilder()
                .setReplicaId(replicaRepository.getAll().size())
                .setIpPcs(pcs.getAddress())
                .build();

        System.out.println("Master says: create replica " + replicaRepository.getAll().size());

        ProcessCreationServiceOuterClass.CreateReplicaResponse response = null;
        try {
            response = stub.replica(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }

        if (response == null || !response.getOk()) {
            System.out.println("Unable to spawn replica.");
        } else {
            System.out.println("New replica was requested");
        }

        // TODO: check this
        // channel.shutdown();

        return true;
    }

    @Override
    public boolean visit(ExitCommand cmd) {
        System.exit(0);
        return true;
    }

    @Override
    public boolean visit(ListCommand cmd) {
        System.out.println("PCSs:");
        for (Pcs pcs: pcsRepository.getAll()) {
            System.out.println(pcs);
        }

        System.out.println("Replicas:");
        for (Replica replica: replicaRepository.getAll()) {
            System.out.println(replica);
        }

        return true;
    }

    @Override
    public boolean visit(PingCommand cmd) {
        // TODO
        return true;
    }

    @Override
    public boolean visit(SetTopologyCommand cmd) {
        // select replicas in topology
        final int n = cmd.getReplicaIds().size();
        final int f = Math.floorDiv(n-1, 3);
        List<Replica> replicas = replicaRepository.getAll().stream()
                .filter(r -> cmd.getReplicaIds().contains(r.getReplicaId())).collect(Collectors.toList());
        assert n == replicas.size();

        // generate threshold keys and shares
        Dealer dealer = ThreshsigUtils.sigSetup(f+1, n, 256);
        GroupKey groupKey = dealer.getGroupKey();
        KeyShare[] keyShares = dealer.getShares();

        // set topology
        Benchmark.Topology topology = new Benchmark.Topology(replicas, groupKey, Arrays.asList(keyShares), f);
        benchmarkService.setTopology(topology);

        return true;
    }

    @Override
    public boolean visit(SetProtocolCommand cmd) {
        // set protocol
        Benchmark.Protocol protocol = new Benchmark.Protocol(
                cmd.getProtocol(), cmd.getBatchSize(), cmd.getBenchmarkMode(), cmd.getFaultMode(), cmd.getLoad());
        benchmarkService.setProtocol(protocol);

        return true;
    }

    @Override
    public boolean visit(StartCommand cmd) {
        benchmarkService.startBenchmark();
        return true;
    }

    @Override
    public boolean visit(StopCommand cmd) {
        benchmarkService.stopBenchmark();
        return true;
    }

    @Override
    public boolean visit(RunBenchmarkCommand cmd) {
        // TODO deprecated benchmarkService.runBenchmark(cmd.getNumRequests(), cmd.getPayloadSize());
        return true;
    }

    @Override
    public boolean visit(ShutdownCommand cmd) {
        // close all connections
        benchmarkService.shutdown(cmd.getTimer());

        // shutdown master
        System.exit(0);
        return true;
    }

    @Override
    public boolean visit(NopCommand cmd) {
        return true;
    }

    @Override
    public boolean visit(SleepCommand cmd) {
        try {
            Thread.sleep(cmd.getDuration());
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(RunScriptCommand cmd) {
        try (BufferedReader reader = new BufferedReader(new FileReader(cmd.getScript()))) {
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                Command command = CommandParser.parse(line);
                command.accept(this);
                line = reader.readLine();
            }
        } catch (InvalidCommandException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(AwsCommand cmd) {
        // get client for region
        Region region = Region.of(cmd.getRegion());
        Ec2Client client = awsService.getClient(region);
        logger.info("Initialized client for region {}.", region.id());

        // check for available instances in the region
        List<Instance> instances = awsService.getInstances(client);
        List<Instance> availableInstances = instances.stream()
                .filter(i -> i.state().name().equals(InstanceStateName.STOPPED)).collect(Collectors.toList());
        logger.info("Found {} available Ec2 instances.", availableInstances.size());

        Instance instance;
        if (availableInstances.isEmpty()) {
            Optional<SecurityGroup> securityGroup = awsService.getSecurityGroup(client, "sg_alea_replica");
            if (securityGroup.isEmpty()) {
                Optional<Vpc> defaultVpc = awsService.getVpcs(client).stream().filter(Vpc::isDefault).findAny();
                if (defaultVpc.isEmpty()) System.exit(-1);

                // create replica security group
                String groupId = awsService.createSecurityGroup(
                        client, "sg_alea_replica", "Replica Server Security Group", defaultVpc.get().vpcId());

                // add security rules
                IpRange ipRange = IpRange.builder().cidrIp("0.0.0.0/0").build();
                awsService.addSecurityRules(
                        client,
                        groupId,
                        // open protocol port for inbound connections
                        IpPermission.builder().ipProtocol("tcp").fromPort(8081).toPort(8082).ipRanges(ipRange).build(),
                        // open control port for inbound connections
                        IpPermission.builder().ipProtocol("tcp").fromPort(9420).toPort(9420).ipRanges(ipRange).build(),
                        // open ssh port
                        IpPermission.builder().ipProtocol("tcp").fromPort(22).toPort(22).ipRanges(ipRange).build()
                );
            }

            // import alea_key key pair
            Optional<KeyPairInfo> keyPair = awsService.getKeyPairs(client).stream()
                    .filter(k -> k.keyName().equals("alea_key")).findAny();
            if (keyPair.isEmpty()) {
                byte[] publicKey = null;
                try {
                    publicKey = Files.readAllBytes(new File("alea_key.pub").toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                this.awsService.importKeyPair(client,"alea_key", publicKey);
                logger.info("Imported alea_key key pair.");
            }

            // create new instance
            logger.info("Creating new Ec2 instance...");
            String name = "alea_replica";
            String amiId = awsService.getAmiId(region);
            String script = new StringBuilder()
                    .append("#!/bin/bash\n\n")
                    .append("sudo yum update -y\n")
                    .append("sudo yum install java-11-amazon-corretto-headless -y\n")
                    .append("wget -q ").append(config.getResourcesServer()).append("/download/replica -O /home/ec2-user/replica.jar\n")
                    .append("java -jar /home/ec2-user/replica.jar 0 http://").append(config.getMasterAddr()).append(":8080\n")
                    .append("echo \"java -jar /home/ec2-user/replica.jar 0 http://").append(config.getMasterAddr()).append(":8080\" | sudo tee -a /etc/rc.local")
                    .append("sudo chmod +x /etc/rc.d/rc.local")
                    .toString();
            instance = awsService.createInstance(client, name, amiId, "sg_alea_replica", script);

        } else {
            // boot available instance
            instance = availableInstances.get(0);
            logger.info("Rebooting Ec2 instance {}...", instance.instanceId());
            awsService.startInstance(client, instance);
        }
        logger.info("Done ({}).", instance.instanceId());

        // close client connection
        client.close();

        return true;
    }

    @Override
    public boolean visit(AwsTerminateCommand cmd) {
        for (Region region: awsService.getRegions()) {
            Ec2Client client = awsService.getClient(region);
            // logger.info("Initialized client for region {}.", region.id());

            // check for available instances in the region
            final List<Tag> tags = List.of(
                    Tag.builder().key("Name").value("alea_replica").build(),
                    Tag.builder().key("Name").value("alea_pcs").build());
            List<Instance> instances = awsService.getInstances(client).stream()
                    .filter(i -> !Collections.disjoint(i.tags(), tags) && !i.state().name().equals(InstanceStateName.TERMINATED))
                    .collect(Collectors.toList());

            if (!instances.isEmpty())
                logger.info("Found {} instances in {} region.", region.id(), instances.size());

            // terminate instance
            for (Instance instance: instances) {
                awsService.terminateInstance(client, instance);
                logger.info("Terminated instance {}.", instance.instanceId());
            }

            // close client connection
            client.close();
        }

        return true;
    }
}
