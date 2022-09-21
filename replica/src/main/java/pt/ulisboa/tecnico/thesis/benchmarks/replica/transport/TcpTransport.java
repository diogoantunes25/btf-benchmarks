package pt.ulisboa.tecnico.thesis.benchmarks.replica.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.Transport;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class TcpTransport implements Transport<String> {

    private final Logger logger = LoggerFactory.getLogger(TcpTransport.class);

    private final List<Replica> replicas;
    private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();

    public TcpTransport(Replica me, List<Replica> replicas) {
        this(me, replicas, 1);
    }

    public TcpTransport(Replica me, List<Replica> replicas, Integer numChannels) {
        this.replicas = replicas;
        try {
            init(me, numChannels);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void init(Replica me, Integer numChannels) throws IOException, InterruptedException {
        logger.info("[{}] Running TCP server at port {}.", System.currentTimeMillis(), me.getPort());
        ServerSocket serverSocket = new ServerSocket(me.getPort());

        // active connection
        for (Replica replica: replicas) {
            if (replica.getId() == me.getId()) continue;
            for (int cid=0; cid < numChannels; cid++) {
                int key = cid*replicas.size() + replica.getId();
                connections.put(key, new Connection(cid, me, replica, logger));
                logger.info("[{}] Attempting connection to replica {} on channel {}.", System.currentTimeMillis(), replica.getId(), cid);
            }
        }

        // Thread.sleep(1000); // Wait for ports to setup

        // passive connection
        CountDownLatch connectedLatch = new CountDownLatch((int) connections.values().stream()
                .filter(connection -> !connection.shouldConnect()).count());
        Thread connThread = new Thread(() -> {
            try {
                while (connectedLatch.getCount() > 0) {
                    logger.info("[{}] Missing {} connections.", System.currentTimeMillis(), connectedLatch.getCount());
                    Socket socket = serverSocket.accept();
                    logger.info("[{}] New connection accepted, waiting for info", System.currentTimeMillis());
                    int remoteId = new DataInputStream(socket.getInputStream()).readInt();
                    int cid = new DataInputStream(socket.getInputStream()).readInt();
                    int key = cid * replicas.size() + remoteId;
                    logger.info("[{}] Connection from replica {} on channel {}.", System.currentTimeMillis(), remoteId, cid);

                    if (connections.containsKey(key)) connections.get(key).connect(socket);
                    connectedLatch.countDown();
                }
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        connThread.setDaemon(true);
        connThread.start();

        connectedLatch.await();
    }

    public Collection<Connection> getConnections() {
        return this.connections.values();
    }

    @Override
    public int countKnownReplicas() {
        return this.replicas.size();
    }

    @Override
    public Collection<Integer> knownReplicaIds() {
        return this.replicas.stream().map(Replica::getId).collect(Collectors.toList());
    }

    @Override
    public void sendToReplica(int replicaId, String data) {
        this.sendToReplica(replicaId, 0, data);
    }

    public void sendToReplica(int replica, int cid, String data) {
        int key = cid*countKnownReplicas() + replica;
        try {
            this.connections.get(key).send(data.getBytes(StandardCharsets.UTF_8));
        } catch (NullPointerException e) {
            System.out.printf("Key: %d%n", key);
            System.out.printf("CID: %d%n", cid);
            System.out.printf("Replica: %d%n", replica);
            System.out.println(connections.keySet());
            System.exit(1);
        }
    }

    @Override
    public void sendToClient(int i, String s) {
        // NOP
    }

    @Override
    public void multicast(String data, int... ignoredReplicas) {
        Set<Integer> ignored = new HashSet<>(ignoredReplicas.length);
        for (int id : ignoredReplicas) {
            ignored.add(id);
        }

        for (int i = 0; i < this.countKnownReplicas(); i++) {
            if (!ignored.contains(i)) {
                this.sendToReplica(i, data);
            }
        }
    }
}
