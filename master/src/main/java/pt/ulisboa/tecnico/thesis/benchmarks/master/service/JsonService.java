package pt.ulisboa.tecnico.thesis.benchmarks.master.service;

public class JsonService {
//    public static void write(List<BenchmarkResult> results, Map<Integer,List<Summary>> history, int n, String protocol, Integer batchSize, String faultMode, int load) {
//        JsonObject root = new JsonObject();
//        root.addProperty("protocol", protocol);
//        root.addProperty("n", n);
//        root.addProperty("batch-size", batchSize);
//        root.addProperty("payload-size", 250);
//        root.addProperty("fault", faultMode);
//        root.addProperty("load", load);
//
//        JsonArray jsonResults = new JsonArray();
//        for (BenchmarkResult result: results) {
//            JsonObject jsonResult = new JsonObject();
//
//            jsonResult.addProperty("replica", result.getReplicaId());
//            jsonResult.addProperty("start", result.getStartTime());
//            jsonResult.addProperty("finish", result.getFinishTime());
//            jsonResult.addProperty("sent-messages", result.getSentMessageCount());
//            jsonResult.addProperty("recv-messages", result.getRecvMessageCount());
//            jsonResult.addProperty("total-tx", result.getTotalTx());
//            jsonResult.addProperty("dropped-tx", result.getDroppedTx());
//
//            JsonArray jsonExecutions = new JsonArray();
//            for (Summary s: history.get(result.getReplicaId())) {
//                JsonObject jsonExecution = new JsonObject();
//
//                jsonExecution.addProperty("start", s.getStart());
//                jsonExecution.addProperty("finish", s.getFinish());
//                jsonExecution.addProperty("tx-committed", s.getTxCommitted());
//                jsonExecution.addProperty("avg-latency", s.getAvgLatency());
//                jsonExecution.addProperty("cpu-load", s.getCpuLoad());
//                jsonExecution.addProperty("in-bandwidth", s.getInBandwidth());
//                jsonExecution.addProperty("out-bandwidth", s.getOutBandwidth());
//
//                jsonExecutions.add(jsonExecution);
//            }
//
//            jsonResult.add("executions", jsonExecutions);
//
//            jsonResults.add(jsonResult);
//        }
//        root.add("results", jsonResults);
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String outputFile = String.format("results/%s-%d-%d-%s-%d.json", protocol, n, batchSize, faultMode, load);
//        try (Writer writer = new FileWriter(outputFile)) {
//            gson.toJson(root, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
