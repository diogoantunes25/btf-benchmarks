package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import java.util.List;

public class BenchmarkResult {

    private Integer replicaId;
    private Long startTime;
    private Long finishTime;
    private Long sentMessageCount;
    private Long recvMessageCount;

    public BenchmarkResult() {
    }

    public BenchmarkResult(
            Integer replicaId,
            Long startTime,
            Long finishTime,
            Long sentMessageCount,
            Long recvMessageCount
    ) {
        this.replicaId = replicaId;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.sentMessageCount = sentMessageCount;
        this.recvMessageCount = recvMessageCount;
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
}
