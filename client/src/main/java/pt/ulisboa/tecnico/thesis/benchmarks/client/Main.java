package pt.ulisboa.tecnico.thesis.benchmarks.client;

import pt.ulisboa.tecnico.thesis.benchmarks.client.exceptions.ReplicasUnknownException;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String args[]) {
        Client client = new Client(1000);

        Map<Integer, String> replicas = new HashMap<>();
        replicas.put(0, "127.0.0.1");
        replicas.put(1, "127.0.0.2");

        client.setReplicas(replicas);
        try {
            client.start(2);
            Thread.sleep(10000);
            client.stop();
        } catch (ReplicasUnknownException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
