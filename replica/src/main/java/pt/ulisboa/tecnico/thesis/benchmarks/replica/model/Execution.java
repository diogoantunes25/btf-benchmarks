package pt.ulisboa.tecnico.thesis.benchmarks.replica.model;

public class Execution {
    private final String pid;
    private final Long start;
    private final Long finish;
    private final Boolean result;

    public Execution(String pid, Long start, Long finish, Boolean result) {
        this.pid = pid;
        this.start = start;
        this.finish = finish;
        this.result = result;
    }

    public String getPid() {
        return pid;
    }

    public Long getStart() {
        return start;
    }

    public Long getFinish() {
        return finish;
    }

    public Boolean getResult() {
        return result;
    }
}
