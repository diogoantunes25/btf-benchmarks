package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

import pt.tecnico.ulisboa.hbbft.utils.threshsig.GroupKey;
import pt.tecnico.ulisboa.hbbft.utils.threshsig.KeyShare;

import java.util.List;
import java.util.stream.Collectors;

public class Topology {

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

    public List<Integer> getReplicaIds() {
        return replicas.stream().map(Replica::getReplicaId).collect(Collectors.toList());
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
