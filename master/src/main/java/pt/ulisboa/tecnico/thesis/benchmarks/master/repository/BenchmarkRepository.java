package pt.ulisboa.tecnico.thesis.benchmarks.master.repository;

import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Benchmark;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BenchmarkRepository {

    public Map<Integer, Benchmark> benchmarks = new HashMap<>();

    public Benchmark get(Integer benchmarkId) {
        return benchmarks.computeIfAbsent(benchmarkId, Benchmark::new);
    }

    public Collection<Benchmark> getAll() {
        return benchmarks.values();
    }

    public void put(Benchmark benchmark) {
        benchmarks.put(benchmark.getId(), benchmark);
    }
}
