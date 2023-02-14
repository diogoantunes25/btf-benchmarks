package pt.ulisboa.tecnico.thesis.benchmarks.replica.service.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.NetworkInfo;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.acs.AcsAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.alea.Alea;
import pt.tecnico.ulisboa.hbbft.abc.honeybadger.crypto.NeverEncrypt;
import pt.tecnico.ulisboa.hbbft.binaryagreement.moustefaoui.MoustefaouiBinaryAgreementFactory;
import pt.tecnico.ulisboa.hbbft.broadcast.bracha.BrachaBroadcastFactory;
import pt.tecnico.ulisboa.hbbft.example.abc.acs.AcsAtomicBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.alea.AleaMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.abc.byzness.EchoVBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.binaryagreement.moustefaoui.MoustefaouiBinaryAgreementMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.broadcast.bracha.BrachaBroadcastMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.election.CommitteeElectionMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.subset.dumbo.DumboSubsetMessageEncoder;
import pt.tecnico.ulisboa.hbbft.example.subset.hbbft.HoneyBadgerSubsetMessageEncoder;
import pt.tecnico.ulisboa.hbbft.subset.SubsetFactory;
import pt.tecnico.ulisboa.hbbft.subset.dumbo.Dumbo2SubsetFactory;
import pt.tecnico.ulisboa.hbbft.subset.dumbo.DumboSubsetFactory;
import pt.tecnico.ulisboa.hbbft.subset.hbbft.HoneyBadgerSubsetFactory;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.BenchmarkMode;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Fault;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Reporter;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Confirmation;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.replica.BenchmarkReplica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Integer replicaId;

    private NetworkInfo networkInfo;
    private TcpTransport transport;
    private BenchmarkReplica benchmarkReplica; // replica.replica.BenchmarkReplica
    private Reporter reporter;

    private int port;

    public BenchmarkService(Integer replicaId, int port, Reporter reporter) {
        System.out.println("The replica id is " + replicaId);
        this.replicaId = replicaId;
        this.port = port;
        this.reporter = reporter;
    }

    public boolean setTopology(List<Replica> replicas, GroupKey groupKey, KeyShare keyShare, int tolerance) {
        logger.info("setTopology - replicas:{}, N:{}, f:{}",
                replicas.stream().map(r -> String.valueOf(r.getId())).sorted().collect(Collectors.joining(", ","[","]")),
                replicas.size(), tolerance);

        logger.info("------------------------------------------------------------------------");
        logger.info("Setting network topology");
        logger.info("------------------------------------------------------------------------");

        // ignore if not in topology
        Replica me = replicas.stream().filter(replica -> replica.getId() == replicaId).findAny().orElse(null);
        if (me == null) {
            System.out.println("Aborting topology set on replica " + replicaId);
            System.out.println("The replicas involved are:");
            for (Replica participant: replicas) {
                System.out.println("  -> " + participant.getId());
            }
            return false;
        }

        // close all connections
        if (this.transport != null)
            for (Connection connection: this.transport.getConnections()) connection.disconnect();

        // set network info
        NetworkInfo.ValidatorSet validators = new NetworkInfo.ValidatorSet(
                replicas.stream().map(Replica::getId).collect(Collectors.toSet()), tolerance);

        this.networkInfo = new NetworkInfo(replicaId, validators, groupKey, keyShare);

        // init transport layer
        // FIXME 2 channels?
        logger.info("Creating transport, port is {}", port);
        this.transport = new TcpTransport(me, replicas, 2, port);

        logger.info("Connection status:");
        for (Connection connection: this.transport.getConnections()) {
            connection.printStatus(logger);
        }

        logger.info("Success.");
        logger.info("------------------------------------------------------------------------\n");
        return true;    // success
    }

    public boolean setProtocol(Protocol protocol, Integer batchSize, BenchmarkMode mode, Fault fault, List<Integer> faulty, int load) {
        logger.info("setProtocol - protocol:{}, batchSize:{}", protocol, batchSize);

        // ignore if topology not set
        if (networkInfo == null || transport == null) {
            logger.info("setProtocol - Failed (Unknown topology).");
            return false;   // failure
        }

        logger.info("------------------------------------------------------------------------");
        logger.info("Initializing benchmark protocol");
        logger.info("------------------------------------------------------------------------");

        MessageEncoder<String> encoder = this.getEncoder(protocol);
        IAtomicBroadcast instance = this.getInstance(protocol, batchSize, mode, fault, faulty);

        this.benchmarkReplica = new BenchmarkReplica(instance, encoder, transport, load, batchSize);
        reporter.register(this.benchmarkReplica);
        logger.info("Success.");
        logger.info("------------------------------------------------------------------------\n");
        return true;    // success
    }

    public boolean start(boolean first) {
        logger.info("------------------------------------------------------------------------");
        logger.info("Starting benchmark (first: {}).", first);
        logger.info("------------------------------------------------------------------------");
        this.benchmarkReplica.start(first);
        logger.info("Success.");
        logger.info("------------------------------------------------------------------------\n");
        return true;
    }

    public void stop() {
        logger.info("------------------------------------------------------------------------");
        logger.info("Stopping benchmark.");
        logger.info("------------------------------------------------------------------------");
        this.benchmarkReplica.stop();
        logger.info("Success.");
        logger.info("------------------------------------------------------------------------\n");
    }

    /**
     *
     * @param payload Payload to input to protocol
     * @param confirmation Callback when state machine confirms submission
     */
    public void submit(byte[] payload, Confirmation confirmation) {
        this.benchmarkReplica.submit(payload, confirmation);
    }

    private MessageEncoder<String> getEncoder(Protocol protocol) {
        // our protocol
        if (protocol == Protocol.ALEA_BFT) {
            return new AleaMessageEncoder(
                    new EchoVBroadcastMessageEncoder(),
                    new MoustefaouiBinaryAgreementMessageEncoder()
            );
        }

        // ACS based protocols
        else {
            MessageEncoder<String> acsEncoder;
            switch (protocol) {
                case HONEY_BADGER:
                    acsEncoder =  new HoneyBadgerSubsetMessageEncoder(
                            new BrachaBroadcastMessageEncoder(),
                            new MoustefaouiBinaryAgreementMessageEncoder()
                    ); break;

                default:
                case DUMBO:
                    acsEncoder = new DumboSubsetMessageEncoder(
                            new BrachaBroadcastMessageEncoder(),
                            new MoustefaouiBinaryAgreementMessageEncoder(),
                            new CommitteeElectionMessageEncoder()
                    ); break;
            }
            return new AcsAtomicBroadcastMessageEncoder(acsEncoder);
        }
    }

    private IAtomicBroadcast getInstance(
            Protocol protocol, Integer batchSize, BenchmarkMode mode, Fault fault, List<Integer> faulty) {
        // our protocol
        if (protocol == Protocol.ALEA_BFT) {
            Map<Fault, Alea.Params.Fault> faultMap = Map.of(
                    Fault.FREE,  Alea.Params.Fault.FREE,
                    Fault.CRASH,  Alea.Params.Fault.CRASH,
                    Fault.BYZANTINE,  Alea.Params.Fault.BYZANTINE
            );

            Map<Integer, Alea.Params.Fault> faults = new HashMap<>();
            for (Integer faultyReplica: faulty)
                faults.put(faultyReplica, faultMap.getOrDefault(fault, Alea.Params.Fault.FREE));

            Alea.Params aleaParams = new Alea.Params.Builder()
                    .batchSize(batchSize)
                    .benchmark(true)
                    .faults(faults)
                    .build();

            logger.info("protocol.name: {}", protocol);
            logger.info("protocol.params.batch_size: {}", aleaParams.getBatchSize());
            logger.info("protocol.params.concurrent_broadcasts: {}", aleaParams.getMaxConcurrentBroadcasts());
            logger.info("protocol.params.pipeline_offset: {}", aleaParams.getMaxPipelineOffset());
            logger.info("protocol.params.benchmark: {}", aleaParams.isBenchmark());
            logger.info("protocol.params.payload_size: {}", aleaParams.getMaxPayloadSize());
            logger.info("protocol.params.faults: {}", faults);

            return new Alea(replicaId, networkInfo, aleaParams);
        }

        // ACS based protocols
        else {
            SubsetFactory acsFactory;
            switch (protocol) {
                case HONEY_BADGER:
                    acsFactory = new HoneyBadgerSubsetFactory(
                            replicaId,
                            networkInfo,
                            new BrachaBroadcastFactory(replicaId, networkInfo),
                            new MoustefaouiBinaryAgreementFactory(replicaId, networkInfo)
                    ); break;

                case DUMBO:
                    acsFactory = new DumboSubsetFactory(
                            replicaId,
                            networkInfo,
                            new BrachaBroadcastFactory(replicaId, networkInfo),
                            new MoustefaouiBinaryAgreementFactory(replicaId, networkInfo)
                    ); break;
                case DUMBO_2:
                default:
                    acsFactory = new Dumbo2SubsetFactory(
                            replicaId,
                            networkInfo
                    ); break;
            }

            Map<Fault, AcsAtomicBroadcast.Params.Fault> faultMap = Map.of(
                    Fault.FREE,  AcsAtomicBroadcast.Params.Fault.FREE,
                    Fault.CRASH,  AcsAtomicBroadcast.Params.Fault.CRASH,
                    Fault.BYZANTINE,  AcsAtomicBroadcast.Params.Fault.BYZANTINE
            );

            Map<Integer, AcsAtomicBroadcast.Params.Fault> faults = new HashMap<>();
            for (Integer faultyReplica: faulty)
                faults.put(faultyReplica, faultMap.getOrDefault(fault, AcsAtomicBroadcast.Params.Fault.FREE));

            AcsAtomicBroadcast.Params params = new AcsAtomicBroadcast.Params.Builder()
                    .batchSize(batchSize)
                    .encryptionSchedule(new NeverEncrypt()) // TODO always encrypt
                    .committeeSize(networkInfo.getF()+1)
                    .benchmark(mode == BenchmarkMode.THROUGHPUT)
                    .maxPayloadSize(250)
                    .faults(faults)
                    .build();

            logger.info("protocol.name: {}", protocol);
            logger.info("protocol.params.batch_size: {}", params.getBatchSize());
            logger.info("protocol.params.encryption: {}", params.getEncryptionSchedule().getClass());
            logger.info("protocol.params.committee_size: {}", params.getK());
            logger.info("protocol.params.benchmark: {}", params.isBenchmark());
            logger.info("protocol.params.payload_size: {}", params.getMaxPayloadSize());
            logger.info("protocol.params.faults: {}", faults);

            return new AcsAtomicBroadcast(replicaId, networkInfo, params, acsFactory);
        }
    }
}
