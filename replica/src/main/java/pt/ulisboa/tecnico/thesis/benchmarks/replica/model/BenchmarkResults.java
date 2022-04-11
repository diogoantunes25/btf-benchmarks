package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

import java.util.*;
import java.util.stream.Collectors;

public class BenchmarkResults {

    private final Long startTime;

    private final Long targetTxs;
    private long executedTxs;

    private Long lastMeasurementTime;

    public final Map<LatencyMeasurement.Key, LatencyMeasurement> latencyMeasurements;
    public final Map<Long, ThroughputMeasurement> throughputMeasurements;

    public BenchmarkResults(Long startTime, Long targetTxs) {
        this.startTime = startTime;
        this.targetTxs = targetTxs;
        this.lastMeasurementTime = startTime;

        this.latencyMeasurements = new HashMap<>();
        this.throughputMeasurements = new HashMap<>();
    }

    public BenchmarkResults(BenchmarkResults other) {
        this.startTime = other.getStartTime();
        this.targetTxs = other.getTargetTxs();
        this.executedTxs = other.getExecutedTxs();
        this.lastMeasurementTime = other.getLastMeasurementTime();

        this.latencyMeasurements = other.latencyMeasurements;
        this.throughputMeasurements = other.throughputMeasurements;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return isComplete() ? lastMeasurementTime : 0;
    }

    public Long getTargetTxs() {
        return targetTxs;
    }

    public long getExecutedTxs() {
        return executedTxs;
    }

    public Long getLastMeasurementTime() {
        return lastMeasurementTime;
    }

    public Boolean isComplete() {
        return this.executedTxs >= this.targetTxs;
    }

    public void logSendEvent(byte[] tx, long timestamp) {
        LatencyMeasurement.Key key = new LatencyMeasurement.Key(tx);
        this.latencyMeasurements.put(key, new LatencyMeasurement(timestamp));
    }

    public void logDeliveryEvent(Collection<byte[]> txs, long timestamp) {
        if (isComplete()) return;

        // log latency metrics
        for (byte[] tx: txs) {
            LatencyMeasurement.Key key = new LatencyMeasurement.Key(tx);
            LatencyMeasurement lm = this.latencyMeasurements.getOrDefault(key, null);
            if (lm != null) lm.setDelivery(timestamp);
        }

        // log throughput metrics
        // long throughput = (txs.size()*1000L) / Math.max(timestamp - this.lastMeasurementTime, 1L);
        this.throughputMeasurements.put(timestamp, new ThroughputMeasurement(timestamp, (long) txs.size()));

        // update control variables
        this.lastMeasurementTime = timestamp;
        this.executedTxs += txs.size();
    }

    public List<LatencyMeasurement> getLatencyMeasurements() {
        return latencyMeasurements.values().stream().filter(lm -> lm.getDelivery() > 0).collect(Collectors.toList());

    }

    public List<ThroughputMeasurement> getThroughputMeasurements() {
        return new ArrayList<>(throughputMeasurements.values());
    }

    public static class ThroughputMeasurement {
        private final Long timestamp;
        private final Long value;

        public ThroughputMeasurement(Long timestamp, Long value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public Long getValue() {
            return value;
        }
    }

    public static class LatencyMeasurement {
        private final Long emission;
        private Long delivery = 0L;

        public LatencyMeasurement(Long emission) {
            this.emission = emission;
        }

        public Long getEmission() {
            return emission;
        }

        public void setDelivery(Long delivery) {
            this.delivery = delivery;
        }

        public Long getDelivery() {
            return delivery;
        }

        public Long getLatency() {
            return this.delivery - this.emission;
        }

        private static class Key {
            private final byte[] tx;

            public Key(byte[] tx) {
                this.tx = tx;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Key key = (Key) o;
                return Arrays.equals(tx, key.tx);
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(tx);
            }
        }
    }
}
