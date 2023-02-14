package pt.ulisboa.tecnico.thesis.benchmarks.client;

import com.google.common.util.concurrent.RateLimiter;
import com.google.protobuf.ByteString;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.thesis.benchmarks.client.exceptions.ReplicasUnknownException;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Client {
    private static final int BASE_CONTROL_PORT = 10000;
    private static final int PAYLOAD_SIZE = 250;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int cooldown;
    private Map<Integer, String> replicas = null;
    private Map<Integer,Loader> loaders;
    private Set<Stat> stats;
    private Map<Integer,Lock> locks;
    private String masterIP;
    private int clientId;

    private static final int collectionDuration = 1000;
    /**
     * Information on ongoing measurments
     */
    private Map<Integer, OnGoingExecution> onGoing;

    /**
     *
     * @param cooldown - time between stop is called and measurements are stopped (in milliseconds)
     */
    public Client(int cooldown, String masterIP) {
        this.cooldown = cooldown;
        stats = new HashSet<>();
        loaders = new HashMap<>();
        onGoing = new HashMap<>();
        locks = new HashMap<>();
        this.masterIP = masterIP;
        this.clientId = ThreadLocalRandom.current().nextInt();
    }

    /**
     *
     * @param replicas - list of the replicas IPs
     */
    public void setReplicas(Map<Integer, String> replicas) {
        this.replicas = replicas;
    }

    public String getMasterIP() {
        return this.masterIP;
    }

    /**
     *
     * @param load load placed on each replica (in transactions per second)
     */
    public void start(int load) throws ReplicasUnknownException {
        if (replicas == null) {
            throw new ReplicasUnknownException();
        }

        for (int id: replicas.keySet()) {
            loaders.put(id, new Loader(id, replicas.get(id), load));
            onGoing.put(id, new OnGoingExecution());
            loaders.get(id).start();
            locks.put(id, new ReentrantLock());
        }
    }

    public void stop() {
        try {
            Thread.sleep(cooldown);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int id: loaders.keySet())  loaders.get(id).halt();
    }

    /**
     * Information on the execution of a replica in a given time period
     */
    private class Stat {

        private int replicaId;
        private long start;
        private long end;
        // average CPU usage
        private double cpu;
        private double networkIn;
        private double networkOut;
        private int load;
        // txs confirmed
        private long txs;
        // average latency
        private double latency;

        public Stat(int replicaId, int load) {
            replicaId = replicaId;
            load = load;
        }

        public void start(long start) {
            this.start = start;
        }

        public void end(long end) {
            this.end = end;
        }

        public void cpu(double cpu) {
            this.cpu = cpu;
        }

        public void networkIn(double networkIn) {
            this.networkIn = networkIn;
        }

        public void networkOut(double networkOut) {
            this.networkOut = networkOut;
        }

        public void txs(long txs) {
            this.txs = txs;
        }

        public void latency(double latency) {
            this.latency = latency;
        }
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

    public int getId() {
        return this.clientId;
    }

    private class Loader extends Thread {
        private int id;
        private String ip;
        private int load;
        private AtomicBoolean done;
        private BenchmarkServiceGrpc.BenchmarkServiceStub stub;

        public Loader(int id, String ip, int load) {
            this.id = id;
            this.ip = ip;
            this.load = load;
            this.done = new AtomicBoolean(false);
            this.stub = BenchmarkServiceGrpc.newStub(
                Grpc.newChannelBuilder(
                    ip + ":" + Integer.toString(BASE_CONTROL_PORT + id), InsecureChannelCredentials.create()
                ).build()
            );
        }

        public void run() {
            RateLimiter rateLimiter = RateLimiter.create(load);
                while (!done.get()) {
                    rateLimiter.acquire();
                    sendTx();
                }
        }

        public void halt() {
            done.set(true);
        }

        public void sendTx() {

            BenchmarkServiceOuterClass.SubmitRequest request = BenchmarkServiceOuterClass.SubmitRequest.newBuilder()
                                                                    .setPayload(ByteString.copyFrom(generateTx()))
                                                                    .build();

            long submitTime = ZonedDateTime.now().toInstant().toEpochMilli();
            stub.submit(request,
                        new StreamObserver<BenchmarkServiceOuterClass.SubmitResponse>() {
                @Override
                public void onNext(BenchmarkServiceOuterClass.SubmitResponse submitResponse) {
                    if (submitResponse.getOk()) {
                        long now = ZonedDateTime.now().toInstant().toEpochMilli();
                        handleSubmission(now - submitTime);
                    } else {
                        handleDropped();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                }
            });
        }

        /**
         *  Produces transaction (that includes submission timestamp)
         */
        public byte[] generateTx() {
            byte[] payload = new byte[PAYLOAD_SIZE];
            (new Random()).nextBytes(payload);
            return payload;
        }


        /**
         * Registers the submission of a transaction
         */
        public void handleSubmission(long latency) {
            logger.info("tx confirmed (latency={})", latency);
            locks.get(id).lock();
            onGoing.get(id).addTransaction(latency);
            locks.get(id).unlock();
        }

        /**
         * Registers the drop of a transaction
         */
        public void handleDropped() {
            logger.info("transaction dropped");
            locks.get(id).lock();
            onGoing.get(id).addDropped();
            locks.get(id).unlock();
        }
    }

    public List<Execution> getStats() {
        Map<Integer, OnGoingExecution> past;

        for (int id: locks.keySet()) locks.get(id).lock();

        past = onGoing;
        onGoing = new HashMap<>();
        for (int id: locks.keySet()) onGoing.put(id, new OnGoingExecution());

        for (int id: locks.keySet()) locks.get(id).unlock();
        long end = ZonedDateTime.now().toInstant().toEpochMilli();

        return past.keySet().stream()
                .map(id -> {
                    OnGoingExecution ex = past.get(id);
                    return Execution.build(id, ex.getStart(), end, ex.getTxs(), ex.getDropped(), ex.getLatency());
                })
                .collect(Collectors.toList());
    }

    private class OnGoingExecution {
        private long start;
        private AtomicLong txs;
        private AtomicLong totalLatency;
        private AtomicLong dropped;

        public OnGoingExecution() {
            start = ZonedDateTime.now().toInstant().toEpochMilli();
            txs = new AtomicLong();
            totalLatency = new AtomicLong();
            dropped = new AtomicLong();
        }

        public void addTransaction(long latency) {
            txs.incrementAndGet();
            totalLatency.addAndGet(latency);
        }

        public void addDropped() {
            dropped.incrementAndGet();
        }

        public long getStart() { return start; }
        public long getTxs() { return txs.get(); }
        public long getDropped() { return dropped.get(); }
        public double getLatency() {
            long t = txs.get();
            if (t == 0) return 0;
            return totalLatency.get()/t;
        }
    }

}
