package pt.ulisboa.tecnico.thesis.benchmarks.client.service.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.client.Client;
import pt.ulisboa.tecnico.thesis.benchmarks.client.Reporter;
import pt.ulisboa.tecnico.thesis.benchmarks.client.exceptions.ReplicasUnknownException;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.BenchmarkServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.LoadServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.LoadServiceOuterClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadGrpcService extends LoadServiceGrpc.LoadServiceImplBase {

    private final int REPORT_WAIT_TIME = 1000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Client client;
    private Reporter reporter;

    public LoadGrpcService(Client client) {
        this.client = client;
        this.reporter = new Reporter(client);
    }

    @Override
    public void start(LoadServiceOuterClass.StartRequest request, StreamObserver<LoadServiceOuterClass.StartResponse> responseObserver) {
        logger.info("start called");
        Map<Integer, String> replicas = new HashMap<>();

        for (LoadServiceOuterClass.StartRequest.Replica replica: request.getReplicasList())
            replicas.put(replica.getId(), replica.getIp());

        client.setReplicas(replicas);
        try {
            client.start(request.getLoad());
        } catch (ReplicasUnknownException e) {
            e.printStackTrace();
        }

        responseObserver.onNext(LoadServiceOuterClass.StartResponse.newBuilder().build());
        responseObserver.onCompleted();

        reporter.start(REPORT_WAIT_TIME);
    }

    public void stop(LoadServiceOuterClass.StopRequest request, StreamObserver<LoadServiceOuterClass.StopResponse> responseObserver) {
        logger.info("stop called");
        client.stop();
        responseObserver.onNext(LoadServiceOuterClass.StopResponse.newBuilder().build());
        responseObserver.onCompleted();
        reporter.stop();
    }
}