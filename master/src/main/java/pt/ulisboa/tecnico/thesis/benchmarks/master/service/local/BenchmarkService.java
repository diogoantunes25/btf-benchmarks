package pt.ulisboa.tecnico.thesis.benchmarks.master.service.local;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.BenchmarkResult;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.BenchmarkRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ReplicaRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.JsonService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchmarkService {

    private final ReplicaRepository replicaRepository;
    private final BenchmarkRepository benchmarkRepository;

    private Benchmark.Topology topology;
    private Benchmark.Protocol protocol;

    public BenchmarkService(ReplicaRepository replicaRepository, BenchmarkRepository benchmarkRepository) {
        this.replicaRepository = replicaRepository;
        this.benchmarkRepository = benchmarkRepository;
    }

    public void setTopology(Benchmark.Topology topology) {
        final CountDownLatch responseLatch = new CountDownLatch(topology.getN());
        StreamObserver<BenchmarkServiceOuterClass.TopologyResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.TopologyResponse topologyResponse) {
                System.out.println(topologyResponse.getOk());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
                throwable.printStackTrace();
                responseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                responseLatch.countDown();
            }
        };

        for (Replica replica: topology.getReplicas()) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);

            BenchmarkServiceOuterClass.TopologyRequest request = BenchmarkServiceOuterClass.TopologyRequest.newBuilder()
                    .setN(topology.getN())
                    .setF(topology.getF())
                    .addAllReplicas(topology.getReplicas().stream()
                            .map(r -> BenchmarkServiceOuterClass.TopologyRequest.Replica
                                    .newBuilder()
                                    .setId(r.getReplicaId())
                                    .setAddr(r.getAddress())
                                    .setPort(r.getPort())
                                    .build()
                            ).collect(Collectors.toList()))
                    .setGk(BenchmarkServiceOuterClass.TopologyRequest.GroupKey.newBuilder()
                            .setK(topology.getGroupKey().getK())
                            .setL(topology.getGroupKey().getL())
                            .setE(topology.getGroupKey().getExponent().toString())
                            .setN(topology.getGroupKey().getModulus().toString())
                            .build()
                    )
                    .setShare(topology.getKeyShares().get(replica.getReplicaId()).getSecret().toString())
                    .buildPartial();

            stub.topology(request, responseObserver);
        }

        try {
            System.out.println("Waiting for replies...");
            responseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.topology = topology;
    }

    public void setProtocol(Benchmark.Protocol protocol) {
        Map<String, BenchmarkServiceOuterClass.ProtocolRequest.Protocol> protocolMap = new HashMap<>();
        protocolMap.put("hb", BenchmarkServiceOuterClass.ProtocolRequest.Protocol.HONEY_BADGER);
        protocolMap.put("dumbo", BenchmarkServiceOuterClass.ProtocolRequest.Protocol.DUMBO);
        protocolMap.put("dumbo2", BenchmarkServiceOuterClass.ProtocolRequest.Protocol.DUMBO_2);
        protocolMap.put("alea", BenchmarkServiceOuterClass.ProtocolRequest.Protocol.ALEA_BFT);
        BenchmarkServiceOuterClass.ProtocolRequest.Protocol grpcProtocol = protocolMap
                .getOrDefault(protocol.getName(), null);
        if (grpcProtocol == null) {
            System.out.println("Unknown protocol.");
            return;
        }

        Map<String, BenchmarkServiceOuterClass.ProtocolRequest.Benchmark> benchmarkMap = Map.of(
                "throughput", BenchmarkServiceOuterClass.ProtocolRequest.Benchmark.THROUGHPUT,
                "latency", BenchmarkServiceOuterClass.ProtocolRequest.Benchmark.LATENCY
        );
        BenchmarkServiceOuterClass.ProtocolRequest.Benchmark benchmarkMode = benchmarkMap
                .getOrDefault(protocol.getBenchmarkMode(), null);
        if (benchmarkMode == null) {
            System.out.println("Unknown benchmark mode.");
            return;
        }

        Map<String, BenchmarkServiceOuterClass.ProtocolRequest.Fault> faultMap = Map.of(
                "free", BenchmarkServiceOuterClass.ProtocolRequest.Fault.FREE,
                "crash", BenchmarkServiceOuterClass.ProtocolRequest.Fault.CRASH,
                "byzantine", BenchmarkServiceOuterClass.ProtocolRequest.Fault.BYZANTINE
        );
        BenchmarkServiceOuterClass.ProtocolRequest.Fault faultMode = faultMap
                .getOrDefault(protocol.getFaultMode(), null);
        if (faultMode == null) {
            System.out.println("Unknown fault model.");
            return;
        }

        // get benchmark config
        if (this.topology == null) {
            System.out.println("No active topology.");
            return;
        }

        final CountDownLatch responseLatch = new CountDownLatch(topology.getReplicas().size());
        StreamObserver<BenchmarkServiceOuterClass.ProtocolResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.ProtocolResponse protocolResponse) {
                System.out.println(protocolResponse.getOk());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("set protocol failed");
                throwable.printStackTrace();
                responseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                responseLatch.countDown();
            }
        };

        List<Replica> replicas = this.topology.getReplicas();
        Collections.shuffle(replicas);


        BenchmarkServiceOuterClass.ProtocolRequest request = BenchmarkServiceOuterClass.ProtocolRequest.newBuilder()
                .setProtocol(grpcProtocol)
                .setBatchSize(protocol.getBatchSize())
                .setBenchmark(benchmarkMode)
                .setFault(faultMode)
                .addAllFaulty(replicas.stream().limit(topology.getF()).map(Replica::getReplicaId).collect(Collectors.toList()))
                .buildPartial();

        for (Replica replica: this.topology.getReplicas()) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.protocol(request, responseObserver);
        }

        try {
            System.out.println("Waiting for protocol replies...");
            responseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.protocol = protocol;
    }

    public void startBenchmark() {
        // init response listener
        final CountDownLatch responseLatch = new CountDownLatch(topology.getN());
        StreamObserver<BenchmarkServiceOuterClass.StartBenchmarkResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.StartBenchmarkResponse response) {
                System.out.println(response.getOk());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("start benchmark failed");
                throwable.printStackTrace();
                responseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                responseLatch.countDown();
            }
        };

        System.out.println("[start benchmark] Setting up request");

        // setup request
        BenchmarkServiceOuterClass.StartBenchmarkRequest request = BenchmarkServiceOuterClass.StartBenchmarkRequest
                .getDefaultInstance();

        System.out.println("[start benchmark] Sending request");

        // send request
        for (Replica replica: topology.getReplicas()) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.start(request, responseObserver);
        }

        System.out.println("[start benchmark] Waiting for responses");

        // wait for responses
        try {
            System.out.println("Waiting for replies...");
            responseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopBenchmark() {
        // init response listener
        final CountDownLatch responseLatch = new CountDownLatch(topology.getN());
        final Map<Integer, BenchmarkServiceOuterClass.StopBenchmarkResponse> responses = new ConcurrentHashMap<>();
        StreamObserver<BenchmarkServiceOuterClass.StopBenchmarkResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.StopBenchmarkResponse response) {
                responses.put(response.getReplica(), response);
                System.out.println(response.getReplica());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("stop benchmark failed");
                throwable.printStackTrace();
                responseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                responseLatch.countDown();
            }
        };

        System.out.println("[stop benchmark] Setting up request");

        // setup request
        BenchmarkServiceOuterClass.StopBenchmarkRequest request = BenchmarkServiceOuterClass.StopBenchmarkRequest
                .getDefaultInstance();

        System.out.println("[stop benchmark] Sending request");

        // send request
        for (Replica replica: topology.getReplicas()) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.stop(request, responseObserver);
        }

        System.out.println("[stop benchmark] Waiting for replies");

        // wait for replies
        try {
            System.out.println("Running benchmarks...");
            responseLatch.await(60*5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Something went wrong!");
        }

        // parse replies
        List<BenchmarkResult> results = responses.values().stream()
                .map(r -> new BenchmarkResult(
                        r.getReplica(),
                        r.getStart(),
                        r.getFinish(),
                        r.getSentMessages(),
                        r.getRecvMessages(),
                        new ArrayList<>(), // FIXME: empty list passed to latencies?
                        r.getMeasurementsList().stream().map(m -> new BenchmarkResult.ThroughputMeasurement(
                                m.getTimestamp(),
                                m.getValue(),
                                m.getBlockNumber(),
                                m.getProposersList()
                        )).collect(Collectors.toList()),
                        r.getExecutionsList().stream().map(e -> new BenchmarkResult.Execution(
                                e.getPid(),
                                e.getStart(),
                                e.getFinish(),
                                e.getResult()
                        )).collect(Collectors.toList())
                )).collect(Collectors.toList());

        // save results
        JsonService.write(results, topology.getN(), protocol.getName(), protocol.getBatchSize(), protocol.getFaultMode());
    }

    public void shutdown(int timer) {
        List<Replica> replicas = new ArrayList<>(replicaRepository.getAll());

        final CountDownLatch responseLatch = new CountDownLatch(replicas.size());
        StreamObserver<BenchmarkServiceOuterClass.ShutdownResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.ShutdownResponse shutdownResponse) {
                // TODO do nothing?
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("shutdown failed");
                throwable.printStackTrace();
                responseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                responseLatch.countDown();
            }
        };

        BenchmarkServiceOuterClass.ShutdownRequest request = BenchmarkServiceOuterClass.ShutdownRequest
                .newBuilder().setTimer(timer).build();

        for (Replica replica: replicas) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.shutdown(request, responseObserver);
        }

        try {
            System.out.println("Waiting for shutdown responses...");
            responseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
