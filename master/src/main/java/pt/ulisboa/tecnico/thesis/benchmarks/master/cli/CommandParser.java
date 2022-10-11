package pt.ulisboa.tecnico.thesis.benchmarks.master.cli;

import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.*;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StartCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.benchmark.StopCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.cli.cmd.util.NopCommand;
import pt.ulisboa.tecnico.thesis.benchmarks.master.exception.InvalidCommandException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandParser {

    public static Command parse(String line) throws InvalidCommandException {
        List<String> tokens = Arrays.asList(line.split(" "));
        switch (tokens.get(0).toLowerCase()) {
            // parse spawn pcs command
            case "pcs": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 2) throw new InvalidCommandException(line);
                final String id = args.get(0);
                final String node = args.get(1);
                return new SpawnPcsCommand(id, node);
            }

            // parse spawn replica command
            case "replica": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 2) throw new InvalidCommandException(line);
                return new SpawnServerCommand(args.get(0));
            }

            // parse exit command
            case "exit": {
                return new ExitCommand();
            }

            // parse list command
            case "list": {
                return new ListCommand();
            }

            case "topology": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 1) throw new InvalidCommandException(line);
                return new SetTopologyCommand(args.stream().map(Integer::parseInt).collect(Collectors.toList()));
            }

            case "protocol": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 5) throw new InvalidCommandException(line);
                final String protocol = args.get(0);
                final int batchSize = Integer.parseInt(args.get(1));
                final String mode = args.get(2);
                final String fault = args.get(3);
                final int load = Integer.parseInt(args.get(4));
                return new SetProtocolCommand(protocol, batchSize, mode, fault, load);
            }

            case "start": {
                return new StartCommand();
            }

            case "stop": {
                return new StopCommand();
            }

            case "benchmark": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 2) throw new InvalidCommandException(line);
                final int numRequests = Integer.parseInt(args.get(0));
                final int payloadSize = Integer.parseInt(args.get(1));
                return new RunBenchmarkCommand(numRequests, payloadSize);
            }

            case "shutdown": {
                List<String> args = tokens.subList(1, tokens.size());
                final int timer = args.size() < 1 ? 0 : Integer.parseInt(args.get(0));
                return new ShutdownCommand(timer);
            }

            case "nop":
            case "": {
                return new NopCommand();
            }

            case "sleep": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 1) throw new InvalidCommandException(line);
                final long duration = Long.parseLong(args.get(0));
                return new SleepCommand(duration);
            }

            case "script": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 1) throw new InvalidCommandException(line);
                final String script = args.get(0);
                return new RunScriptCommand(script);
            }

            case "aws": {
                List<String> args = tokens.subList(1, tokens.size());
                if (args.size() < 1) throw new InvalidCommandException(line);
                final String region = args.get(0);
                return new AwsCommand(region);
            }
            case "terminate": {
                List<String> args = tokens.subList(1, tokens.size());
                final String instanceId = args.size() < 1 ? "all" : args.get(0);
                return new AwsTerminateCommand(instanceId);
            }

            default:
                throw new InvalidCommandException(line);
        }
    }
}
