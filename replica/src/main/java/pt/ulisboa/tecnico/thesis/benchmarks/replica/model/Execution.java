package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

public class Execution {
    /**
     *  Time transaction was submitted at
     */
    private final Long start;
    /**
     * Time transaction was confirmed (whether it was submitted by me or not)
     */
    private final Long finish;
    public Execution(Long start, Long finish) {
        this.start = start;
        this.finish = finish;
    }

    public Long getStart() {
        return start;
    }

    public Long getFinish() {
        return finish;
    }
}
