package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SpawnPcsCommand extends Command {

    private final String id;
    private final String node;

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
