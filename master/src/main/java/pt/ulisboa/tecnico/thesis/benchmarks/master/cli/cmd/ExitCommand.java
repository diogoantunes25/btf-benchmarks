package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class ExitCommand extends Command {
    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
