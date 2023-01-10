package pt.ulisboa.tecnico.thesis.benchmarks.master.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.PrimitiveIterator;

public class UpdateRepository {

    private String filename;
    private final String defaultFilename;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Writer writer;

    /**
     *
     * @param defaultFilename File to which updates are written when there's no registered experiment running.
     */
    public UpdateRepository(String defaultFilename) {
        this.filename = defaultFilename;
        this.defaultFilename = defaultFilename;
        try {
            this.writer = new BufferedWriter(new FileWriter(defaultFilename + ".csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unset() {
        set(this.defaultFilename);
    }

    public void set(String filename) {
        this.filename = filename;
        Writer old = this.writer;
        try {
            this.writer = new BufferedWriter(new FileWriter(this.filename + ".csv"));
            old.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClientUpdate(int replicaId, long txs, double latency, long start, long end, double cpu,
                                double bandwidthIn, double bandwidthOut, double freeMemory, double totalMemory) {
        try {
            writer.write(String.format("c,%d,%d,%f,%d,%d,%f,%f,%f,%f,%f", replicaId, txs, latency, start, end,
                    cpu, bandwidthIn, bandwidthOut, freeMemory, totalMemory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addReplicaUpdate(int replicaId, double cpu, double bandwidthIn, double bandwidthOut, double freeMemory,
                                 double totalMemory, long timestamp) {
        try {
            writer.write(String.format("r,%d,%f,%f,%f,%f,%f\n", replicaId, cpu, bandwidthIn, bandwidthOut,
                    freeMemory, totalMemory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}