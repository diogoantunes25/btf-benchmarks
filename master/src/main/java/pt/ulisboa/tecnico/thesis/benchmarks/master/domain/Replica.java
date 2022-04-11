package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Replica {
    private final int replicaId;
    private final String address;
    private final int port;
    private final int controlPort;

    private ManagedChannel channel;

    public Replica(int replicaId, String address, int port, int controlPort) {
        this.replicaId = replicaId;
        this.address = address; //"127.0.0.1"; //
        this.port = port;
        this.controlPort = controlPort;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getControlPort() {
        return controlPort;
    }

    public ManagedChannel getChannel() {
        if (channel == null)
            channel = ManagedChannelBuilder.forAddress(address, controlPort).usePlaintext().build();
        return channel;
    }

    @Override
    public String toString() {
        return "Replica{" +
                "replicaId=" + replicaId +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", controlPort=" + controlPort +
                '}';
    }
}
