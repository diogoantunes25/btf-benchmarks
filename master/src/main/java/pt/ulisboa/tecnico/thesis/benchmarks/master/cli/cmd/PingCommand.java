package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class PingCommand extends Command {

    private final String group;
    private final String id;

    public PingCommand(String group, String id) {
        this.group = group;
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
