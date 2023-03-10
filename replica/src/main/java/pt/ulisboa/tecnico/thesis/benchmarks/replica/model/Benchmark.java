package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    private long startTime;
    private long finishTime;
    private long sentMessageCount;
    private long recvMessageCount;
    private List<Measurement> measurements;
    private List<Execution> executions;

    private long txSubmitted;
    private long txDropped;

    public static class Builder {
        private long startTime;
        private long finishTime = 0L;
        private long sentMessageCount = 0L;
        private long recvMessageCount = 0L;
        private List<Measurement> measurements = new ArrayList<>();
        private List<Execution> executions = new ArrayList<>();

        private long txSubmitted = 0;

        private long txDropped = 0;

        public Builder(long startTime) {
            this.startTime = startTime;
        }

        public Builder txSubmitted(long txSubmitted) {
            this.txSubmitted = txSubmitted;
            return this;
        }

        public Builder txDropped(long txDropped) {
            this.txDropped = txDropped;
            return this;
        }

        public Builder finishTime(long finishTime) {
            this.finishTime = finishTime;
            return this;
        }

        public Builder sentMessageCount(long sentMessageCount) {
            this.sentMessageCount = sentMessageCount;
            return this;
        }

        public Builder recvMessageCount(long recvMessageCount) {
            this.recvMessageCount = recvMessageCount;
            return this;
        }

        public Builder measurements(List<Measurement> measurements) {
            this.measurements = measurements;
            return this;
        }

        public Builder executions(List<Execution> executions) {
            this.executions = executions;
            return this;
        }

        public Benchmark build() {
            Benchmark benchmark = new Benchmark();

            benchmark.startTime = this.startTime;
            benchmark.finishTime = this.finishTime;
            benchmark.sentMessageCount = this.sentMessageCount;
            benchmark.recvMessageCount = this.recvMessageCount;
            benchmark.measurements = this.measurements;
            benchmark.executions = this.executions;
            benchmark.txDropped = this.txDropped;
            benchmark.txSubmitted = this.txSubmitted;

            return benchmark;
        }
    }

    private Benchmark() {}

    public Benchmark(long start, List<Measurement> measurements, List<Execution> executions, long finish,
                     long txSubmitted, long txDropped) {
        this.startTime = start;
        this.finishTime = finish;
        this.measurements = measurements;
        this.executions = executions;
        this.txSubmitted = txSubmitted;
        this.txDropped = txDropped;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getSentMessageCount() {
        return sentMessageCount;
    }

    public long getRecvMessageCount() {
        return recvMessageCount;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public List<Execution> getExecutions() {
        return executions;
    }

    public long getTxSubmitted() { return txSubmitted; }
    public long getTxDropped() { return txDropped; }
}