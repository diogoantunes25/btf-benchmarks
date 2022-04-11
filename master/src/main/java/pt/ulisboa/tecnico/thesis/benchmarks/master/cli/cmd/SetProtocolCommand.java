package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SetProtocolCommand extends Command {

    private final String protocol;
    private final Integer batchSize;

    private final String benchmarkMode;
    private final String faultMode;

    public SetProtocolCommand(String protocol, Integer batchSize) {
        this(protocol, batchSize, "throughput", "free");
    }

    public SetProtocolCommand(String protocol, Integer batchSize, String benchmarkMode) {
        this(protocol, batchSize, benchmarkMode, "free");
    }

    public SetProtocolCommand(String protocol, Integer batchSize, String benchmarkMode, String faultMode) {
        this.protocol = protocol;
        this.batchSize = batchSize;
        this.benchmarkMode = benchmarkMode;
        this.faultMode = faultMode;
    }

    public String getProtocol() {
        return protocol;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public String getBenchmarkMode() {
        return benchmarkMode;
    }

    public String getFaultMode() {
        return faultMode;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
