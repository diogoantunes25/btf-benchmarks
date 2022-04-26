package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

// pcs = process creation service (used to spawn replicas)
public class Pcs {

    public enum Status {
        ONLINE,
        OFFLINE,
        UNKNOWN
    }

    private final String name;
    private final String address;
    private final Integer port;

    private Status status;

    private ManagedChannel channel;

    public Pcs(String name, String address, Integer port) {
        this.name = name;
        this.address = address;
        this.port = port;

        this.status = Status.UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ManagedChannel getChannel() {
        if (channel == null)
            channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build();
        return channel;
    }

    @Override
    public String toString() {
        return "Pcs{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", status=" + status +
                '}';
    }
}
