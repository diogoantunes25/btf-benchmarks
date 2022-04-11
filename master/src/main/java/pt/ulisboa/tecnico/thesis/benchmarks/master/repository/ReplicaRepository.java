package pt.ulisboa.tecnico.thesis.benchmarks.master.repository;

import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Replica;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReplicaRepository {

    private final Map<Integer, Replica> replicas = new HashMap<>();

    public void addReplica(Replica replica) {
        replicas.put(replica.getReplicaId(), replica);
    }

    public Replica getReplica(Integer replicaId) {
        return replicas.get(replicaId);
    }

    public Collection<Replica> getAll() {
        return replicas.values();
    }

    private void clear() {
        replicas.clear();
    }
}
