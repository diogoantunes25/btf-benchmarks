package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.replica.BenchmarkReplica;

import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * periodically sends information about client to the master
 */
public class Reporter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int MASTER_PORT = 15000;
    private String masterIP;
    private AtomicBoolean done = new AtomicBoolean(true);
    private Thread reportDaemon;
    private InformationCollectorServiceGrpc.InformationCollectorServiceBlockingStub stub;
    private int replicaID;
    private BenchmarkReplica replica;

    public Reporter(String masterIP, int replicaID) {
        this.masterIP = masterIP;
        this.replicaID = replicaID;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(masterIP, MASTER_PORT)
                .usePlaintext()
                .build();

        stub = InformationCollectorServiceGrpc.newBlockingStub(channel);
        replica = null;
    }

    /**
     *
     * @param waitTime time between reporting
     */
    public void start(int waitTime) {
        logger.info("started");
        done.set(false);
        reportDaemon = new Thread(() -> {
            while (!done.get()) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendReport();
            }
        });
        reportDaemon.start();
    }

    public void sendReport() {
        SystemReport systemReport = new SystemReport();

        InformationCollectorServiceOuterClass.ReplicaRequest request =
                InformationCollectorServiceOuterClass.ReplicaRequest.newBuilder()
                        .setReplicaId(replicaID)
                        .setSystemInfo(
                                InformationCollectorServiceOuterClass.SystemInfo.newBuilder()
                                        .setCpu(systemReport.getCpu())
                                        .setBandwidthIn(systemReport.getBandwidthIn())
                                        .setBandwidthOut(systemReport.getBandwidthOut())
                                        .setFreeMemory(systemReport.getFreeMemory())
                                        .setTotalMemory(systemReport.getTotalMemory())
                                        .build()
                        )
                        .setReceived(received())
                        .setConfirmed(confirmed())
                        .setDropped(dropped())
                        .setBufferOccupancy(bufferOccupancy())
                        .setTime(ZonedDateTime.now().toInstant().toEpochMilli())
                        .build();

        stub.replicaUpdate(request);
    }

    private long received() {
        if (replica == null) {
            logger.info("txs received defaulted to 0 - no replica registered yet");
            return 0;
        }
        return replica.getReceivedAndReset();
    }

    private long confirmed() {
        if (replica == null) {
            logger.info("txs confirmed defaulted to 0 - no replica registered yet");
            return 0;
        }
        return replica.getConfirmedAndReset();
    }

    private long dropped() {
        if (replica == null) {
            logger.info("txs dropped defaulted to 0 - no replica registered yet");
            return 0;
        }
        return replica.getDroppedAndReset();
    }

    private double bufferOccupancy() {
        if (replica == null) {
            logger.info("buffer occupancy defaulted to 0 - no replica registered yet");
            return 0;
        }
        return replica.getBufferOccupancy();
    }

    public void register(BenchmarkReplica replica) {
        logger.info("replica registered");
        this.replica = replica;
    }

    public void stop() {
        this.replica = null;
        logger.info("stopped");
        done.set(true);
    }
}