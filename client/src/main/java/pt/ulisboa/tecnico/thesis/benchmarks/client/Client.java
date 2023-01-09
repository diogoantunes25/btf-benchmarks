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
import pt.ulisboa.tecnico.thesis.benchmarks.contract.TransactionServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.TransactionServiceOuterClass;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private static final int BASE_CONTROL_PORT = 10000;
    private static final int PAYLOAD_SIZE = 250;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int cooldown;
    // replica ip -> replica IP
    private Map<Integer, String> replicas = null;
    private InformationCollector informationCollector;
    private Map<Integer,Loader> loaders;
    private Set<Stat> stats;
    private Map<Integer,Lock> locks;

    private static final int collectionDuration = 1000;
    /**
     * Information on ongoing measurments
     */
    private Map<Integer, OnGoingExecution> onGoing;

    /**
     *
     * @param cooldown - time between stop is called and measurements are stopped (in milliseconds)
     */
    public Client(int cooldown) {
        this.cooldown = cooldown;
        stats = new HashSet<>();
        loaders = new HashMap<>();
        onGoing = new HashMap<>();
        locks = new HashMap<>();
    }

    /**
     *
     * @param replicas - list of the replicas IPs
     */
    public void setReplicas(Map<Integer, String> replicas) {
        this.replicas = replicas;
    }

    /**
     *
     * @param load load placed on each replica (in transactions per second)
     */
    public void start(int load) throws ReplicasUnknownException {
        logger.info("starting with load={}", load);

        if (replicas == null) {
            throw new ReplicasUnknownException();
        }

        informationCollector = new InformationCollector(collectionDuration);
        informationCollector.start();

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
        informationCollector.halt();
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

    private class Loader extends Thread {
        private int id;
        private String ip;
        private int load;
        private AtomicBoolean done;
        private TransactionServiceGrpc.TransactionServiceStub stub;

        public Loader(int id, String ip, int load) {
            this.id = id;
            this.ip = ip;
            this.load = load;
            this.done = new AtomicBoolean(false);
            this.stub = TransactionServiceGrpc.newStub(
                Grpc.newChannelBuilder(
                    ip + ":" + Integer.toString(BASE_CONTROL_PORT + id), InsecureChannelCredentials.create()
                ).build()
            );
            logger.info("start to load {}:{}", ip, BASE_CONTROL_PORT+id);
        }

        public void run() {
            logger.info("starting to load {}", ip);
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

            TransactionServiceOuterClass.SubmitRequest request = TransactionServiceOuterClass.SubmitRequest.newBuilder()
                                                                    .setPayload(ByteString.copyFrom(generateTx()))
                                                                    .build();

            logger.info("submitting to {}", ip);
            stub.submit(request,
                        new StreamObserver<TransactionServiceOuterClass.SubmitResponse>() {
                @Override
                public void onNext(TransactionServiceOuterClass.SubmitResponse submitResponse) {
                    logger.info("new submission");
                    // TODO
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.info("error");
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    logger.info("stream closed");
                    // TODO
                }
            });
        }

        /**
         *  Produces transaction (that includes submission timestamp)
         */
        public byte[] generateTx() {

            byte[] payload = new byte[PAYLOAD_SIZE];

            (new Random()).nextBytes(payload);

            long submitTime = ZonedDateTime.now().toInstant().toEpochMilli();
            // logger.info("The submit time is {}", submitTime);
            byte[] submitTimeBytes = longToBytes(submitTime);
            for (int i = 0; i < submitTimeBytes.length; i++) {
                payload[i] = submitTimeBytes[i];
            }

            return payload;
        }


        /**
         * Registers the submission of a transaction
         */
        public void handleSubmission(int latency) {
            logger.info("tx confirmed (latency={})", latency);
            locks.get(id).lock();
            onGoing.get(id).addTransaction(latency);
            locks.get(id).unlock();
        }

    }

    private class InformationCollector extends Thread {
        private int periodDuration;
        private AtomicBoolean done;

        /**
         * @param periodDuration time between collection (in milliseconds)
         */
        public InformationCollector(int periodDuration) {
            this.periodDuration = periodDuration;
            this.done = new AtomicBoolean(false);
        }

        public void run() {
            while (!done.get()) {
                try {
                    Thread.sleep(periodDuration);
                } catch (InterruptedException e) {
                    logger.info("collector stopped");
                    return;
                }
                collect();
            }
        }

        public void halt() {
            done.set(true);
        }

        public void collect() {
            logger.info("collecting info");
            Map<Integer, OnGoingExecution> past;

            for (int id: locks.keySet()) locks.get(id).lock();

            past = onGoing;
            onGoing = new HashMap<>();
            for (int id: locks.keySet()) onGoing.put(id, new OnGoingExecution());

            for (int id: locks.keySet()) locks.get(id).unlock();

            for (int id: past.keySet()) {
                OnGoingExecution ex = past.get(id);
                logger.info("{} has {} new txs, latency is {}", id, ex.getTxs(), ex.getLatency());
                // TODO: Save stats
            }

        }


    }

    private class OnGoingExecution {
        private long start;
        private AtomicLong txs;
        private AtomicLong totalLatency;

        public OnGoingExecution() {
            start = ZonedDateTime.now().toInstant().toEpochMilli();
            txs = new AtomicLong();
            totalLatency = new AtomicLong();
        }

        public void addTransaction(int latency) {
            txs.incrementAndGet();
            totalLatency.addAndGet(latency);
        }

        public long getStart() { return start; }
        public long getTxs() { return txs.get(); }
        public double getLatency() {
            long t = txs.get();
            if (t == 0) return 0;
            return totalLatency.get()/t;
        }
    }

}
