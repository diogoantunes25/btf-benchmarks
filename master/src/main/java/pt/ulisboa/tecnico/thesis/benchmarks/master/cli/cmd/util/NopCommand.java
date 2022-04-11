package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.util;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.Command;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class NopCommand extends Command {
    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
