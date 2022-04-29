package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.Step;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Measurement;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LatencyReplica extends BenchmarkReplica {

    private long startTime;

    private long proposeTime;
    private byte[] payload;

    private final List<Measurement> measurements = new ArrayList<>();

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

        return new Benchmark(startTime, measurements, finishTime);
    }

    @Override
    public void deliver(Block block) {
        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        for (byte[] entry: block.getEntries()) {
            if (this.payload != null && Arrays.equals(this.payload, entry)) {
                this.measurements.add(new Measurement(timestamp, (timestamp - proposeTime)));
                this.handleStep(this.propose());
                break;
            }
        }
    }

    private Step<Block> propose() {

        this.proposeTime = ZonedDateTime.now().toInstant().toEpochMilli();

        Random rng = new Random();
        this.payload = new byte[250];
        rng.nextBytes(payload);

        return this.protocol.handleInput(this.payload);
    }
}
