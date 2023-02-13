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
import java.io.File;

public class ProcessCreationService extends ProcessCreationServiceGrpc.ProcessCreationServiceImplBase {

    private final URI masterUri;
    private final List<Process> replicaProcesses = new ArrayList<>();

    private final boolean DEBUG_MODE = false;

    public ProcessCreationService(URI masterUri) {
        this.masterUri = masterUri;
    }

    @Override
    public void replica(
            ProcessCreationServiceOuterClass.CreateReplicaRequest request,
            StreamObserver<ProcessCreationServiceOuterClass.CreateReplicaResponse> responseObserver
    ) {
        boolean result = _replica(request.getIpPcs(), request.getReplicaId());

        ProcessCreationServiceOuterClass.CreateReplicaResponse response = ProcessCreationServiceOuterClass.CreateReplicaResponse
                .newBuilder().setOk(result).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private boolean _replica(String pcsIP, int replicaId) {
        // FIXME: Remove hard coded version (use parameters or something)
        final String jarPath = "./replica.jar";

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar",
                jarPath, String.valueOf(replicaId), masterUri.toString(), String.valueOf(pcsIP));

//        File outputFile = new File("./logs/replica" + replicaId + ".output");
//        File logFile = new File("./logs/replica" + replicaId + ".log");
//        processBuilder.redirectOutput(Redirect.to(outputFile));
//        processBuilder.redirectError(Redirect.to(logFile));

        try {
            Process process = processBuilder.start();

            System.out.println("Replica " + replicaId + " was created.");

            replicaProcesses.add(process);

            if (!process.isAlive()) {
                System.out.println(process.exitValue());
                return false;
            }

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
