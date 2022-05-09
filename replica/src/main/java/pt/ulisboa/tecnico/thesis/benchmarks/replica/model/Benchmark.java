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

    public static class Builder {
        private long startTime;
        private long finishTime = 0L;
        private long sentMessageCount = 0L;
        private long recvMessageCount = 0L;
        private List<Measurement> measurements = new ArrayList<>();
        private List<Execution> executions = new ArrayList<>();

        public Builder(long startTime) {
            this.startTime = startTime;
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

            return benchmark;
        }
    }

    private Benchmark() {}

    public Benchmark(long start, List<Measurement> measurements, List<Execution> executions, long finish) {
        this.startTime = start;
        this.finishTime = finish;
        this.measurements = measurements;
        this.executions = executions;
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
}