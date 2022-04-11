package pt.ulisboa.tecnico.benchmarks.pcs;

import java.net.URI;

public class Config {

    private final int port;
    private final URI masterUri;

    public static Config fromArgs(String[] args) {
        if (args.length < 2) {
            System.out.println("Use: java Pcs <port> <masterUri>");
            System.exit(-1);
        }

        int port = Integer.parseInt(args[0]);
        URI masterUri = URI.create(args[1]);

        return new Config(port, masterUri);
    }

    public Config(int port, URI masterUri) {
        this.port = port;
        this.masterUri = masterUri;
    }

    public int getPort() {
        return port;
    }

    public URI getMasterUri() {
        return masterUri;
    }
}
