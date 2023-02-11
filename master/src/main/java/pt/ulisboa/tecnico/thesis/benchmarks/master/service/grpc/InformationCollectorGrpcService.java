package pt.ulisboa.tecnico.thesis.benchmarks.master.service.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.InformationCollectorServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.UpdateRepository;

public class InformationCollectorGrpcService extends InformationCollectorServiceGrpc.InformationCollectorServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UpdateRepository updateRepository;

    public InformationCollectorGrpcService(UpdateRepository updateRepository) {
        this.updateRepository = updateRepository;
    }

    @Override
    public void replicaUpdate(InformationCollectorServiceOuterClass.ReplicaRequest request,
                              StreamObserver<InformationCollectorServiceOuterClass.ReplicaResponse> responseObserver) {

        responseObserver.onNext(InformationCollectorServiceOuterClass.ReplicaResponse.newBuilder().build());
        responseObserver.onCompleted();

        updateRepository.addReplicaUpdate(
                request.getReplicaId(),
                request.getSystemInfo().getCpu(),
                request.getSystemInfo().getBandwidthIn(),
                request.getSystemInfo().getBandwidthOut(),
                request.getSystemInfo().getFreeMemory(),
                request.getSystemInfo().getTotalMemory(),
                request.getTime()
        );
    }

    @Override
    public void clientUpdate(InformationCollectorServiceOuterClass.ClientRequest request,
                             StreamObserver<InformationCollectorServiceOuterClass.ClientResponse> responseObserver) {

        responseObserver.onNext(InformationCollectorServiceOuterClass.ClientResponse.newBuilder().build());
        responseObserver.onCompleted();

        updateRepository.addClientUpdate(
            request.getClientId(),
            request.getReplicaId(),
            request.getTxs(),
            request.getLatency(),
            request.getStart(),
            request.getEnd(),
            request.getSystemInfo().getCpu(),
            request.getSystemInfo().getBandwidthIn(),
            request.getSystemInfo().getBandwidthOut(),
            request.getSystemInfo().getFreeMemory(),
            request.getSystemInfo().getTotalMemory()
        );
    }

}
