package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class AwsCommand extends Command {

    private final String region;

    public AwsCommand(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
