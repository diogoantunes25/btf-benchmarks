package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class Config {

    private static final String DEFAULT_MASTER_URI = "http://127.0.0.1:8080";

    private final int replicaId;
    private final URI masterUri;

    private final InetAddress ip;

    /**
     * Creates a Config from list of arguments
     * @param args Replica ID and master URI (optional)
     * @return
     */
    public static Config fromArgs(String[] args) throws UnknownHostException {
        if (args.length < 1) {
            System.out.println("Use: java -jar replica.jar <replicaId> [masterUri]");
            System.exit(-1);
        }

        int replicaId = Integer.parseInt(args[0]);
        URI masterUri = URI.create(args.length < 2 ? DEFAULT_MASTER_URI : args[1]);
        InetAddress ip = InetAddress.getByName(args[2]);

        return new Config(replicaId, masterUri, ip);
    }

    public Config(int replicaId, URI masterUri, InetAddress ip) {
        this.replicaId = replicaId;
        this.masterUri = masterUri;
        this.ip = ip;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public URI getMasterUri() {
        return masterUri;
    }

    public InetAddress getPcsIP() {
        return ip;
    }
}
