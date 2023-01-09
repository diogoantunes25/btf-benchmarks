package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
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


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ThroughputReplica extends BenchmarkReplica {

    private static final long MEGABYTE = 1024L * 1024L;

    // FIXME: Get interface on a per machine basis
    private static final String INTERFACE = "br0";

    private final Logger logger = LoggerFactory.getLogger(ThroughputReplica.class);

    private long startTime;


    private long txSubmitted = 0;

    private long txDropped = 0;

    private AtomicLong txPending;

    private long maxPending;

    private int load;

    private Thread systemMonitor;

    private Thread loader;

    public ThroughputReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport, int load, int batchSize) {
        super(protocol, encoder, transport);
        this.load = load;
        this.txPending = new AtomicLong();

        this.maxPending = (load > batchSize ? load : batchSize) * 60;

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

        this.systemMonitor = (new Thread(new SystemMonitor(INTERFACE)));
        this.systemMonitor.start();
        logger.info("System Monitor started");
    }

    public Benchmark stop() {
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();

        this.protocol.stop();

        this.loader.interrupt();
        logger.info("Load generator interrupted");

        this.systemMonitor.interrupt();
        logger.info("System monitor interrupted");

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
     * Generates random payload, specific to current replica.
     * Payload content includes submit timestamp and replica id.
     * @return New payload
     */
    private byte[] getPayload() {
        byte[] payload = new byte[250];

        (new Random()).nextBytes(payload);

        payload[0] = (byte) this.transport.getMyId();

        long submitTime = ZonedDateTime.now().toInstant().toEpochMilli();
        // logger.info("The submit time is {}", submitTime);
        byte[] submitTimeBytes = longToBytes(submitTime);

        for (int i = 0; i < submitTimeBytes.length; i++) {
            payload[i+1] = submitTimeBytes[i];
        }

        return payload;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }


    @Override
    public void deliver(Block block) {

        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();

        synchronized (this) {
            for (byte[] entry: block.getEntries()) {

                int replicaId = entry[0];
                if (replicaId == this.transport.getMyId()) {
                    long submitTime = bytesToLong(Arrays.copyOfRange(entry, 1, 1 + Long.BYTES));

                    long pending = txPending.decrementAndGet();
                    // logger.info("Commit (latency : {}, pending: {})", timestamp - submitTime, pending);

                    // Save measurement
                    synchronized (this.executions) {
                        this.executions.add(new Execution(submitTime, timestamp));
                    }
                }
            }
        }
    }

    private class SystemMonitor implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(SystemMonitor.class);
        private static final int WAIT_PERIOD = 1000;
        private String inter;

        private OperatingSystemMXBean bean;
        public SystemMonitor(String inter) {
            this.inter = inter;
            this.bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                    .getOperatingSystemMXBean();
        }

        public void run() {
            double load, in, out;
            String line;
            try {
                ProcessBuilder pb = new ProcessBuilder("ifstat", "-n", "-i", this.inter);
                Process p = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                // Ignore first two lines of output
                reader.readLine();
                reader.readLine();
                while (true) {
                    // Measure CPU load
                    // logger.info("Checking CPU load");
                    load = bean.getSystemLoadAverage();
                    cpuLoadMeasurements.add(load);

                    // Measure bandwidth
                    // logger.info("Checking bandwidth");
                    line = reader.readLine();
                    String[] things = line.strip().split(" ");
                    try {
                        in = Double.parseDouble(things[0]);
                    } catch (NumberFormatException e) {
                        in = 0;
                    }
                    try {
                        out = Double.parseDouble(things[things.length-1]);
                    } catch (NumberFormatException e) {
                        out = 0;
                    }

                    receivedBandwidthMeasurements.add(in);
                    sentBandwidthMeasurements.add(out);

                    // Measure memory usage
                    long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    // logger.info("Total memory: {} MB", memory / MEGABYTE);

                    Thread.sleep(WAIT_PERIOD);
                }
            } catch (InterruptedException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class LoadGenerator implements Runnable {

        // Sleep time in milliseconds
        private static final int TICK_PERIOD = 10;
        private int load;
        private final Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

        private int counter = 0;

        private double pendingAcumulator = 0;

        public LoadGenerator(int load) {
            this.load = load;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(TICK_PERIOD); // FIXME: subtract time already spent
                    pendingAcumulator += load * (TICK_PERIOD * Math.pow(10,-3));
                    if (pendingAcumulator >= 1) {
                        int toSend = (int) Math.floor(pendingAcumulator);
                        pendingAcumulator -= toSend;
                        txSubmitted += toSend;

                        long pending = txPending.addAndGet(toSend);

                        if (pending <= maxPending) {
                            (new Thread(() -> {
                                // logger.info("Submitting {} payloads (pending: {}, max: {})", toSend, txPending.get(), maxPending);
                                for (int i = 0; i < toSend; i++) {
                                    byte[] payload = getPayload();
                                    handleStep(protocol.handleInput(payload));
                                }
                            })).run();
                        } else {
                            txPending.addAndGet(-toSend);
                            txDropped += toSend;
                        }
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