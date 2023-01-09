package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Client {
    private final String name;
    private final String address;
    private final int port;

    private ManagedChannel channel;
    public Client(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
    public int getPort() {
        return port;
    }

    public ManagedChannel getChannel() {
        if (channel == null)
            channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build();
        return channel;
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port='" + port + '\'' +
                '}';
    }

}
