package pt.ulisboa.tecnico.thesis.benchmarks.master.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.BenchmarkResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class JsonService {
    public static void write(List<BenchmarkResult> results, int n, String protocol, Integer batchSize, String faultMode) {
        JsonObject root = new JsonObject();
        root.addProperty("protocol", protocol);
        root.addProperty("n", n);
        root.addProperty("batch-size", batchSize);
        root.addProperty("payload-size", 250);
        root.addProperty("fault", faultMode);

        JsonArray jsonResults = new JsonArray();
        for (BenchmarkResult result: results) {
            JsonObject jsonResult = new JsonObject();

            jsonResult.addProperty("replica", result.getReplicaId());
            jsonResult.addProperty("start", result.getStartTime());
            jsonResult.addProperty("finish", result.getFinishTime());
            jsonResult.addProperty("sent-messages", result.getSentMessageCount());
            jsonResult.addProperty("recv-messages", result.getRecvMessageCount());

            /*
            NOTE: moved calculations to visualizer script

            jsonResult.addProperty("latency-avg", result.getAvgLatency());
            jsonResult.addProperty("latency-min", result.getMinLatency());
            jsonResult.addProperty("latency-max", result.getMaxLatency());

            jsonResult.addProperty("throughput-avg", result.getAvgThroughput());
            jsonResult.addProperty("throughput-min", result.getMinThroughput());
            jsonResult.addProperty("throughput-max", result.getMaxThroughput());
             */

            JsonArray jsonLatencies = new JsonArray();
            for (BenchmarkResult.LatencyMeasurement measurement: result.getLatencyMeasurements()) {
                JsonObject jsonLatency = new JsonObject();
                jsonLatency.addProperty("emission", measurement.getEmission());
                jsonLatency.addProperty("delivery", measurement.getDelivery());
                jsonLatency.addProperty("value", measurement.getValue());
                jsonLatencies.add(jsonLatency);
            }
            jsonResult.add("latencies", jsonLatencies);

            JsonArray jsonThroughputs = new JsonArray();
            for (BenchmarkResult.ThroughputMeasurement measurement: result.getThroughputMeasurements()) {
                JsonObject jsonThroughput = new JsonObject();
                jsonThroughput.addProperty("timestamp", measurement.getTimestamp());
                jsonThroughput.addProperty("value", measurement.getValue());
                jsonThroughput.addProperty("block", measurement.getBlockNumber());

                JsonArray jsonProposers = new JsonArray();
                for (Integer proposer: measurement.getProposers())
                    jsonProposers.add(proposer);
                jsonThroughput.add("proposers", jsonProposers);

                jsonThroughputs.add(jsonThroughput);
            }
            jsonResult.add("throughputs", jsonThroughputs);

            JsonArray jsonExecutions = new JsonArray();
            for (BenchmarkResult.Execution execution: result.getExecutions()) {
                JsonObject jsonExecution = new JsonObject();

                jsonExecution.addProperty("pid", execution.getPid());
                jsonExecution.addProperty("start", execution.getStart());
                jsonExecution.addProperty("finish", execution.getFinish());
                jsonExecution.addProperty("result", execution.getResult());

                jsonExecutions.add(jsonExecution);
            }
            jsonResult.add("executions", jsonExecutions);

            jsonResults.add(jsonResult);
        }
        root.add("results", jsonResults);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String outputFile = String.format("results/%s-%d-%d-%s.json", protocol, n, batchSize, faultMode);
        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
