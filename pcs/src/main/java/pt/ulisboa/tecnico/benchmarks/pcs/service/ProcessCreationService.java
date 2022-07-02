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

    private boolean _replica(String pcsIP, int replicaID) {
        // FIXME: Remove hard coded version (use parameters or something)
        //final String javaPath = "/bin/java";
        final String javaPath = "/opt/java/openjdk/bin/java";
        // final String jarPath = "D:\\Code\\benchmarks\\replica\\target\\replica-1.0-SNAPSHOT.jar";
        // final String jarPath = "/home/diogo/MEGA/2 | LEIC-A/Projeto BIG/Study material/code/alea-benchmarks/replica/target/replica-1.0-SNAPSHOT.jar";
        final String jarPath = "/alea/replica.jar";

        int offset = replicaProcesses.size();

        ProcessBuilder processBuilder;
        if (DEBUG_MODE) {
            // These arguments are needed to be able to attach a remote debugger to the replica processes
            String debugParamenter = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%d", 1044 + replicaProcesses.size());
            processBuilder = new ProcessBuilder(javaPath, debugParamenter, "-jar", jarPath, String.valueOf(replicaProcesses.size()), masterUri.toString(), pcsIP);
        }
        else {
            processBuilder = new ProcessBuilder(javaPath, "-jar", jarPath, String.valueOf(replicaProcesses.size()), masterUri.toString(), String.valueOf(pcsIP));
        }

        // By default, the output and error are redirected to pipes
        // processBuilder.redirectOutput(Redirect.INHERIT);
        File outputFile = new File("./logs/replica" + replicaProcesses.size() + ".output");
        File logFile = new File("./logs/replica" + replicaProcesses.size() + ".log");
        processBuilder.redirectOutput(Redirect.to(outputFile));
        processBuilder.redirectError(Redirect.to(logFile));

        try {
            Process process = processBuilder.start();

            System.out.println("Replica " + replicaProcesses.size() + " was created.");

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
