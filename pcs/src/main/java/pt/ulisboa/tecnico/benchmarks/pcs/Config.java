package pt.ulisboa.tecnico.benchmarks.pcs;

import java.net.URI;

public class Config {

    private final URI masterUri;

    public static Config fromArgs(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java Pcs <masterUri>");
            System.exit(-1);
        }

        URI masterUri = URI.create(args[0]);

        return new Config(masterUri);
    }

    public Config(URI masterUri) {
        this.masterUri = masterUri;
    }

    public URI getMasterUri() {
        return masterUri;
    }
}
