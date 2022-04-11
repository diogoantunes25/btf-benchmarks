package pt.ulisboa.tecnico.thesis.benchmarks.replica.transport;

import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Replica;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.replica.BenchmarkReplica;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
    private final Lock recvLock = new ReentrantLock();

    private BlockingDeque<byte[]> pendingQueue = new LinkedBlockingDeque<>();
    private Thread listenerThread;
    private Thread workerThread;
    private BenchmarkReplica listener;

    public Connection(Replica me, Replica other) {
        this(0, me, other);
    }

    public Connection(Integer cid, Replica me, Replica other) {
        this.cid = cid;
        this.me = me;
        this.other = other;

        if (this.shouldConnect()) connect();
    }

    public Boolean shouldConnect() {
        return me.getId() < other.getId();
    }

    public void setListener(BenchmarkReplica replica) {
        recvLock.lock();
        this.listener = replica;
        recvLock.unlock();

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

        if (this.workerThread == null || !this.workerThread.isAlive()) {
            this.workerThread = new Thread(() -> {
               while (!Thread.currentThread().isInterrupted()) {
                   try {
                       // get message from queue
                       byte[] data = pendingQueue.take();

                       // process message
                       recvLock.lock();
                       if (listener != null) listener.handleMessage(new String(data));
                       recvLock.unlock();

                   } catch (InterruptedException e) {
                       e.printStackTrace();
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
            this.socket = new Socket(other.getAddress(), other.getPort());
            this.socketOutStream = new DataOutputStream(socket.getOutputStream());
            this.socketInStream = new DataInputStream(socket.getInputStream());
            new DataOutputStream(this.socket.getOutputStream()).writeInt(me.getId());
            new DataOutputStream(this.socket.getOutputStream()).writeInt(cid);

        } catch (IOException e) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {}
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
}
