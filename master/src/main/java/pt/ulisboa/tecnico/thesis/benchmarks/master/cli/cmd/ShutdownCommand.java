package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class ShutdownCommand extends Command {

    private final Integer timer;

    public ShutdownCommand(Integer timer) {
        this.timer = timer;
    }

    public Integer getTimer() {
        return timer;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
