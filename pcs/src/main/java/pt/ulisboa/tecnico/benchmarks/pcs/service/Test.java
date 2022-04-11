package pt.ulisboa.tecnico.benchmarks.pcs.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Test {
    public static void main(String[] args) {
        String javaPath = "C:/Program Files/Java/jdk-11.0.6/java.exe";
        javaPath = "java.exe";
        String jarPath = "D:\\Code\\benchmarks\\replica\\target\\replica-1.0-SNAPSHOT.jar";
        ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", jarPath);
        //ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-cp", jarPath, "pt.ulisboa.tecnico.thesis.benchmarks.replica.Main");

        try {
            Process process = processBuilder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
