package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SpawnPcsCommand extends Command {

    private final String id;
    private final String region;

    public SpawnPcsCommand(String id, String region) {
        this.id = id;
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }


    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
