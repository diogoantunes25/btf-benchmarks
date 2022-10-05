package pt.ulisboa.tecnico.thesis.benchmarks.master.domain;

public class Commit {
	private long start;
	private long finish;
	public Commit(long start, long finish) {
		this.start = start; this.finish = finish;
	}

	public long getStart() {return start;}
	public long getFinish() {return finish;}
}
