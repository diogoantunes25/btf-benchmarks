package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SpawnPcsCommand extends Command {

    private final String id; // Name to use within program
    // TODO: Generalize to be able to acess Grid5000
    private final String node; // Node name to ssh to (GSD is assumed)

    public SpawnPcsCommand(String id, String node) {
        this.id = id;
        this.node = node;
    }

    public String getId() {
        return id;
    }

    public String getNode() {
        return node;
    }


    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
