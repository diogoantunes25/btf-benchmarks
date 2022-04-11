package pt.ulisboa.tecnico.thesis.benchmarks.master.repository;

import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Pcs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PcsRepository {

    private final Map<String, Pcs> instances = new HashMap<>();

    public PcsRepository() {
        instances.put("pcs-1", new Pcs("pcs-1", "localhost", 8844));
    }

    public void add(Pcs pcs) {
        instances.put(pcs.getName(), pcs);
    }

    public Pcs get(String name) {
        return instances.get(name);
    }

    public Collection<Pcs> getAll() {
        return instances.values();
    }
}
