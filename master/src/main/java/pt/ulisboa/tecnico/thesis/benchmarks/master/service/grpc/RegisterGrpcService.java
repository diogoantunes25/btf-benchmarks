package pt.ulisboa.tecnico.thesis.benchmarks.master.service.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.RegisterServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.RegisterServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.master.repository.ReplicaRepository;

public class RegisterGrpcService extends RegisterServiceGrpc.RegisterServiceImplBase {

    private final ReplicaRepository repository;

    public RegisterGrpcService(ReplicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void register(
            RegisterServiceOuterClass.RegisterRequest request,
            StreamObserver<RegisterServiceOuterClass.RegisterResponse> responseObserver
    ) {
        // generate auto incremented replica id
        int replicaId = repository.getAll().size();

        // init replica
        Replica replica = new Replica(
                replicaId,
                request.getAddress(),
                request.getPort(),
                request.getControlPort()
        );

        // store replica in repository
        repository.addReplica(replica);

        // send register response
        RegisterServiceOuterClass.RegisterResponse response = RegisterServiceOuterClass.RegisterResponse.newBuilder()
                .setReplicaId(replicaId).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
