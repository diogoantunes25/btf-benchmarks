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
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BenchmarkReplica {

    protected final Logger logger = LoggerFactory.getLogger(BenchmarkReplica.class);

    protected IAtomicBroadcast protocol;
    protected MessageEncoder<String> encoder;
    protected TcpTransport transport;

    protected final AtomicLong sentMessageCount = new AtomicLong();
    protected final AtomicLong recvMessageCount = new AtomicLong();

    protected final List<Long> encodingTimes = Collections.synchronizedList(new ArrayList<>());
    protected final List<Long> decodingTimes = Collections.synchronizedList(new ArrayList<>());

    public BenchmarkReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport) {
        this.protocol = protocol;
        this.encoder = encoder;
        this.transport = transport;
    }

    public abstract void start();

    public abstract Benchmark stop();

    synchronized public void handleMessage(String data) {
        this.recvMessageCount.incrementAndGet();

        final long start = ZonedDateTime.now().toInstant().toEpochMilli();
        ProtocolMessage message = this.encoder.decode(data);
        final long end = ZonedDateTime.now().toInstant().toEpochMilli();
        this.decodingTimes.add(end - start);

        if (message != null) {
            Step<Block> step = this.protocol.handleMessage(message);
            this.handleStep(step);
        }
    }

    public void handleStep(Step<Block> step) {
        // send messages generated during protocol execution
        for (TargetedMessage message: step.getMessages()) {
            final long start = ZonedDateTime.now().toInstant().toEpochMilli();
            String encoded = this.encoder.encode(message.getContent());
            final long end = ZonedDateTime.now().toInstant().toEpochMilli();
            this.encodingTimes.add(end - start);

            final int cid = (protocol instanceof Alea && message.getContent() instanceof BinaryAgreementMessage) ? 1 : 0;
            this.sentMessageCount.addAndGet(message.getTargets().size());
            for (Integer target: message.getTargets()) {
                this.transport.sendToReplica(target, cid, encoded);
            }
        }

        // handle step outputs
        for (Block block: step.getOutput()) {
            this.deliver(block);
        }
    }

    public abstract void deliver(Block block);
}
