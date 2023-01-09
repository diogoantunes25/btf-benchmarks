package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class RegisterClientCommand extends Command {

    private final String node;

    public RegisterClientCommand(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }
    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}

