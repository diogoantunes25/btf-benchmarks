package pt.ulisboa.tecnico.thesis.benchmarks.master.cli.visitor;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.*;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StartCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StopCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.util.NopCommand;

public interface CommandVisitor {
    boolean visit(SpawnPcsCommand cmd);
    boolean visit(SpawnServerCommand cmd);
    boolean visit(RegisterClientCommand cmd);

    boolean visit(ExitCommand cmd);
    boolean visit(ListCommand cmd);
    boolean visit(PingCommand cmd);

    // benchmark commands
    boolean visit(SetTopologyCommand cmd);
    boolean visit(SetProtocolCommand cmd);
    boolean visit(StartCommand cmd);
    boolean visit(StopCommand cmd);

    // TODO deprecated?
    boolean visit(RunBenchmarkCommand cmd);
    boolean visit(ShutdownCommand cmd);

    // util commands
    boolean visit(NopCommand cmd);
    boolean visit(SleepCommand cmd);
    boolean visit(RunScriptCommand cmd);

    // aws commands
    boolean visit(AwsCommand cmd);
    boolean visit(AwsTerminateCommand cmd);
}
