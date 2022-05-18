package pt.ulisboa.tecnico.benchmarks.pcs;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.benchmarks.pcs.service.PingService;
import pt.ulisboa.tecnico.benchmarks.pcs.service.ProcessCreationService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        System.out.println("PCS is running...");

        // parse args
        Config config = Config.fromArgs(args);

        Server server = ServerBuilder
                .forPort(config.getPort())
                .addService(new ProcessCreationService(config.getMasterUri()))
                .addService(new PingService())
                .build();

        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
