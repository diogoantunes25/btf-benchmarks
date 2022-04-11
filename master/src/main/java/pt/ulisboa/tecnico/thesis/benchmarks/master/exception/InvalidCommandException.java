package pt.ulisboa.tecnico.thesis.benchmarks.master.exception;

public class InvalidCommandException extends Exception {
    public InvalidCommandException(String command) {
        super(command);
    }
}
