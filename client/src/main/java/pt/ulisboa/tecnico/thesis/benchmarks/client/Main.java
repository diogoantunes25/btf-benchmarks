package pt.ulisboa.tecnico.thesis.benchmarks.client;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import pt.ulisboa.tecnico.thesis.benchmarks.client.exceptions.ReplicasUnknownException;
import pt.ulisboa.tecnico.thesis.benchmarks.client.service.grpc.LoadGrpcService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final int CLIENT_PORT = 20000;
    public static void main(String args[]) {
        Client client = new Client(1000);

        // Start server
        Server server = ServerBuilder
                .forPort(CLIENT_PORT)
                .addService(new LoadGrpcService(client))
                .build();

        try {
            server.start();
            System.out.println("client server started - waiting for commands");
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
