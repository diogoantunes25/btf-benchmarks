package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor.CommandVisitor;

public class RunScriptCommand extends Command {

    private final String script;

    public RunScriptCommand(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @Override
    public void accept(CommandVisitor v) {
        v.visit(this);
    }
}
