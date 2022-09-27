package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.Command;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class StartCommand extends Command {

    private int load;

    public StartCommand(int load) {
        this.load = load;
    }
    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }

    public int getLoad() { return this.load; }
}
