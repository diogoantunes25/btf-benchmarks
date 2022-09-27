package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import java.math.BigInteger;
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


    private static BigInteger proposalCount = BigInteger.ZERO;

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
    public void start(boolean first, int load) {
        // log starting time
        protocol.reset();
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        logger.info("Starting");

        Step<Block> step = this.propose();
        logger.info("Handling following steps");
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

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

    @Override
    public void deliver(Block block) {

        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();

        synchronized (this) {
            for (byte[] entry: block.getEntries()) {
                // Check if my payload was delivered
                if (this.payload != null && Arrays.equals(entry, this.payload)) {
                    try {
                        logger.info("{}", entry);
                        logger.info("Payload number {} was delivered (latency = {}). Submitting new payload.", this.proposalCount, timestamp - this.proposeTime);
                    }
                    catch (MissingFormatArgumentException e) {
                        e.printStackTrace();
                    }
                    this.deliverTime = timestamp;
                    this.executions.add(new Execution("commiter", this.proposeTime, timestamp, true));

                    this.handleStep(this.propose());
                }
            }
        }
    }

    private byte[] getPayload() {
            byte[] load = new byte[250];
            System.arraycopy(this.proposalCount.toByteArray(), 0, load, 250 - this.proposalCount.toByteArray().length, this.proposalCount.toByteArray().length);

            return load;
    }

    private Step<Block> propose() {

        this.proposalCount = this.proposalCount.add(BigInteger.ONE);

        synchronized (this) {
            // Generate a command to submit
            this.payload = this.getPayload();
        }

        // Add current turn number to proposal

        // logger.info("Proposed new payload for turn {}", this.proposalCount);

        logger.info("{}", this.payload);
        this.proposeTime = ZonedDateTime.now().toInstant().toEpochMilli();
        return this.protocol.handleInput(this.payload);
    }
}