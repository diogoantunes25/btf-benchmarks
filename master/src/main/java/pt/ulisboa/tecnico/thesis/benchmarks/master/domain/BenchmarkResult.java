package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import java.util.List;

public class BenchmarkResult {

    private Integer replicaId;
    private Long startTime;
    private Long finishTime;
    private Long sentMessageCount;
    private Long recvMessageCount;
    private Long totalTx;
    private Long droppedTx;

    public BenchmarkResult() {
    }

    public BenchmarkResult(
            Integer replicaId,
            Long startTime,
            Long finishTime,
            Long sentMessageCount,
            Long recvMessageCount,
            Long totalTx,
            Long droppedTx
    ) {
        this.replicaId = replicaId;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.sentMessageCount = sentMessageCount;
        this.recvMessageCount = recvMessageCount;
        this.totalTx = totalTx;
        this.droppedTx = droppedTx;
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

    public Long getTotalTx() {
        return totalTx;
    }

    public long getDroppedTx() {
        return droppedTx;
    }
}
