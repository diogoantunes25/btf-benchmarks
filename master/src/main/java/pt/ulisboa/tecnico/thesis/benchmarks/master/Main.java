package pt.ulisboa.tecnico.thesis.benchmarks.master;
import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.PingServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.PingServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.CommandParser;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.Command;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.ExecuteVisitor;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Pcs;
import pt.ulisboa.tecnico.thesis.benchmarks.master.exception.InvalidCommandException;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.BenchmarkRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.PcsRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ReplicaRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.local.BenchmarkService;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.grpc.RegisterGrpcService;

import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final int DEFAULT_MASTER_PORT = 15000;
    
    /**
     * @param args Resources server and master address (both optional). By default
     * resources server is set to a fixed address and the master address to the
     * current IP.
     */
    public static void main(String[] args) {

        // Set up ----------------------------------------------------
        // -----------------------------------------------------------

        // parse args
        Config config = Config.fromArgs(args);

        // init data repositories
        PcsRepository pcsRepository = new PcsRepository();
        ReplicaRepository replicaRepository = new ReplicaRepository();
        BenchmarkRepository benchmarkRepository = new BenchmarkRepository();

        // init services
        BenchmarkService benchmarkService = new BenchmarkService(replicaRepository, benchmarkRepository);

        // for all pcs whose status is unknown pings. If ping goes well
        // set status as ONLINE, otherwise set as offline
        for (Pcs pcs: pcsRepository.getAll().stream().filter(p -> p.getStatus().equals(Pcs.Status.UNKNOWN)).collect(Collectors.toList())) {
            ManagedChannel channel = pcs.getChannel();
            PingServiceGrpc.PingServiceBlockingStub stub = PingServiceGrpc.newBlockingStub(channel);
            PingServiceOuterClass.Ping ping = PingServiceOuterClass.Ping.newBuilder().build();
            try {
                stub.ping(ping);
                pcs.setStatus(Pcs.Status.ONLINE);
            } catch (StatusRuntimeException e) {
                pcs.setStatus(Pcs.Status.OFFLINE);
                channel.shutdownNow();
            }
        }

        System.out.println("Master running on port " + DEFAULT_MASTER_PORT);

        Server server = ServerBuilder
                .forPort(DEFAULT_MASTER_PORT)
                .addService(new RegisterGrpcService(replicaRepository))
                .addService(ProtoReflectionService.newInstance())
                .build();


        // Command loop ----------------------------------------------
        // -----------------------------------------------------------

        try {
            server.start();

            try (Scanner scanner = new Scanner(System.in)) {
                CommandVisitor visitor = new ExecuteVisitor(config, pcsRepository, replicaRepository, benchmarkService);
                while (true) {
                    System.out.print("$ ");
                    String line = scanner.nextLine();
                    if (line.equals("exit")) break;

                    try {
                        Command command = CommandParser.parse(line);
                        command.accept(visitor);
                    } catch (InvalidCommandException e) {
                        System.out.println("Unable to parse command.");
                    }
                }
            }

            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
