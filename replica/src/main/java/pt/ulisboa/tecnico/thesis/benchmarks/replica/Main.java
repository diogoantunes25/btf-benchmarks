package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import com.google.common.base.Verify;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.*;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.service.grpc.BenchmarkGrpcService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int TOLERANCE = 1;
    private static final int NUM_REPLICAS = 3 * TOLERANCE + 1;
    private static final int BASE_PORT = 9000;
    private static final int BASE_CONTROL_PORT = 10000;

    public static void main(String[] args) throws UnknownHostException {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        // Parse args
        Config config = Config.fromArgs(args);

        int mainPort = BASE_PORT + config.getOffset();
        int controlPort = BASE_CONTROL_PORT + config.getOffset();

        System.out.println("OUTPUT FILE FOR REPLICA " + config.getReplicaId());
        System.err.println("LOG FILE FOR REPLICA " + config.getReplicaId());

        System.out.println("Building replica...");
        System.out.println("URI: " + config.getMasterUri());
        System.out.println("Master: [host] " + config.getMasterUri().getHost() + " [port] " + config.getMasterUri().getPort());
        System.out.println("Listening on " + mainPort + " (main) and " + controlPort + " (control)");

        // Register with master
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(config.getMasterUri().getHost(), config.getMasterUri().getPort())
                .usePlaintext()
                .build();
        RegisterServiceGrpc.RegisterServiceBlockingStub stub = RegisterServiceGrpc.newBlockingStub(channel);

        RegisterServiceOuterClass.RegisterRequest request = RegisterServiceOuterClass.RegisterRequest.newBuilder()
                //.setReplicaId(config.getReplicaId())
                .setAddress(config.getPcsIP().getHostAddress())
                .setPort(BASE_PORT + config.getOffset())
                .setControlPort(BASE_CONTROL_PORT + config.getOffset())
                .build();

        RegisterServiceOuterClass.RegisterResponse response = stub.register(request);
        int replicaId = response.getReplicaId();

        // shutdown register channel
        channel.shutdown();
        try {
            channel.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            channel.shutdownNow();
        }
        Verify.verify(channel.isShutdown());

        // Start server
        Server server = ServerBuilder
                .forPort(BASE_CONTROL_PORT + config.getOffset())
                .addService(new BenchmarkGrpcService(replicaId))
                .addService(ProtoReflectionService.newInstance())
                .build();

        try {
            server.start();
            System.out.println(String.format("Replica %d server started", config.getReplicaId()));
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
