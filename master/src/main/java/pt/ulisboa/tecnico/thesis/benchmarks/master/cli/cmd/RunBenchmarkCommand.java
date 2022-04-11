package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class RunBenchmarkCommand extends Command {

    private final int numRequests;
    private final int payloadSize;

    public RunBenchmarkCommand(int numRequests, int payloadSize) {
        this.numRequests = numRequests;
        this.payloadSize = payloadSize;
    }

    public int getNumRequests() {
        return numRequests;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
