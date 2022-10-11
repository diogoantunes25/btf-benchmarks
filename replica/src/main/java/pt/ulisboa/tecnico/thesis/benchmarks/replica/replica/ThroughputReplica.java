package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.Step;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.alea.Alea;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Execution;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Measurement;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

// FIXME: Remove this
import pt.tecnico.ulisboa.hbbft.abc.alea.benchmark.ExecutionLog;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class ThroughputReplica extends BenchmarkReplica {

    private final Logger logger = LoggerFactory.getLogger(ThroughputReplica.class);

    private long startTime;

    /**
     * Maps pending payloads to their submit time.
     */
    private final Map<Entry, Long> pendingPayloads = new ConcurrentHashMap<>();

    /**
     * Max number of pending payloads (must exist otherwise heap capacity is reached).
     */
    private final int maxPendingPayload;

    private long txSubmitted = 0;

    private long txDropped = 0;
    private int load;

    private Thread loader;

    public ThroughputReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport, int load, int batchSize) {
        super(protocol, encoder, transport);
        this.load = load;

        // Use both load and batch size to define max pending payload because:
        //    - if I only have load , if batch size is larger, I can't build a batch
        //    - if I only have batch, if load is larger it might limit throuhput
        this.maxPendingPayload = (load > batchSize ? load : batchSize) * 5;

        // start listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(this);
        }
    }

    /**
     *
     * @param first Whether this if the replica selected to input
     */
    public void start(boolean first) {

        // log starting time
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        this.protocol.reset();

        this.loader = (new Thread(new LoadGenerator(this.load)));
        this.loader.start();
        logger.info("Load generator started");
    }

    public Benchmark stop() {
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();

        this.loader.interrupt();
        logger.info("Load generator interrupted");

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }
        logger.info("setListener finished");

        return new Benchmark.Builder(startTime)
                .finishTime(finishTime)
                .executions(executions)
                .sentMessageCount(sentMessageCount.get())
                .recvMessageCount(recvMessageCount.get())
                .txSubmitted(txSubmitted)
                .txDropped(txDropped)
                .build();
    }

    /**
     * Generates random payload, specific to current replica
     * @return New payload
     */
    private byte[] getPayload() {
        byte[] payload = new byte[250];

        (new Random()).nextBytes(payload);

        payload[0] = (byte) this.transport.getMyId();

        if (this.pendingPayloads.get(payload) != null) {
            logger.info("The payload was already pending, computing new one.");
            payload = getPayload();
        }

        return payload;
    }


    @Override
    public void deliver(Block block) {

        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();

        synchronized (this) {
            for (byte[] entry: block.getEntries()) {
                // Check if my payload was delivered

                // logger.info("New payload arrived");
                int sizeBefore = pendingPayloads.size();
                Long submitTime = pendingPayloads.remove(new Entry(entry));
                int sizeAfter = pendingPayloads.size();
                if (submitTime != null) {
                    logger.info("Commit (latency : {})", timestamp - submitTime);

                    // Save measurement
                    synchronized (this.executions) {
                        this.executions.add(new Execution(submitTime, timestamp));
                    }
                } else {
                    // logger.info("Received foreign payload");
                }
            }
        }
    }

    private class LoadGenerator implements Runnable {

        // Sleep time in milliseconds
        private static final int SLEEP_TIME = 1000;
        private int load;
        private final Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

        private int counter = 0;

        public LoadGenerator(int load) {
            this.load = load;
        }
        public void run() {
            while (true) {
                try {
                    Thread.sleep(SLEEP_TIME);
                    txSubmitted += load;
                    if (pendingPayloads.size() + load < maxPendingPayload) {
                        logger.info("Submitting payloads (current: {}, max: {}, totalTx: {}, totalDropped: {})",
                                pendingPayloads.size(), maxPendingPayload, txSubmitted, txDropped);
                        for (int i = 0; i < load; i++) {
                            long submitTime = ZonedDateTime.now().toInstant().toEpochMilli();

                            byte[] payload = getPayload();
                            handleStep(protocol.handleInput(payload));
                            pendingPayloads.put(new Entry(payload), submitTime);
                        }
                    } else {
                        logger.info("Too many pending payloads, dropping payload (current: {}, max: {}, totalTx: {}, totalDropped: {})",
                                pendingPayloads.size(), maxPendingPayload, txSubmitted, txDropped);
                        txDropped += load;
                    }
                } catch(InterruptedException e) {
                    return;
                }
            }
        }
    }

    private class Entry {
        private byte[] contents;

        public Entry(byte[] contents) {
            this.contents = contents;
        }

        public byte[] getContents() {
            return contents;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof Entry) {
                Entry entry = (Entry) obj;
                byte[] otherContents = entry.getContents();
                if (this.contents.length != otherContents.length) { return false; }

                for (int i = 0; i < this.contents.length; i++) {
                    if (otherContents[i] != contents[i]) {
                        return false;
                    }
                }

                return true;
            }
            return false;
        }

        public int hashCode() {
            return Arrays.hashCode(contents);
        }

        public String toString() {
            return contents.toString();
        }
    }
}