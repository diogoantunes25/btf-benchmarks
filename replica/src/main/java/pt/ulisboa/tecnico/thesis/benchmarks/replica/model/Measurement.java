package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

import pt.tecnico.ulisboa.hbbft.abc.Block;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class Measurement {

    private final long timestamp;
    private final long value;

    private final Long blockNumber;
    private final Collection<Integer> proposers;

    public Measurement(Block block) {
        this.timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        this.value = block.getEntries().size();
        this.blockNumber = block.getNumber();
        this.proposers = block.getProposers();
    }

    public Measurement(long timestamp, long value) {
        this.timestamp = timestamp;
        this.value = value;

        this.blockNumber = 0L;
        this.proposers = new ArrayList<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getValue() {
        return value;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public Collection<Integer> getProposers() {
        return proposers;
    }
}
