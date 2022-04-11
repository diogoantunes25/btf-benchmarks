package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import java.util.List;

public class BenchmarkResult {

    private Integer replicaId;
    private Long startTime;
    private Long finishTime;
    private Long sentMessageCount;
    private Long recvMessageCount;

    private List<LatencyMeasurement> latencyMeasurements;
    private List<ThroughputMeasurement> throughputMeasurements;
    private List<Execution> executions;

    public BenchmarkResult() {
    }

    public BenchmarkResult(
            Integer replicaId,
            Long startTime,
            Long finishTime,
            Long sentMessageCount,
            Long recvMessageCount,
            List<LatencyMeasurement> lm,
            List<ThroughputMeasurement> tm,
            List<Execution> executions
    ) {
        this.replicaId = replicaId;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.sentMessageCount = sentMessageCount;
        this.recvMessageCount = recvMessageCount;
        this.latencyMeasurements = lm;
        this.throughputMeasurements = tm;
        this.executions = executions;
    }

    public Integer getReplicaId() {
        return replicaId;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public Long getFinishTime() {
        return this.finishTime;
    }

    public Long getSentMessageCount() {
        return sentMessageCount;
    }

    public Long getRecvMessageCount() {
        return recvMessageCount;
    }

    public List<LatencyMeasurement> getLatencyMeasurements() {
        return latencyMeasurements;
    }

    public List<ThroughputMeasurement> getThroughputMeasurements() {
        return throughputMeasurements;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public Double getAvgLatency() {
        return latencyMeasurements.stream().mapToDouble(LatencyMeasurement::getValue).average().orElse(0L);
    }

    public Double getAvgThroughput() {
        return throughputMeasurements.stream().mapToDouble(ThroughputMeasurement::getValue).average().orElse(0L);
    }

    public Double getMinLatency() {
        return latencyMeasurements.stream().mapToDouble(LatencyMeasurement::getValue).min().orElse(0);
    }

    public Double getMinThroughput() {
        return throughputMeasurements.stream().mapToDouble(ThroughputMeasurement::getValue).min().orElse(0);
    }

    public Double getMaxLatency() {
        return latencyMeasurements.stream().mapToDouble(LatencyMeasurement::getValue).max().orElse(0);
    }

    public Double getMaxThroughput() {
        return throughputMeasurements.stream().mapToDouble(ThroughputMeasurement::getValue).max().orElse(0);
    }

    public static class LatencyMeasurement {
        private final Long emission;
        private final Long delivery;
        private final Long value;

        public LatencyMeasurement(Long emission, Long delivery, Long value) {
            this.emission = emission;
            this.delivery = delivery;
            this.value = value;
        }

        public Long getEmission() {
            return emission;
        }

        public Long getDelivery() {
            return delivery;
        }

        public Long getValue() {
            return value;
        }
    }

    public static class ThroughputMeasurement {
        private final Long timestamp;
        private final Long value;
        private final Long blockNumber;
        private final List<Integer> proposers;

        public ThroughputMeasurement(Long timestamp, Long value, Long blockNumber, List<Integer> proposers) {
            this.timestamp = timestamp;
            this.value = value;
            this.blockNumber = blockNumber;
            this.proposers = proposers;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public Long getValue() {
            return value;
        }

        public Long getBlockNumber() {
            return blockNumber;
        }

        public List<Integer> getProposers() {
            return proposers;
        }
    }

    public static class Execution {
        private final String pid;
        private final Long start;
        private final Long finish;
        private final Boolean result;

        public Execution(String pid, Long start, Long finish, Boolean result) {
            this.pid = pid;
            this.start = start;
            this.finish = finish;
            this.result = result;
        }

        public String getPid() {
            return pid;
        }

        public Long getStart() {
            return start;
        }

        public Long getFinish() {
            return finish;
        }

        public Boolean getResult() {
            return result;
        }
    }
}
