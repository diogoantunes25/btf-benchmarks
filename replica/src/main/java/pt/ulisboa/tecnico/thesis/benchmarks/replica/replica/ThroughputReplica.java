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
    private final Queue<Long> proposeTimes = new PriorityBlockingQueue<>();
    private final List<Measurement> measurements = new ArrayList<>();
    private final List<Execution> executions = new ArrayList<>();

    private Thread loader;

    public ThroughputReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport) {
        super(protocol, encoder, transport);

        // start listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(this);
        }
    }

    /**
     *
     * @param first Whether this if the replica selected to input
     * @param load Load to keep on the system (tx/sec)
     */
    public void start(boolean first, int load) {

        // log starting time
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        this.protocol.reset();

        this.loader = (new Thread(new LoadGenerator(load)));
        this.loader.start();
        logger.info("Load generator started");

        // input any random value into the protocol (in benchmark mode)
//        logger.info("Handle Input");
//        Step<Block> step = this.protocol.handleInput(new byte[0]);
//        logger.info("Handle step");
//        this.handleStep(step);
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

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

//    public Benchmark stop() {
//        System.out.println("[ThroughputReplica.stop] started");
//        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();
//
//        // stop listeners
//        for (Connection connection: this.transport.getConnections()) {
//            connection.setListener(null);
//        }
//
//        List<Execution> executions = new ArrayList<>();
//        if (this.protocol instanceof Alea) {
//
//            executions = ((Alea) this.protocol).getExecutionLog().getChildren().stream().filter(e->e.getFinish() != null)
//                    .map(e -> new Execution(e.getPid(), e.getStart(), e.getFinish(), e.getResult() instanceof Boolean ? (Boolean) e.getResult() : false)).collect(Collectors.toList());
//        }
//
//        return new Benchmark.Builder(startTime)
//                .finishTime(finishTime)
//                .sentMessageCount(this.sentMessageCount.get())
//                .recvMessageCount(this.recvMessageCount.get())
//                .measurements(measurements)
//                .executions(executions)
//                .build();
//    }

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

        // logger.info("delivered called");

        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();

        synchronized (this) {
            for (byte[] entry: block.getEntries()) {
                // Check if my payload was delivered
                Long submitTime = pendingPayloads.remove(new Entry(entry));
                if (submitTime != null) {

                    logger.info("A payload of mine came back (latency = {})", timestamp - submitTime);

                    // Save measurement
                    this.executions.add(new Execution("committer", submitTime, timestamp, true));
                } else {
                    // logger.info("Received foreign payload");
                }
            }
        }
    }

    private class LoadGenerator implements Runnable {
        private int load;
        private final Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

        private int counter = 0;

        public LoadGenerator(int load) {
            this.load = load;
        }
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < load; i++) {
                        long submitTime = ZonedDateTime.now().toInstant().toEpochMilli();

                        byte[] payload = getPayload();
                        handleStep(protocol.handleInput(payload));
                        pendingPayloads.put(new Entry(payload), submitTime);

                        logger.info("Payload #{} sent", ++counter);
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
