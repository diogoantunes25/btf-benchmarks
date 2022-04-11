package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.Command;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class StartCommand extends Command {

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
