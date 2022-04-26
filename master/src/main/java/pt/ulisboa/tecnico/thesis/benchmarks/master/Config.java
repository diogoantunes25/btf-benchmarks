package pt.ulisboa.tecnico.thesis.benchmarks.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Config {

    private static final String DEFAULT_RESOURCES_SERVER  = "http://9582-2001-8a0-7f73-2101-85fe-9c98-6ffe-a466.ngrok.io";

    private final String resourcesServer;
    private final String masterAddr;

    /**
     * Builds a config from a list of string arguments.
     * If no argument is passed, the resourcesServer is set to default
     * If only one argument is passed, the masterAddr is set to current IP
     * @param args arguments from which to set the config
     * @return Config built from arguments
     */
    public static Config fromArgs(String[] args) {
        String resourcesServer = (args.length >= 1) ? args[0] : DEFAULT_RESOURCES_SERVER;
        String masterAddr = null;
        if (args.length < 2) {
            try {
                URL whatsMyIp = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatsMyIp.openStream()));
                masterAddr = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            masterAddr = args[1];
        }
        return new Config(resourcesServer, masterAddr);
    }

    public Config(String resourcesServer, String masterAddr) {
        this.resourcesServer = resourcesServer;
        this.masterAddr = masterAddr;
    }

    public String getResourcesServer() {
        return resourcesServer;
    }

    public String getMasterAddr() {
        return masterAddr;
    }
}
