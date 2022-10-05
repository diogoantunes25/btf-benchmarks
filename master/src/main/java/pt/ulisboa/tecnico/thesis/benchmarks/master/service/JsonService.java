package pt.ulisboa.tecnico.thesis.benchmarks.master.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.BenchmarkResult;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Commit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class JsonService {
    public static void write(List<BenchmarkResult> results, Map<Integer,List<Commit>> history, int n, String protocol, Integer batchSize, String faultMode, int load) {
        JsonObject root = new JsonObject();
        root.addProperty("protocol", protocol);
        root.addProperty("n", n);
        root.addProperty("batch-size", batchSize);
        root.addProperty("payload-size", 250);
        root.addProperty("fault", faultMode);
        root.addProperty("load", load);

        JsonArray jsonResults = new JsonArray();
        for (BenchmarkResult result: results) {
            JsonObject jsonResult = new JsonObject();

            jsonResult.addProperty("replica", result.getReplicaId());
            jsonResult.addProperty("start", result.getStartTime());
            jsonResult.addProperty("finish", result.getFinishTime());
            jsonResult.addProperty("sent-messages", result.getSentMessageCount());
            jsonResult.addProperty("recv-messages", result.getRecvMessageCount());

            JsonArray jsonExecutions = new JsonArray();
            for (Commit commit: history.get(result.getReplicaId())) {
                JsonObject jsonExecution = new JsonObject();

                jsonExecution.addProperty("start", commit.getStart());
                jsonExecution.addProperty("finish", commit.getFinish());

                jsonExecutions.add(jsonExecution);
            }
            jsonResult.add("executions", jsonExecutions);

            jsonResults.add(jsonResult);
        }
        root.add("results", jsonResults);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String outputFile = String.format("results/%s-%d-%d-%s-%d.json", protocol, n, batchSize, faultMode, load);
        try (Writer writer = new FileWriter(outputFile)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
