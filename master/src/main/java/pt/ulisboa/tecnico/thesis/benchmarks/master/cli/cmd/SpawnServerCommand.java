package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SpawnServerCommand extends Command {

    private final String pcs;
    // private final Integer replicaId;

    public SpawnServerCommand(String pcs) {
        this.pcs = pcs;
    }

    public String getPcs() {
        return pcs;
    }


    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
