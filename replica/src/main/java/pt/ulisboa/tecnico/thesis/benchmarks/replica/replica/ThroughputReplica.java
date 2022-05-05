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
import java.util.stream.Collectors;

public class ThroughputReplica extends BenchmarkReplica {

    private final Logger logger = LoggerFactory.getLogger(ThroughputReplica.class);

    private long startTime;
    private final List<Measurement> measurements = new ArrayList<>();

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
        Step<Block> step = this.protocol.handleInput(new byte[0]);
        this.handleStep(step);
    }

    public Benchmark stop() {
        System.out.println("[ThroughputReplica.stop] started");
        final long finishTime = ZonedDateTime.now().toInstant().toEpochMilli();

        // stop listeners
        for (Connection connection: this.transport.getConnections()) {
            connection.setListener(null);
        }

        List<Execution> executions = new ArrayList<>();
        if (this.protocol instanceof Alea) {

            System.out.println("[getExecutionLog.stop] called 1");

            System.out.println((Alea) this.protocol);

            System.out.println("[getExecutionLog.stop] call ended 1");

            System.out.println("[getExecutionLog.stop] called 2");
            executions = ((Alea) this.protocol).getExecutionLog().getChildren().stream()
                    .map(e -> new Execution(e.getPid(), e.getStart(), e.getFinish(), e.getResult() instanceof Boolean ? (Boolean) e.getResult() : false)).collect(Collectors.toList());
            System.out.println("[getExecutionLog.stop] call ended 2");
        }

        return new Benchmark.Builder(startTime)
                .finishTime(finishTime)
                .sentMessageCount(this.sentMessageCount.get())
                .recvMessageCount(this.recvMessageCount.get())
                .measurements(measurements)
                .executions(executions)
                .build();
    }

    @Override
    public void deliver(Block block) {
        logger.info("Delivered: {}", block.toString());
        this.measurements.add(new Measurement(block));
    }
}
