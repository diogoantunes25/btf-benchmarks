package pt.ulisboa.tecnico.thesis.benchmarks.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.RegisterServiceGrpc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * periodically sends information about client to the master
 */
public class Reporter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int MASTER_PORT = 15000;
    private Client client;
    private String masterIP;

    private AtomicBoolean done = new AtomicBoolean(true);
    private Thread reportDaemon;
    private InformationCollectorServiceGrpc.InformationCollectorServiceBlockingStub stub;

    public Reporter(Client client) {
        this.client = client;
        this.masterIP = client.getMasterIP();
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
        for (Execution ex: client.getStats()) {
            InformationCollectorServiceOuterClass.ClientRequest request =
                    InformationCollectorServiceOuterClass.ClientRequest.newBuilder()
                    .setReplicaId(ex.replicaId)
                    .setTxs(ex.txs)
                    .setStart(ex.start)
                    .setEnd(ex.end)
                    .setLatency(ex.latency)
                    .setSystemInfo(
                            InformationCollectorServiceOuterClass.SystemInfo.newBuilder()
                                    .setCpu(ex.cpu)
                                    .setBandwidthIn(ex.bandwithIn)
                                    .setBandwidthOut(ex.bandwithOut)
                                    .setTotalMemory(ex.totalMemory)
                                    .setFreeMemory(ex.freeMemory)
                                    .build()
                    ).build();
            stub.clientUpdate(request);
        }
        logger.info("sent reports to master");
    }

    public void stop() {
        logger.info("stopped");
        done.set(true);
    }
}
