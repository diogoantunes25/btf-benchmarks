package pt.ulisboa.tecnico.thesis.benchmarks.replica.replica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.ulisboa.hbbft.MessageEncoder;
import pt.tecnico.ulisboa.hbbft.Step;
import pt.tecnico.ulisboa.hbbft.abc.Block;
import pt.tecnico.ulisboa.hbbft.abc.IAtomicBroadcast;
import pt.tecnico.ulisboa.hbbft.abc.alea.Alea;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.Protocol;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Benchmark;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Execution;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.model.Measurement;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.Connection;
import pt.ulisboa.tecnico.thesis.benchmarks.replica.transport.TcpTransport;

// FIXME: Remove this
import pt.tecnico.ulisboa.hbbft.abc.alea.benchmark.ExecutionLog;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class ThroughputReplica extends BenchmarkReplica {

    private final Logger logger = LoggerFactory.getLogger(ThroughputReplica.class);

    private long startTime;

    private final Queue<Long> proposeTimes = new PriorityBlockingQueue<>();
    private final List<Measurement> measurements = new ArrayList<>();
    private final List<Execution> executions = new ArrayList<>();

    public ThroughputReplica(IAtomicBroadcast protocol, MessageEncoder<String> encoder, TcpTransport transport) {
        super(protocol, encoder, transport);

        // start listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(this);
        }
    }

    public void start() {   // TODO params
        // log starting time
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();

        // input any random value into the protocol (in benchmark mode)
        logger.info("Handle Input");
        Step<Block> step = this.protocol.handleInput(new byte[0]);
        logger.info("Handle step");
        this.handleStep(step);
    }

    public Benchmark stop() {
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }
        logger.info("setListener finished");

        return new Benchmark(startTime, measurements, executions, finishTime);
    }

//    public Benchmark stop() {
//        System.out.println("[ThroughputReplica.stop] started");
//        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();
//
//        // stop listeners
//        for (Connection connection: this.transport.getConnections()) {
//            connection.setListener(null);
//        }
//
//        List<Execution> executions = new ArrayList<>();
//        if (this.protocol instanceof Alea) {
//
//            executions = ((Alea) this.protocol).getExecutionLog().getChildren().stream().filter(e->e.getFinish() != null)
//                    .map(e -> new Execution(e.getPid(), e.getStart(), e.getFinish(), e.getResult() instanceof Boolean ? (Boolean) e.getResult() : false)).collect(Collectors.toList());
//        }
//
//        return new Benchmark.Builder(startTime)
//                .finishTime(finishTime)
//                .sentMessageCount(this.sentMessageCount.get())
//                .recvMessageCount(this.recvMessageCount.get())
//                .measurements(measurements)
//                .executions(executions)
//                .build();
//    }

    @Override
    public void deliver(Block block) {
        final long timestamp = ZonedDateTime.now().toInstant().toEpochMilli();
        logger.info("Delivered: {}", block.toString());
        this.measurements.add(new Measurement(block));
        long proposeTime = this.proposeTimes.poll();
        this.executions.add(new Execution("node", proposeTime , timestamp, true));
    }
}
