package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

/**
 * Summary of information about execution during a period of time.
 * */
public class Summary {

    /**
     *  Time period starts at
     */
    private final Long start;

    /**
     * Time period ends at
     */
    private final Long finish;

	/**
	 * Number of transactions committed during period
	 * */
	private final Long txCommitted;

	/**
	 * Average latency measured during period (tx/sec)
	 * */
	private final float avgLatency;

	private final double cpuLoad;

	private final double inBandwidth;
	private final double outBandwidth;

	public Summary(Long start, Long finish, Long txCommitted, float avgLatency, double cpuLoad, double inBandwidth,
				   	double outBandwidth) {
        this.start = start;
        this.finish = finish;
		this.txCommitted = txCommitted;
		this.avgLatency = avgLatency;
		this.cpuLoad = cpuLoad;
		this.inBandwidth = inBandwidth;
		this.outBandwidth = outBandwidth;
    }

    public Long getStart() {
        return start;
    }

    public Long getFinish() {
        return finish;
    }

	public float getAvgLatency() {
		return avgLatency;
	}

	public long getTxCommitted() {
		return txCommitted;
	}

	public double getCpuLoad() { return cpuLoad; }
	public double getInBandwidth() { return inBandwidth; }
	public double getOutBandwidth() { return outBandwidth; }
}
