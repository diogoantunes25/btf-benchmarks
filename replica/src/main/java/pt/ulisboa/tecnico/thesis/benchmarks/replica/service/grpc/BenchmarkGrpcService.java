package pt.ulisboa.tecnico.thesis.benchmarks.replica.service.grpc;

import io.grpc.stub.StreamObserver;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.Dealer;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.BenchmarkMode;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.BenchmarkReplica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Fault;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.*;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.service.local.BenchmarkService;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchmarkGrpcService extends BenchmarkServiceGrpc.BenchmarkServiceImplBase {

    private final Integer replicaId;
    private final BenchmarkService benchmarkService;

    private final int port;

    // TODO deprecated
    // private final BenchmarkReplica benchmarkReplica;


    public BenchmarkGrpcService(Integer replicaId, int port) {
        this.replicaId = replicaId;
        this.benchmarkService = new BenchmarkService(replicaId);
        this.port = port;

        // TODO deprecated
        //this.benchmarkReplica = new BenchmarkReplica(replicaId);
    }

    @Override
    public void topology(
            BenchmarkServiceOuterClass.TopologyRequest request,
            StreamObserver<BenchmarkServiceOuterClass.TopologyResponse> responseObserver
    ) {
        

        List<Replica> replicas = request.getReplicasList().stream().map(Replica::new).collect(Collectors.toList());

        int k = request.getGk().getK();
        int l = request.getGk().getL();
        BigInteger e = new BigInteger(request.getGk().getE());
        BigInteger n = new BigInteger(request.getGk().getN());
        GroupKey groupKey = new GroupKey(k, l, e, n);

        BigInteger secret = new BigInteger(request.getShare());
        KeyShare keyShare = new KeyShare(replicaId+1, secret, n, Dealer.factorial(l));
        Dealer.generateVerifiers(n, new KeyShare[]{keyShare});

        boolean result = benchmarkService.setTopology(replicas, groupKey, keyShare, request.getF());

        // send topology response
        BenchmarkServiceOuterClass.TopologyResponse response = BenchmarkServiceOuterClass.TopologyResponse
                .newBuilder().setOk(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void protocol(
            BenchmarkServiceOuterClass.ProtocolRequest request,
            StreamObserver<BenchmarkServiceOuterClass.ProtocolResponse> responseObserver
    ) {
        Protocol protocol;
        switch (request.getProtocol()) {
            case HONEY_BADGER: protocol = Protocol.HONEY_BADGER; break;
            case DUMBO: protocol = Protocol.DUMBO; break;
            case DUMBO_2: protocol = Protocol.DUMBO; break;
            case ALEA_BFT: protocol = Protocol.ALEA_BFT; break;
            default: {
                protocol = Protocol.HONEY_BADGER;
            }
        }
        int batchSize = request.getBatchSize();

        BenchmarkMode mode;
        switch (request.getBenchmark()) {
            case LATENCY: mode = BenchmarkMode.LATENCY; break;
            case THROUGHPUT:
            default: {
                mode = BenchmarkMode.THROUGHPUT;
            }
        }

        Fault fault;
        switch (request.getFault()) {
            case CRASH: fault = Fault.CRASH; break;
            case BYZANTINE: fault = Fault.BYZANTINE; break;
            case FREE:
            default: {
                fault = Fault.FREE;
            }
        }
        List<Integer> faulty = request.getFaultyList();

        int load = request.getLoad();

        boolean result = benchmarkService.setProtocol(protocol, batchSize, mode, fault, faulty, load);
        // TODO deprecated boolean result = this.benchmarkReplica.setProtocol(protocol, batchSize);

        // send protocol response
        BenchmarkServiceOuterClass.ProtocolResponse response = BenchmarkServiceOuterClass.ProtocolResponse
                .newBuilder().setOk(result).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void start(
            BenchmarkServiceOuterClass.StartBenchmarkRequest request,
            StreamObserver<BenchmarkServiceOuterClass.StartBenchmarkResponse> responseObserver
    ) {
        boolean result = benchmarkService.start(request.getFirst());

        BenchmarkServiceOuterClass.StartBenchmarkResponse response = BenchmarkServiceOuterClass.StartBenchmarkResponse
                .newBuilder()
                .setOk(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void stop(
            BenchmarkServiceOuterClass.StopBenchmarkRequest request,
            StreamObserver<BenchmarkServiceOuterClass.StopBenchmarkResponse> responseObserver
    ) {
        Benchmark benchmark = benchmarkService.stop();

        BenchmarkServiceOuterClass.StopBenchmarkResponse response = BenchmarkServiceOuterClass.StopBenchmarkResponse
                .newBuilder()
                .setReplica(replicaId)
                .setStart(benchmark.getStartTime())
                .setFinish(benchmark.getFinishTime())
                .setSentMessages(benchmark.getSentMessageCount())
                .setRecvMessages(benchmark.getRecvMessageCount())
                .addAllExecutions(benchmark.getExecutions().stream().map(
                        e -> BenchmarkServiceOuterClass.StopBenchmarkResponse.Execution.newBuilder()
                                .setStart(e.getStart())
                                .setFinish(e.getFinish())
                        .build()
                ).collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void inform(
            BenchmarkServiceOuterClass.InformRequest request,
            StreamObserver<BenchmarkServiceOuterClass.InformResponse> responseObserver
    ) {

        Summary info = benchmarkService.inform();

        BenchmarkServiceOuterClass.InformResponse response = BenchmarkServiceOuterClass.InformResponse
                .newBuilder()
                .setReplica(this.replicaId)
                .setStart(info.getStart())
                .setFinish(info.getFinish())
                .setTxCommitted(info.getTxCommitted())
                .setAvgLatency(info.getAvgLatency())
                .build();

        System.out.println("I was asked to inform master of progress");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void shutdown(
            BenchmarkServiceOuterClass.ShutdownRequest request,
            StreamObserver<BenchmarkServiceOuterClass.ShutdownResponse> responseObserver
    ) {
        // get shutdown timer
        int shutdownTimer = request.getTimer();

        // send shutdown response
        BenchmarkServiceOuterClass.ShutdownResponse response = BenchmarkServiceOuterClass.ShutdownResponse.newBuilder()
                .getDefaultInstanceForType();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        // schedule shutdown
        Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> System.exit(0), shutdownTimer, TimeUnit.MILLISECONDS);
    }
}
