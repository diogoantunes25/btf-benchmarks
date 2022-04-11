package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class AwsTerminateCommand extends Command {

    private final String instanceId;

    public AwsTerminateCommand(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
