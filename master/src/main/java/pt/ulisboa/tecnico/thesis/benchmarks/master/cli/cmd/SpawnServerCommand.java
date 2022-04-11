package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SpawnServerCommand extends Command {

    private final String pcs;
    private final Integer replicaId;

    public SpawnServerCommand(String pcs, Integer replicaId) {
        this.pcs = pcs;
        this.replicaId = replicaId;
    }

    public String getPcs() {
        return pcs;
    }

    public Integer getReplicaId() {
        return replicaId;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
