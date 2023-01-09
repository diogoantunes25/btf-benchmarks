package pt.ulisboa.tecnico.thesis.benchmarks.master.repository;

import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.Client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClientRepository {
    private final Map<String, Client> instances = new HashMap<>();

    public ClientRepository() {}

    public void add(Client client) {
        instances.put(client.getName(), client);
    }

    public Client get(String name) {
        return instances.get(name);
    }

    public int size() { return instances.size(); }

    public Collection<Client> getAll() {
        return instances.values();
    }
}
