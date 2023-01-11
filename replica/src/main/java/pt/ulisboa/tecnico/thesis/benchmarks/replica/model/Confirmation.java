package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

@FunctionalInterface
public interface Confirmation {
    void confirm(boolean ok);
}
