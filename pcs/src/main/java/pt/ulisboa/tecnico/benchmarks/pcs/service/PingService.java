package pt.ulisboa.tecnico.benchmarks.pcs.service;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.PingServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.PingServiceOuterClass;

public class PingService extends PingServiceGrpc.PingServiceImplBase {

    @Override
    public void ping(
        PingServiceOuterClass.Ping request,
        StreamObserver<PingServiceOuterClass.Pong> responseObserver
    ) {
        PingServiceOuterClass.Pong pong = PingServiceOuterClass.Pong.newBuilder().build();
        responseObserver.onNext(pong);
        responseObserver.onCompleted();
    }
}
