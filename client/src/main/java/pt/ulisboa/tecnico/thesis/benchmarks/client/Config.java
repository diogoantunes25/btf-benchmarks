package pt.ulisboa.tecnico.thesis.benchmarks.client;

import java.net.URI;

public class Config {

    private String masterIP;

    private Config(String masterIP) {
        this.masterIP = masterIP;
    }

    public static Config fromArgs(String[] args) {
        if (args.length < 1) {
            System.out.println("Use: java client <masterIP>");
            System.exit(-1);
        }

        String masterIP = args[0];

        return new Config(masterIP);
    }

    public String getMasterIP() {
        return masterIP;
    }

}
