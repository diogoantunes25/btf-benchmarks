package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.PriorityBlockingQueue;

// replica.replica.BenchmarkReplica
public class LatencyReplica extends BenchmarkReplica {


    private static int proposalCount = 0;
    
    private long startTime;

    private long proposeTime;
    private long deliverTime = 0;
    private byte[] payload;

    private final List<Measurement> measurements = Collections.synchronizedList(new ArrayList<>());
    private final List<Execution> executions = Collections.synchronizedList(new ArrayList<>());

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
        logger.info("Starting");

        Step<Block> step = this.propose();
        this.handleStep(step);
    }


    @Override
    public Benchmark stop() {
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();
        logger.info("Stopping");

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }

        executions.add(new Execution("commiter", this.proposeTime, this.deliverTime, this.deliverTime != 0));

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

    @Override
    public void deliver(Block block) {
        // logger.info("deliver called - {}", block);
        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        for (byte[] entry: block.getEntries()) {
            // Check if my payload was delivered
            if (this.payload != null && Arrays.equals(entry, this.payload)) {
                try {
                    logger.info("My payload was delivered (latency = {})", timestamp - this.proposeTime);
                }
                catch (MissingFormatArgumentException e) {
                    e.printStackTrace();
                }
                this.deliverTime = timestamp;
            }
        }
    }

    private Step<Block> propose() {

        // Generate a random command to submit
        Random rng = new Random();
        this.payload = new byte[250];
        rng.nextBytes(payload);
        // Make the random unique for all replicas
        // TODO: Place replica id at beginning of payload (or something unique for the replica)

        this.logger.info("Proposed new entry");

        this.proposeTime = ZonedDateTime.now().toInstant().toEpochMilli();
        return this.protocol.handleInput(this.payload);
    }
}