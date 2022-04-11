package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import pt.tecnico.ulisboa.hbbft.Transport;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TcpTransportOld implements Transport<String> {

    private final Map<Integer, Connection> connections;
    private final Integer replicas;

    public TcpTransportOld(Map<Integer, Connection> connections, Integer replicas) {
        this.connections = connections;
        this.replicas = replicas;
    }

    public Collection<Connection> getConnections() {
        return this.connections.values();
    }

    @Override
    public int countKnownReplicas() {
        return this.replicas;
    }

    @Override
    public Collection<Integer> knownReplicaIds() {
        return Stream.iterate(0, n -> n + 1).limit(this.replicas).collect(Collectors.toList());
    }

    @Override
    public void sendToReplica(int replicaId, String data) {
        Connection connection = this.connections.get(replicaId);
        connection.send(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendToClient(int clientId, String data) {
        // TODO
    }

    @Override
    public void multicast(String data, int... ignoredReplicas) {
        Set<Integer> ignored = new HashSet<>(ignoredReplicas.length);
        for (int id : ignoredReplicas) {
            ignored.add(id);
        }

        for (int i = 0; i < this.replicas; i++) {
            if (!ignored.contains(i)) {
                this.sendToReplica(i, data);
            }
        }
    }

    public static class Connection {

        private final Integer replicaId;
        private final Replica remote;

        private Socket socket;
        private DataOutputStream socketOutStream;
        private DataInputStream socketInStream;

        private final Lock connectLock = new ReentrantLock();
        private final Lock sendLock = new ReentrantLock();

        private SocketReader socketReader;
        private Thread receiverThread;

        // private final AtomicBoolean working = new AtomicBoolean(true);

        public Connection(Integer replicaId, Replica remote) {
            this.replicaId = replicaId;
            this.remote = remote;

            // Connect to the remote process or wait for the connection
            if (isToConnect()) connect();

            if (this.socket != null) {
                try {
                    this.socketOutStream = new DataOutputStream(this.socket.getOutputStream());
                    this.socketInStream = new DataInputStream(this.socket.getInputStream());
                } catch (IOException e) {
                    System.out.println("Error creating connection to " + remote.getId());
                }
            }
        }

        public boolean isToConnect() {
            return replicaId < remote.getId();
        }

        public void connect() {
            try {
                this.socket = new Socket(remote.getAddress(), remote.getPort());
                new DataOutputStream(this.socket.getOutputStream()).writeInt(replicaId);
                System.out.println("Connected to " + remote.getId());
            } catch (IOException e) {
                this.waitAndConnect();
            }
        }

        public void send(byte[] data) {
            sendLock.lock();

            boolean abort = false;
            do {
                // If there is a need to reconnect, abort this method
                if (abort) break;

                if (socket != null && socketOutStream != null) {
                    try {
                        socketOutStream.writeInt(data.length);
                        socketOutStream.write(data);
                        break;

                    } catch (IOException e) {
                        closeSocket();
                        waitAndConnect();
                        abort = true;
                    }

                } else {
                    System.out.println("CANT SEND TO " + remote.getId());
                    waitAndConnect();
                    abort = true;
                }
            } while (true);

            sendLock.unlock();
        }

        private void waitAndConnect() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            reconnect(null);
        }

        public void reconnect(Socket newSocket) {
            connectLock.lock();

            if (socket == null || !socket.isConnected()) {
                if (isToConnect()) connect();
                else socket = newSocket;

                if (socket != null) {
                    try {
                        socketOutStream = new DataOutputStream(socket.getOutputStream());
                        socketInStream = new DataInputStream(socket.getInputStream());
                    } catch (IOException e) {
                        System.out.println("Failed to authenticate to replica");
                    }
                }
            }

            connectLock.unlock();
        }

        private void closeSocket() {
            connectLock.lock();

            if (socket != null) {
                try {
                    socketOutStream.flush();
                    socketOutStream.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket to " + remote.getId());
                } catch (NullPointerException e) {
                    System.out.println("Socket already closed");
                }
            }

            socket = null;
            socketOutStream = null;
            socketInStream = null;

            connectLock.unlock();
        }

        /*public void startListener(BenchmarkReplica replica) {
            if (this.receiverThread != null && this.receiverThread.isAlive()) return;

            this.receiverThread = new Thread(() -> {
                while (true) {
                    if (socket != null && socketInStream != null) {
                        try {
                            // read data length
                            int dataLength = socketInStream.readInt();
                            byte[] data = new byte[dataLength];

                            // read data
                            int read = 0;
                            do {
                                read += socketInStream.read(data, read, dataLength - read);
                            } while (read < dataLength);

                            // Pass message to replica
                            replica.handleMessage(new String(data));

                        } catch (IOException ex) {
                            System.out.println("Closing socket and reconnecting");
                            closeSocket();
                            waitAndConnect();
                        }
                    }
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();
        }*/

        public void startListener(BenchmarkReplica replica) {
            if (this.receiverThread != null && this.receiverThread.isAlive()) return;

            this.socketReader = new SocketReader(replica);

            this.receiverThread = new Thread(this.socketReader);
            this.receiverThread.setDaemon(true);
            this.receiverThread.start();
        }

        public void shutdown() {
            if (this.socketReader != null) this.socketReader.stop();
            this.closeSocket();
            try {
                if (this.receiverThread != null) this.receiverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private class SocketReader implements Runnable {

            private final BenchmarkReplica replica;
            private final AtomicBoolean running = new AtomicBoolean(false);

            public SocketReader(BenchmarkReplica replica) {
                this.replica = replica;
            }

            @Override
            public void run() {
                running.set(true);
                while (running.get()) {
                    if (socket != null && socketInStream != null) {
                        try {
                            // read data length
                            int dataLength = socketInStream.readInt();
                            byte[] data = new byte[dataLength];

                            // read data
                            int read = 0;
                            do {
                                read += socketInStream.read(data, read, dataLength - read);
                            } while (read < dataLength);

                            // Pass message to replica
                            replica.handleMessage(new String(data));

                        } catch (IOException ex) {
                            // System.out.println("Closing socket and reconnecting");
                            System.out.println("Closing socket.");
                            closeSocket();
                            // waitAndConnect();
                        }
                    }
                }
            }

            public void stop() {
                running.set(false);
            }
        }
    }
}
