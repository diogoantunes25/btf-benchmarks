package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import com.google.common.base.Verify;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.*;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.service.grpc.BenchmarkGrpcService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int TOLERANCE = 1;
    private static final int NUM_REPLICAS = 3 * TOLERANCE + 1;
    private static final int BASE_PORT = 9000;
    private static final int BASE_CONTROL_PORT = 10000;

    public static void main(String[] args) {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        // Parse args
        Config config = Config.fromArgs(args);

        // Register with master
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(config.getMasterUri().getHost(), config.getMasterUri().getPort())
                .usePlaintext()
                .build();
        RegisterServiceGrpc.RegisterServiceBlockingStub stub = RegisterServiceGrpc.newBlockingStub(channel);

        String ipAddr = null;
        try {
            URL whatsMyIp = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatsMyIp.openStream()));
            ipAddr = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        RegisterServiceOuterClass.RegisterRequest request = RegisterServiceOuterClass.RegisterRequest.newBuilder()
                //.setReplicaId(config.getReplicaId())
                .setAddress(ipAddr)
                .setPort(BASE_PORT + config.getReplicaId())
                .setControlPort(BASE_CONTROL_PORT + config.getReplicaId())
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
                .forPort(BASE_CONTROL_PORT + config.getReplicaId())
                .addService(new BenchmarkGrpcService(replicaId))
                .build();
        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
