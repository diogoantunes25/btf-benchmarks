package pt.ulisboa.tecnico.thesis.benchmarks.replica.service.grpc;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.TransactionServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.TransactionServiceOuterClass;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.replica.ThroughputReplica;

import java.util.Random;

public class TransactionGrpcService extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(TransactionGrpcService.class);
    @Override
    public void submit(TransactionServiceOuterClass.SubmitRequest request, StreamObserver<TransactionServiceOuterClass.SubmitResponse> responseObserver) {
        logger.info("got transaction");

        try {
            Thread.sleep(Math.abs((new Random()).nextInt()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO: actually handle request
        TransactionServiceOuterClass.SubmitResponse response = TransactionServiceOuterClass.SubmitResponse.newBuilder().setOk(true).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
