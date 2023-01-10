package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceOuterClass;

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

    public Reporter(String masterIP, int replicaID) {
        this.masterIP = masterIP;
        this.replicaID = replicaID;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(masterIP, MASTER_PORT)
                .usePlaintext()
                .build();

        stub = InformationCollectorServiceGrpc.newBlockingStub(channel);
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
                        .build();

        stub.replicaUpdate(request);

        logger.info("sent report to master");
    }

    public void stop() {
        logger.info("stopped");
        done.set(true);
    }
}