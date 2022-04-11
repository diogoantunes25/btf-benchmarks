package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class SleepCommand extends Command {

    private final long duration;

    public SleepCommand(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
