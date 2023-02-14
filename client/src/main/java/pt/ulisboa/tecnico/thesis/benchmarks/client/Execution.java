package pt.ulisboa.tecnico.thesis.benchmarks.client;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

/**
 * Execution information off interaction between replica and client during time period
 */
public class Execution {
    private static final String INTERFACE = "enp0s8";
    public int replicaId;
    public long start;
    public long end;
    public long txs;
    public long dropped;
    public double latency;
    public double cpu;
    public double bandwithIn;
    public double bandwithOut;
    public double freeMemory;
    public double totalMemory;
    public static final OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();

    public Execution(int replicaId, long start, long end, long txs, long dropped, double latency, double cpu, double bandwidthIn,
                     double bandwidthOut, double freeMemory, double totalMemory) {
        this.replicaId = replicaId;
        this.start = start;
        this.end = end;
        this.txs = txs;
        this.dropped = dropped;
        this.latency = latency;
        this.cpu = cpu;
        this.bandwithIn = bandwidthIn;
        this.bandwithOut = bandwidthOut;
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
    }

    public static Execution build(int replicaId, long start, long end, long txs, long dropped, double latency) {
        try {
            double cpu, in, out, freeMemory, totalMemory;
            String line;
            ProcessBuilder pb = new ProcessBuilder("ifstat", "-n", "-i", INTERFACE, "0.1", "1");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Ignore first two lines of output
            reader.readLine();
            reader.readLine();
            cpu = bean.getSystemLoadAverage();

            // Measure bandwidth
            line = reader.readLine();
            String[] things = line.strip().split(" ");
            try {
                in = Double.parseDouble(things[0]);
            } catch (NumberFormatException e) {
                in = 0;
            }
            try {
                out = Double.parseDouble(things[things.length-1]);
            } catch (NumberFormatException e) {
                out = 0;
            }

            // Measure memory usage
            freeMemory = Runtime.getRuntime().freeMemory();
            totalMemory = Runtime.getRuntime().totalMemory();

            return new Execution(replicaId, start, end, txs, dropped, latency, cpu, in, out, freeMemory, totalMemory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
