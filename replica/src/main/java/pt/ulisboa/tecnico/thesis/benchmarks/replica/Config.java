package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import java.net.URI;

public class Config {

    private static final String DEFAULT_MASTER_URI = "http://127.0.0.1:8080";

    private final int replicaId;
    private final URI masterUri;

    /**
     * Creates a Config from list of arguments
     * @param args Replica ID and master URI (optional)
     * @return
     */
    public static Config fromArgs(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java MainTcp <replicaId> [masterUri]");
            System.exit(-1);
        }

        int replicaId = Integer.parseInt(args[0]);
        URI masterUri = URI.create(args.length < 2 ? DEFAULT_MASTER_URI : args[1]);

        return new Config(replicaId, masterUri);
    }

    public Config(int replicaId, URI masterUri) {
        this.replicaId = replicaId;
        this.masterUri = masterUri;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public URI getMasterUri() {
        return masterUri;
    }
}
