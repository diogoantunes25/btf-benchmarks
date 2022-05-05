package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// replica.replica.BenchmarkReplica
public class LatencyReplica extends BenchmarkReplica {

    private static int proposalCount = 0;
    
    private long startTime;

    private long proposeTime;
    private byte[] payload;

    // FIXME: I think that LatencyReplicas should have a list of 
    // executions and not measurements (check Json Service meauremenets in master)
    private final List<Measurement> measurements = new ArrayList<>();
    private final List<Execution> executions = new ArrayList<>();

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
        // TODO: Why is this different from the throughput version
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

        System.out.println(proposalCount + " proposals");

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

    @Override
    public void deliver(Block block) {
        System.out.println("delivery");
        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        for (byte[] entry: block.getEntries()) {
            if (this.payload != null && Arrays.equals(this.payload, entry)) {
                this.executions.add(new Execution("node", proposeTime , timestamp, true));
                this.measurements.add(new Measurement(timestamp, (timestamp - proposeTime)));

                // As soon as the proposal is delievered, a new random on is proposed
                this.handleStep(this.propose());
                break;
            }
        }
    }

    private Step<Block> propose() {

        // Generates a random proposal

        this.proposeTime = ZonedDateTime.now().toInstant().toEpochMilli();

        Random rng = new Random();
        this.payload = new byte[250];
        rng.nextBytes(payload);

        // FIXME: Remove this counter
        proposalCount++;
        System.out.println("new proposal " + proposalCount);

        return this.protocol.handleInput(this.payload);
    }
}