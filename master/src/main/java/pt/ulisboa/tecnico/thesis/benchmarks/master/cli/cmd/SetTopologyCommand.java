package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

import java.util.List;

public class SetTopologyCommand extends Command {

    private final List<Integer> replicaIds;

    public SetTopologyCommand(List<Integer> replicaIds) {
        this.replicaIds = replicaIds;
    }

    public List<Integer> getReplicaIds() {
        return replicaIds;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
