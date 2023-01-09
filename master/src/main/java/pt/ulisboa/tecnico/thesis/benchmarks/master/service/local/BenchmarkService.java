package pt.ulisboa.tecnico.thesis.benchmarks.master.service.local;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.LoadServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.LoadServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.*;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.BenchmarkRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ClientRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ReplicaRepository;
import pt.ulisboa.tecnico.thesis.benchmarks.master.service.JsonService;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Summary;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BenchmarkService {

    private final ReplicaRepository replicaRepository;
    private final BenchmarkRepository benchmarkRepository;
    private final ClientRepository clientRepository;

    private Benchmark.Topology topology;
    private Benchmark.Protocol protocol;

    // Thread that periodically retrieves information from replicas
    private InformationCollector informationCollector = null;

    public BenchmarkService(ReplicaRepository replicaRepository, BenchmarkRepository benchmarkRepository, ClientRepository clientRepository) {
        this.replicaRepository = replicaRepository;
        this.benchmarkRepository = benchmarkRepository;
        this.clientRepository = clientRepository;
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
            System.out.println("Topology set failed...");
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
        } else {
            System.out.println("Fault mode: " + protocol.getFaultMode());
            System.out.println("Faulty replicas are: " +
                    this.topology.getReplicas().stream().limit(topology.getF()).map(Replica::getReplicaId).collect(Collectors.toList()));
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
                .setLoad(protocol.getLoad())
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
        final CountDownLatch replicaResponseLatch = new CountDownLatch(topology.getN());
        final CountDownLatch clientResponseLatch = new CountDownLatch(clientRepository.size());
        StreamObserver<BenchmarkServiceOuterClass.StartBenchmarkResponse> replicaResponseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.StartBenchmarkResponse response) {
                System.out.println(response.getOk());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("start benchmark failed");
                throwable.printStackTrace();
                replicaResponseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                replicaResponseLatch.countDown();
            }
        };
        StreamObserver<LoadServiceOuterClass.StartResponse> clientResponseObserver = new StreamObserver<LoadServiceOuterClass.StartResponse>() {
            @Override
            public void onNext(LoadServiceOuterClass.StartResponse response) {
                System.out.println("ok");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("start benchmark failed");
                throwable.printStackTrace();
                clientResponseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                clientResponseLatch.countDown();
            }
        };

        System.out.println("[start benchmark] Setting up request");

        // setup request
        BenchmarkServiceOuterClass.StartBenchmarkRequest.Builder requestBuilder = BenchmarkServiceOuterClass.StartBenchmarkRequest.newBuilder();

        // send request to replicas
        System.out.println("[start benchmark] Sending request");
        boolean first = true;
        for (Replica replica: topology.getReplicas()) {
            BenchmarkServiceOuterClass.StartBenchmarkRequest request = requestBuilder.setFirst(first).build();
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.start(request, replicaResponseObserver);

            first = false;
        }

        // wait for replica responses
        try {
            System.out.println("[start benchmark] Waiting for replica responses");
            replicaResponseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // send request to clients
        for (Client client: clientRepository.getAll()) {
            LoadServiceOuterClass.StartRequest request = LoadServiceOuterClass.StartRequest.newBuilder()
                    .addAllReplicas(
                            replicaRepository.getAll().stream()
                                    .map(replica -> LoadServiceOuterClass.StartRequest.Replica.newBuilder()
                                                        .setId(replica.getReplicaId())
                                                        .setIp(replica.getAddress())
                                                        .build())
                                    .collect(Collectors.toList())
                    )
                    .setLoad(protocol.getLoad())
                    .build();

            ManagedChannel channel = client.getChannel();
            LoadServiceGrpc.LoadServiceStub stub = LoadServiceGrpc.newStub(channel);
            stub.start(request, clientResponseObserver);
        }

        // wait for client responses
        try {
            System.out.println("[start benchmark] Waiting for client responses");
            replicaResponseLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.informationCollector = new InformationCollector();
        this.informationCollector.start();
        System.out.println("[start benchmark] started information collector");

    }

    public void stopBenchmark() {
        // init response listener
        final CountDownLatch replicaResponseLatch = new CountDownLatch(topology.getN());
        final CountDownLatch clientResponseLatch = new CountDownLatch(clientRepository.size());
        final Map<Integer, BenchmarkServiceOuterClass.StopBenchmarkResponse> responses = new ConcurrentHashMap<>();
        StreamObserver<BenchmarkServiceOuterClass.StopBenchmarkResponse> replicaResponseObserver = new StreamObserver<>() {
            @Override
            public void onNext(BenchmarkServiceOuterClass.StopBenchmarkResponse response) {
                responses.put(response.getReplica(), response);
                System.out.println(response.getReplica());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("stop benchmark failed");
                throwable.printStackTrace();
                replicaResponseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                replicaResponseLatch.countDown();
            }
        };

        StreamObserver<LoadServiceOuterClass.StopResponse> clientResponseObserver = new StreamObserver<>() {
            @Override
            public void onNext(LoadServiceOuterClass.StopResponse response) {
                System.out.println("ok");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("stop benchmark failed");
                throwable.printStackTrace();
                clientResponseLatch.countDown();
            }

            @Override
            public void onCompleted() {
                clientResponseLatch.countDown();
            }
        };


        this.informationCollector.stop();
        Map<Integer,List<Summary>> history = this.informationCollector.getHistory();
        this.informationCollector = null;
        System.out.println("[stop benchmark] stopped information collector");

        System.out.println("[stop benchmark] Setting up request");

        // setup client request
        LoadServiceOuterClass.StopRequest clientRequest = LoadServiceOuterClass.StopRequest.newBuilder().build();
        for (Client client: clientRepository.getAll()) {
            ManagedChannel channel = client.getChannel();
            LoadServiceGrpc.LoadServiceStub stub = LoadServiceGrpc.newStub(channel);
            stub.stop(clientRequest, clientResponseObserver);
        }

        // wait for replies
        try {
            System.out.println("Running benchmarks...");
            clientResponseLatch.await(60*5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Something went wrong!");
        }

        // setup request
        BenchmarkServiceOuterClass.StopBenchmarkRequest replicaRequest = BenchmarkServiceOuterClass.StopBenchmarkRequest
                .getDefaultInstance();

        System.out.println("[stop benchmark] Sending request");

        // send request
        for (Replica replica: topology.getReplicas()) {
            ManagedChannel channel = replica.getChannel();
            BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
            stub.stop(replicaRequest, replicaResponseObserver);
        }

        System.out.println("[stop benchmark] Waiting for replies");

        // wait for replies
        try {
            System.out.println("Running benchmarks...");
            replicaResponseLatch.await(60*5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Something went wrong!");
        }

        // parse replies
        List<BenchmarkResult> overview = responses.values().stream()
                .map(r -> new BenchmarkResult(
                        r.getReplica(),
                        r.getStart(),
                        r.getFinish(),
                        r.getSentMessages(),
                        r.getRecvMessages(),
                        r.getTotalTx(),
                        r.getDroppedTx()
                )).collect(Collectors.toList());

        // save results
        JsonService.write(overview, history, topology.getN(), protocol.getName(), protocol.getBatchSize(), protocol.getFaultMode(), protocol.getLoad());
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

        // TODO: Probably need to shutdown PCS also
    }

    // Thread that periodically requests information from replicas
    private class InformationCollector {

        private final Logger logger = LoggerFactory.getLogger(InformationCollector.class);
        private final int WAIT_PERIOD = 10000; // Milliseconds
        private final Map<Integer, List<Summary>> history = new HashMap<>();
        private Thread thread = null;
        public InformationCollector() {
            topology.getN();
            for (int i = 0; i < topology.getN(); i++) {
                history.put(i, new ArrayList<>());
            }
        }

        public void start() {

            if (this.thread != null) {
                return;
            }

            this.thread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(WAIT_PERIOD);
                        getInfo();
                    } catch(InterruptedException e) {
                        return;
                    }
                }
            });

            this.thread.start();
        }

        public void stop() {
            this.thread.interrupt();
        }

        public Map<Integer, List<Summary>> getHistory() {
            return history;
        }

        public void getInfo() {
            // logger.info("Retrieving information from replicas");

            final CountDownLatch responseLatch = new CountDownLatch(topology.getN());
            final Map<Integer, BenchmarkServiceOuterClass.InformResponse> responses = new ConcurrentHashMap<>();

            StreamObserver<BenchmarkServiceOuterClass.InformResponse> responseObserver = new StreamObserver<>() {
                @Override
                public void onNext(BenchmarkServiceOuterClass.InformResponse response) {
                    responses.put(response.getReplica(), response);
                    // logger.info("New information arrived from {} ({} commits)", response.getReplica(), response.getCommitsCount());
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

            BenchmarkServiceOuterClass.InformRequest.Builder requestBuilder = BenchmarkServiceOuterClass.InformRequest.newBuilder();

            // logger.info("Sending retrieval requests");

            for(Replica replica: topology.getReplicas()) {
                ManagedChannel channel = replica.getChannel();
                BenchmarkServiceGrpc.BenchmarkServiceStub stub = BenchmarkServiceGrpc.newStub(channel);
                stub.inform(requestBuilder.build(), responseObserver);
            }


            try {
                // logger.info("Waiting for responses");
                responseLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // logger.info("All responses collected");

            // Add response info to history
            for (int i = 0; i < topology.getN(); i++) {
                BenchmarkServiceOuterClass.InformResponse response = responses.get(i);
                Summary summary = new Summary(response.getStart(), response.getFinish(), response.getTxCommitted(),
                    response.getAvgLatency(), response.getCPULoad(), response.getInBandwidth(), response.getOutBandwidth());
                history.get(i).add(summary);
            }
        }
    }
}
