package pt.ulisboa.tecnico.benchmarks.pcs;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import pt.ulisboa.tecnico.benchmarks.pcs.service.PingService;
import pt.ulisboa.tecnico.benchmarks.pcs.service.ProcessCreationService;

import java.io.IOException;

public class Main {

    private static final int DEFAULT_PCS_PORT = 8500;
    public static void main(String[] args) {


        // parse args
        Config config = Config.fromArgs(args);

        Server server = ServerBuilder
                .forPort(DEFAULT_PCS_PORT)
                .addService(new ProcessCreationService(config.getMasterUri()))
                .addService(new PingService())
                .addService(ProtoReflectionService.newInstance())
                .build();

        // System.out.println("PCS running on port " + config.getPort());
        System.out.println("PCS running on port " + DEFAULT_PCS_PORT);

        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
