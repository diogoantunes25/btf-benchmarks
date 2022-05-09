package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import java.util.PriorityQueue;

import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.Step;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Measurement;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Execution;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.time.ZonedDateTime;
import java.util.*;

// replica.replica.BenchmarkReplica
public class LatencyReplica extends BenchmarkReplica {

    private static int proposalCount = 0;
    
    private long startTime;

    private long proposeTime;
    private byte[] payload;

    private final List<Measurement> measurements = new ArrayList<>();
    private final List<Execution> executions = new ArrayList<>();

    private final Queue<Long> proposeTimes = new PriorityQueue<>();

    public LatencyReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport) {
        super(protocol, encoder, transport);

        // start listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(this);
        }
    }

    @Override
    public void start() {
        // log starting time
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();

        System.out.println("Starting...");
        Step<Block> step = this.propose();
        System.out.println("Proposed...");
        this.handleStep(step);
        System.out.println("Handled step...");
    }


    @Override
    public Benchmark stop() {
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

    @Override
    public void deliver(Block block) {
        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        for (byte[] entry: block.getEntries()) {
            Long proposeTime = proposeTimes.poll();
            if (proposeTime != null) {
                this.executions.add(new Execution("node", proposeTime , timestamp, true));
                this.measurements.add(new Measurement(timestamp, (timestamp - proposeTime)));

                // As soon as the proposal is delivered, a new random on is proposed
                this.handleStep(this.propose());
                break;
            }
            else {
                System.out.println("[error] More delivers than proposals.");
            }
        }
    }

    private Step<Block> propose() {

        // Generates a random proposal
        // FIXME: in alea doesn't work

        Random rng = new Random();
        this.payload = new byte[250];
        rng.nextBytes(payload);

        long proposeTime = ZonedDateTime.now().toInstant().toEpochMilli();

        this.proposeTimes.add(proposeTime);

        System.out.println("Proposed new batch");

        return this.protocol.handleInput(this.payload);
    }
}