package pt.ulisboa.tecnico.thesis.benchmarks.replica.state;

import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;

import java.util.Set;

public interface BenchmarkState {
    void setTopology(Set<Replica> replicas, GroupKey groupKey, KeyShare keyShare, int tolerance);
    void setProtocol(Protocol protocol, Integer batchSize);
    void runBenchmark(Integer numRequests, Integer payloadSize);
}
