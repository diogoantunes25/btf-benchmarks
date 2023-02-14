package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.ProtocolMessage;
import pt.tecnico.ulisboa.hbbft.Step;
import pt.tecnico.ulisboa.hbbft.TargetedMessage;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.alea.Alea;
import pt.tecnico.ulisboa.hbbft.binaryagreement.BinaryAgreementMessage;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Reporter;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Confirmation;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkReplica {

    private final Logger logger = LoggerFactory.getLogger(BenchmarkReplica.class);
    private IAtomicBroadcast protocol;
    private MessageEncoder<String> encoder;
    private TcpTransport transport;
    private final long maxPending;
    private Map<Transaction, Confirmation> pending;
    private AtomicLong dropped = new AtomicLong();
    private AtomicLong received = new AtomicLong();
    private AtomicLong confirmed = new AtomicLong();

    public BenchmarkReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport,
                            int load, int batchSize) {
        this.protocol = protocol;
        this.encoder = encoder;
        this.transport = transport;
        this.pending = new ConcurrentHashMap<>();

        // start listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(this);
        }

        this.maxPending = (load > batchSize ? load : batchSize) * 60;
    }

    public void start(boolean first) {
        this.protocol.reset();
    }

    public void stop() {
        this.protocol.stop();

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }
    }

    synchronized public void handleMessage(String data) {
        ProtocolMessage message = this.encoder.decode(data);

        if (message != null) {
            Step<Block> step = this.protocol.handleMessage(message);
            this.handleStep(step);
        }
    }

    /**
     * Takes protocol step and outputs what there's to output (the protocol will resume when replies arrive)
     * @param step
     */
    public void handleStep(Step<Block> step) {
        for (TargetedMessage message: step.getMessages()) {
            String encoded = this.encoder.encode(message.getContent());

            final int cid = (protocol instanceof Alea && message.getContent() instanceof BinaryAgreementMessage) ? 1 : 0;
            for (Integer target: message.getTargets()) {
                this.transport.sendToReplica(target, cid, encoded);
            }
        }

        // handle step outputs
        for (Block block: step.getOutput()) {
            this.deliver(block);
        }
    }

    public void deliver(Block block) {
        for (byte[] entry: block.getEntries()) {
            Transaction t = new Transaction(entry);
            if (pending.containsKey(t)) {
                pending.remove(t).confirm(true);
                confirmed.incrementAndGet();
            }
        }
    }

    public void submit(byte[] payload, Confirmation confirmation) {
        received.incrementAndGet();

//        if (pending.size() > maxPending) {
//            dropped.incrementAndGet();
//            confirmation.confirm(false);
//        }

        pending.put(new Transaction(payload), confirmation);
        handleStep(this.protocol.handleInput(payload));
    }

    public long getReceivedAndReset() {
        return received.getAndSet(0);
    }

    public long getConfirmedAndReset() {
        return confirmed.getAndSet(0);
    }

    public long getDroppedAndReset() {
        return dropped.getAndSet(0);
    }

    public double getBufferOccupancy() {
        return (double) this.pending.size() / (double) this.maxPending;
    }

    /**
     * Wrapper on byte[]. byte[] can't be used in hashmap because hashmap first uses the hash and then equal to compare
     * arrays (which means that arrays will only match if they are the same object).
     */
    class Transaction {
        private byte[] contents;

        public Transaction(byte[] contents) {
            this.contents = contents;
        }

        public byte[] content() {
            return contents;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof Transaction) {
                Transaction t = (Transaction) obj;
                byte[] otherContents = t.content();
                if (this.contents.length != otherContents.length) { return false; }

                for (int i = 0; i < this.contents.length; i++) {
                    if (otherContents[i] != contents[i]) {
                        return false;
                    }
                }

                return true;
            }
            return false;
        }

        public int hashCode() {
            return Arrays.hashCode(contents);
        }

        public String toString() {
            return contents.toString();
        }
    }
}
