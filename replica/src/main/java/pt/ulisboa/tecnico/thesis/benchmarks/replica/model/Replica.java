package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;

public class Replica {
    private final int id;
    private final String address;
    private final int port;

    public Replica(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public Replica(BenchmarkServiceOuterClass.TopologyRequest.Replica replica) {
        this(replica.getId(), replica.getAddr(), replica.getPort());
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
