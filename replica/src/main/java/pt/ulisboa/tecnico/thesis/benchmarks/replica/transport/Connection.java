package pt.ulisboa.tecnico.thesis.benchmarks.replica.transport;

import org.slf4j.Logger;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.replica.BenchmarkReplica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection {

    private final Integer cid;
    private final Replica me;
    private final Replica other;

    private Socket socket;
    private DataOutputStream socketOutStream;
    private DataInputStream socketInStream;

    private final Lock connectLock = new ReentrantLock();
    private final Lock sendLock = new ReentrantLock();

    private BlockingDeque<byte[]> pendingQueue = new LinkedBlockingDeque<>();
    private Thread listenerThread;
    private Thread workerThread;
    private AtomicReference<BenchmarkReplica> listener = new AtomicReference<>();


    private Logger logger;

    public Connection(Replica me, Replica other, Logger logger) {
        this(0, me, other, logger);
    }

    public Connection(Integer cid, Replica me, Replica other, Logger logger) {
        this.cid = cid;
        this.me = me;
        this.other = other;
        this.logger = logger;

        if (this.shouldConnect()) {
            connect();
        }
        else {
            logger.info("Aborted connection to replica {} on channel {}.", me.getId(), other.getId());
        }
    }

    public Boolean shouldConnect() {
        return me.getId() < other.getId();
    }

    public void setListener(BenchmarkReplica replica) {

        pendingQueue.clear();
        this.listener.set(replica);

        if (this.listenerThread == null || !this.listenerThread.isAlive()) {
            this.listenerThread = new Thread(() -> {
                while (true) {
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
                        pendingQueue.put(data);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        System.out.println("Closing socket.");
                        disconnect();
                        break;
                    }
                }
            });
            this.listenerThread.setDaemon(true);
            this.listenerThread.start();
        }

        if (this.workerThread != null) {
            try {
                this.workerThread.interrupt();
                this.workerThread = null;
            }
            catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        if (this.workerThread == null || !this.workerThread.isAlive()) {
            this.workerThread = new Thread(() -> {
               while (!Thread.currentThread().isInterrupted()) {
                   try {
                       // get message from queue
                       byte[] data = pendingQueue.take();

                       // process message
                       BenchmarkReplica listener_replica = listener.get();
                       if (listener_replica != null) {
                           listener_replica.handleMessage(new String(data));
                       }

                   } catch (InterruptedException e) {
                       // e.printStackTrace();
                       Thread.currentThread().interrupt();
                       break;
                   }
               }
            });
            this.workerThread.setDaemon(true);
            this.workerThread.start();
        }
    }

    private void connect() {
        try {
            // int localPort = cid * 100 + 10 * me.getId() + other.getId() + 18000;
            // logger.info("[{}] Opening socket to {}:{}, from port {}", System.currentTimeMillis(), other.getAddress(), other.getPort(), localPort);
            //this.socket = new Socket(other.getAddress(), other.getPort(), null, localPort);
            this.socket = new Socket(other.getAddress(), other.getPort());
            // logger.info("[{} The socket port at host is {}]", System.currentTimeMillis(), this.socket.getLocalPort());
            this.socketOutStream = new DataOutputStream(socket.getOutputStream());
            this.socketInStream = new DataInputStream(socket.getInputStream());
            this.socketOutStream.writeInt(me.getId());
            this.socketOutStream.writeInt(cid);
            logger.info("[{}] Socket to {}:{}, channel {} opened - {}", System.currentTimeMillis(), other.getAddress(), other.getId(), cid, socket.isConnected());
        } catch (IOException e) {
            logger.info("[{}] Socket to {}:{} failed, trying again in 0.1 seconds", System.currentTimeMillis(), other.getAddress(), other.getPort());
            e.printStackTrace();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
            this.connect();
        }
    }

    public void connect(Socket newSocket) {
        if (socket == null || !socket.isConnected()) {
            this.socket = newSocket;
            try {
                this.socketOutStream = new DataOutputStream(socket.getOutputStream());
                this.socketInStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("Failed to authenticate to replica");
            }
        }
    }

    public void disconnect() {
        connectLock.lock();

        try {
            if (socket != null) {
                socketOutStream.flush();
                socketOutStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            connectLock.unlock();
        }
    }

    public void send(byte[] data) {
        sendLock.lock();
        try {
            this.socketOutStream.writeInt(data.length);
            this.socketOutStream.write(data);
        } catch (IOException e) {
            disconnect();
        } finally {
            sendLock.unlock();
        }
    }

    public void printStatus(Logger logger) {
        logger.info("[{}] from {}:{} to {}:{} - {}", System.currentTimeMillis(),
                me.getAddress(), me.getPort(), other.getAddress(), other.getPort(),
                socket.isConnected());
    }

}
