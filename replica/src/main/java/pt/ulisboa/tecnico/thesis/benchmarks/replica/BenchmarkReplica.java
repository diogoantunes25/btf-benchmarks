package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.*;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.acs.AcsAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.alea.Alea;
import pt.tecnico.ulisboa.hbbft.abc.dumbo.Dumbo;
import pt.tecnico.ulisboa.hbbft.abc.honeybadger.HoneyBadger;
import pt.tecnico.ulisboa.hbbft.abc.honeybadger.Params;
import pt.tecnico.ulisboa.hbbft.abc.honeybadger.crypto.EncryptionSchedule;
import pt.tecnico.ulisboa.hbbft.abc.honeybadger.crypto.NeverEncrypt;
import pt.tecnico.ulisboa.hbbft.binaryagreement.BinaryAgreementFactory;
import pt.tecnico.ulisboa.hbbft.binaryagreement.BinaryAgreementMessage;
import pt.tecnico.ulisboa.hbbft.binaryagreement.moustefaoui.MoustefaouiBinaryAgreementFactory;
import pt.tecnico.ulisboa.hbbft.broadcast.BroadcastFactory;
import pt.tecnico.ulisboa.hbbft.broadcast.bracha.BrachaBroadcastFactory;
import pt.tecnico.ulisboa.hbbft.example.abc.acs.AcsAtomicBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.alea.AleaMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.byzness.EchoVBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.dumbo.DumboMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.honeybadger.HoneyBadgerMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.agreement.mvba.MvbaMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.agreement.vba.VbaMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.binaryagreement.moustefaoui.MoustefaouiBinaryAgreementMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.broadcast.bracha.BrachaBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.election.CommitteeElectionMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.subset.dumbo.DumboSubsetMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.subset.hbbft.HoneyBadgerSubsetMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.vbroadcast.prbc.PrbcMessageEncoder;
import pt.tecnico.ulisboa.hbbft.subset.SubsetFactory;
import pt.tecnico.ulisboa.hbbft.subset.SubsetMessage;
import pt.tecnico.ulisboa.hbbft.subset.dumbo.DumboSubsetFactory;
import pt.tecnico.ulisboa.hbbft.subset.hbbft.HoneyBadgerSubsetFactory;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.BenchmarkResults;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BenchmarkReplica {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Integer replicaId;

    private NetworkInfo networkInfo;
    private TcpTransport transport;

    private MessageEncoder<String> encoder;
    // private AtomicReference<IAtomicBroadcast> protocol = new AtomicReference<>();
    private IAtomicBroadcast protocol;

    private final Lock benchmarkLock = new ReentrantLock();
    private final Condition benchmarkComplete = benchmarkLock.newCondition();
    private BenchmarkResults benchmarkResults;

    public BenchmarkReplica(Integer replicaId) {
        this.replicaId = replicaId;
    }

    public boolean setTopology(List<Replica> replicas, GroupKey groupKey, KeyShare keyShare, int tolerance) {
        logger.info("setTopology - replicas:{}, N:{}, f:{}",
                replicas.stream().map(r -> String.valueOf(r.getId())).sorted().collect(Collectors.joining(", ","[","]")),
                replicas.size(), tolerance);

        // ignore message if not in new topology
        Replica me = replicas.stream().filter(replica -> replica.getId() == replicaId).findAny().orElse(null);
        if (me == null) return false;

        // close all connections
        if (this.transport != null)
            for (Connection connection: this.transport.getConnections()) connection.disconnect();

        // set network info
        NetworkInfo.ValidatorSet validators = new NetworkInfo.ValidatorSet(
                replicas.stream().map(Replica::getId).collect(Collectors.toSet()), tolerance);
        this.networkInfo = new NetworkInfo(replicaId, validators, groupKey, keyShare);

        // init transport layer
        this.transport = new TcpTransport(me, replicas, 2);

        return true;
    }

    public boolean setProtocol(Protocol protocol, Integer batchSize) {
        logger.info("setProtocol - protocol:{}, batchSize:{}", protocol, batchSize);

        // must specify topology before setting a protocol
        if (networkInfo == null || transport == null) {
            logger.info("setProtocol - Failed (Unknown topology).");
            return false;
        }

        // stop listeners
        logger.info("------------------------------------------------------------------------");
        logger.info("Stopping message listeners");
        logger.info("------------------------------------------------------------------------");
        int cDone = 0;
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);

            cDone++;
            logger.info("[{}/{}] listeners stopped", cDone, this.transport.getConnections().size());
        }
        logger.info("Success.");
        logger.info("------------------------------------------------------------------------");

        logger.info("------------------------------------------------------------------------");
        logger.info("Initializing benchmark protocol");
        logger.info("------------------------------------------------------------------------");
        if (protocol == Protocol.ALEA_BFT) {
            // initialize ALEA_BFT encoders
            this.encoder = new AleaMessageEncoder(
                    new EchoVBroadcastMessageEncoder(),
                    new MoustefaouiBinaryAgreementMessageEncoder()
            );

            // config params
            Alea.Params aleaParams = new Alea.Params.Builder()
                    .maxConcurrentBroadcasts(1)
                    .maxPipelineOffset(1)
                    .batchSize(batchSize).benchmark(true).build();

            logger.info("protocol.name: {}", protocol);
            logger.info("protocol.params.batch_size: {}", aleaParams.getBatchSize());
            logger.info("protocol.params.concurrent_broadcasts: {}", aleaParams.getMaxConcurrentBroadcasts());
            logger.info("protocol.params.pipeline_offset: {}", aleaParams.getMaxPipelineOffset());
            logger.info("protocol.params.benchmark: {}", aleaParams.isBenchmark());

            // setup protocol
            this.protocol = new Alea(replicaId, networkInfo, aleaParams);

        } else {
            MessageEncoder<String> acsEncoder;
            SubsetFactory acsFactory;
            switch (protocol) {
                case HONEY_BADGER: {
                    acsEncoder = new HoneyBadgerSubsetMessageEncoder(
                            new BrachaBroadcastMessageEncoder(),
                            new MoustefaouiBinaryAgreementMessageEncoder()
                    );
                    acsFactory = new HoneyBadgerSubsetFactory(
                            replicaId,
                            networkInfo,
                            new BrachaBroadcastFactory(replicaId, networkInfo),
                            new MoustefaouiBinaryAgreementFactory(replicaId, networkInfo)
                    ); break;
                }
                case DUMBO: {
                    acsEncoder = new DumboSubsetMessageEncoder(
                            new BrachaBroadcastMessageEncoder(),
                            new MoustefaouiBinaryAgreementMessageEncoder(),
                            new CommitteeElectionMessageEncoder()
                    );
                    acsFactory = new DumboSubsetFactory(
                            replicaId,
                            networkInfo,
                            new BrachaBroadcastFactory(replicaId, networkInfo),
                            new MoustefaouiBinaryAgreementFactory(replicaId, networkInfo)
                    );
                    break;
                }
                default: {
                    logger.info("Failed (Unknown protocol:{}).", protocol);
                    logger.info("------------------------------------------------------------------------");
                    return false;
                }
            }
            logger.info("protocol.name: {}", protocol);
            logger.info("protocol.params.batch_size: {}", batchSize);
            logger.info("protocol.params.encryption: {}", NeverEncrypt.class);
            logger.info("protocol.params.committee_size: {}", networkInfo.getF()+1);
            logger.info("protocol.params.benchmark: {}", true);
            logger.info("protocol.params.payload_size: {}", 250);
            logger.info("protocol.params.benchmark: {}", true);

            this.encoder = new AcsAtomicBroadcastMessageEncoder(acsEncoder);
            AcsAtomicBroadcast.Params params = new AcsAtomicBroadcast.Params.Builder()
                    .batchSize(batchSize)
                    .encryptionSchedule(new NeverEncrypt()) // TODO always encrypt
                    .committeeSize(networkInfo.getF()+1)
                    .benchmark(true)
                    .maxPayloadSize(250)
                    .build();
            this.protocol = new AcsAtomicBroadcast(replicaId, networkInfo, params, acsFactory);
        }

        // start listeners
        logger.info("setProtocol - Staring message listeners.");
        // FIXME start listeners
        //for (Connection connection: this.transport.getConnections())
        //    connection.setListener(this);

        logger.info("Success.");
        logger.info("------------------------------------------------------------------------");
        return true;
    }

    public BenchmarkResults runBenchmark(Integer numRequests, Integer payloadSize) {
        logger.info("runBenchmark - numRequests:{}, payloadSize:{}", numRequests, payloadSize);

        if (networkInfo == null || transport == null || encoder == null || protocol == null) {
            logger.info("runBenchmark - Failed (Unknown topology/protocol).");
            return new BenchmarkResults(0L, 0L);
        }

        // pre generate payload list
        Random rd = new Random();
        List<byte[]> payloads = new ArrayList<>();
        for (int i=0; i<numRequests; i++) {
            byte[] payload = new byte[payloadSize];
            rd.nextBytes(payload);
            payloads.add(payload);
        }

        benchmarkLock.lock();
        long startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        benchmarkResults = new BenchmarkResults(startTime, (long) (numRequests*networkInfo.getN()*0.95));
        benchmarkLock.unlock();

        Step<Block> step = this.protocol.handleInput(payloads.get(0));
        for (TargetedMessage message: step.getMessages()) {
            String encoded = this.encoder.encode(message.getContent());
            for (Integer target: message.getTargets()) {
                this.transport.sendToReplica(target, encoded);
            }
        }


        final long floodFinish = ZonedDateTime.now().toInstant().toEpochMilli();
        logger.info("runBenchmark - Flood finished at {}.", Math.floorDiv(floodFinish-startTime, 1000));

        // wait for completion
        benchmarkLock.lock();
        benchmarkComplete.awaitUninterruptibly();

        BenchmarkResults results = new BenchmarkResults(this.benchmarkResults);
        this.benchmarkResults = null;

        benchmarkLock.unlock();

        final long interval = results.getEndTime() - results.getStartTime();
        logger.info("runBenchmark - Throughput: {} tx/s", ((double) results.getTargetTxs()/interval)*1000);
        logger.info("runBenchmark - Done ({} ms).", interval);

        return results;
    }

    public void handleMessage(String data) {
        ProtocolMessage message = this.encoder.decode(data);
        if (message != null) {
            Step<Block> step = this.protocol.handleMessage(message);
            this.handleStep(step);
        }
    }

    private void handleStep(Step<Block> step) {
        // Send messages generated during this step
        for (TargetedMessage message: step.getMessages()) {
            String encoded = this.encoder.encode(message.getContent());
            // logger.info("{} -> {}", message.getTarget(), encoded);
            final int cid = (protocol instanceof Alea && message.getContent() instanceof BinaryAgreementMessage) ? 1 : 0;
            for (Integer target: message.getTargets()) {
                this.transport.sendToReplica(target, cid, encoded);
            }
        }

        Collection<byte[]> entries = step.getOutput().stream()
                .map(Block::getEntries).collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
        if (!entries.isEmpty()) {
            // logger.info("runBenchmark - Step with blocks {} and {} transactions.", step.getOutput().stream().map(r -> String.valueOf(r.getNumber())).sorted().collect(Collectors.joining(", ","[","]")), entries.size());
            benchmarkLock.lock();
            if (benchmarkResults != null) {
                Long last = benchmarkResults.getLastMeasurementTime();
                Long now = ZonedDateTime.now().toInstant().toEpochMilli();
                Long throughput = (entries.size()*1000L) / Math.max(now - last, 1L);
                //logger.info("runBenchmark - last:{}, current:{}, offset:{}, throughput:{}", last, now, (now-last), throughput);
                //logger.info("runBenchmark - {}", (now-last));
                //System.exit(1);
                benchmarkResults.logDeliveryEvent(entries, ZonedDateTime.now().toInstant().toEpochMilli());
                if (benchmarkResults.isComplete()) benchmarkComplete.signal();
            }
            benchmarkLock.unlock();
        }
    }
}
