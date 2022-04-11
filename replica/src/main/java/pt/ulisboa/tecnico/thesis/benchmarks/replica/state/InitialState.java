package pt.ulisboa.tecnico.thesis.benchmarks.replica.state;

import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;

import java.util.Set;

public class InitialState implements BenchmarkState {

    @Override
    public void setTopology(Set<Replica> replicas, GroupKey groupKey, KeyShare keyShare, int tolerance) {

    }

    @Override
    public void setProtocol(Protocol protocol, Integer batchSize) {
        // not possible
    }

    @Override
    public void runBenchmark(Integer numRequests, Integer payloadSize) {
        // not possible
    }
}
