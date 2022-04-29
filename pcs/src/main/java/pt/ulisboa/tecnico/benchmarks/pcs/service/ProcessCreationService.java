package pt.ulisboa.tecnico.benchmarks.pcs.service;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.ProcessCreationServiceGrpc;
import pt.ulisboa.tecnico.thesis.benchmarks.contract.ProcessCreationServiceOuterClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProcessCreationService extends ProcessCreationServiceGrpc.ProcessCreationServiceImplBase {

    private final URI masterUri;
    private final List<Process> replicaProcesses = new ArrayList<>();

    public ProcessCreationService(URI masterUri) {
        this.masterUri = masterUri;
    }

    @Override
    public void replica(
            ProcessCreationServiceOuterClass.CreateReplicaRequest request,
            StreamObserver<ProcessCreationServiceOuterClass.CreateReplicaResponse> responseObserver
    ) {
        boolean result = _replica();

        ProcessCreationServiceOuterClass.CreateReplicaResponse response = ProcessCreationServiceOuterClass.CreateReplicaResponse
                .newBuilder().setOk(result).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private boolean _replica() {
        // FIXME: Remove hard coded version (use parameters or something)
        final String javaPath = "/bin/java";
        // final String jarPath = "D:\\Code\\benchmarks\\replica\\target\\replica-1.0-SNAPSHOT.jar";
        final String jarPath = "/home/diogo/MEGA/LEIC-A/Projeto BIG/Study material/code/alea-benchmarks/replica/target/replica-1.0-SNAPSHOT.jar";


        ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", jarPath, String.valueOf(replicaProcesses.size()), masterUri.toString());
        processBuilder.redirectOutput(Redirect.INHERIT);
        processBuilder.redirectError(Redirect.INHERIT);

        try {
            Process process = processBuilder.start();
            replicaProcesses.add(process);
            // StreamGobbler streamGobbler =
            //         new StreamGobbler(process.getErrorStream(), System.out::println);
            // Executors.newSingleThreadExecutor().submit(streamGobbler);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
