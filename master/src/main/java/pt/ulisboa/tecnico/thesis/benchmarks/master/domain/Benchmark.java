package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;

import java.util.List;
import java.util.Map;

// Benchmark for given protocol and topology
public class Benchmark {

    private final Integer id;

    private Topology topology;
    private Protocol protocol;
    private Map<Integer, BenchmarkResult> results;

    public Benchmark(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        if (this.topology == null) this.topology = topology;
    }

    public void setProtocol(Protocol protocol) {
        if (this.protocol == null) this.protocol = protocol;
    }

    public void setResults(Map<Integer, BenchmarkResult> results) {
        this.results = results;
    }

    public Map<Integer, BenchmarkResult> getResults() {
        return results;
    }

    public static class Topology {
        private final List<Replica> replicas;
        private final GroupKey groupKey;
        private final List<KeyShare> keyShares;
        private final Integer f;

        public Topology(List<Replica> replicas, GroupKey groupKey, List<KeyShare> keyShares, Integer f) {
            this.replicas = replicas;
            this.groupKey = groupKey;
            this.keyShares = keyShares;
            this.f = f;
        }

        public List<Replica> getReplicas() {
            return replicas;
        }

        public GroupKey getGroupKey() {
            return groupKey;
        }

        public List<KeyShare> getKeyShares() {
            return keyShares;
        }

        public Integer getN() {
            return replicas.size();
        }

        public Integer getF() {
            return f;
        }
    }

    public static class Protocol {
        private final String name;
        private final Integer batchSize;

        private final String benchmarkMode;
        private final String faultMode;
        private final int load;

        public Protocol(String name, Integer batchSize, String benchmarkMode, String faultMode, int load) {
            this.name = name;
            this.batchSize = batchSize;
            this.benchmarkMode = benchmarkMode;
            this.faultMode = faultMode;
            this.load = load;
        }

        public String getName() {
            return name;
        }

        public Integer getBatchSize() {
            return batchSize;
        }

        public String getBenchmarkMode() {
            return benchmarkMode;
        }

        public String getFaultMode() {
            return faultMode;
        }

        public int getLoad() { return load; }
    }
}
