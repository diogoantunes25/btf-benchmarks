package pt.ulisboa.tecnico.thesis.benchmarks.replica;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.ZonedDateTime;

public class SystemReport {

    private static final String INTERFACE = "enp0s8";
    private long time;
    private double cpu;
    private double bandwidthIn;
    private double bandwidthOut;
    private double freeMemory;
    private double totalMemory;
    public static final OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();

    public SystemReport() {
        this.time = ZonedDateTime.now().toInstant().toEpochMilli();
        try {
            String line;
            ProcessBuilder pb = new ProcessBuilder("ifstat", "0.1", "1");
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Ignore first two lines of output
            reader.readLine();
            reader.readLine();
            this.cpu = bean.getSystemLoadAverage();

            // Measure bandwidth
            line = reader.readLine();
            String[] things = line.strip().split(" ");
            try {
                this.bandwidthIn = Double.parseDouble(things[0]);
            } catch (NumberFormatException e) {
                this.bandwidthIn = 0;
            }
            try {
                this.bandwidthOut = Double.parseDouble(things[things.length-1]);
            } catch (NumberFormatException e) {
                this.bandwidthOut = 0;
            }

            // Measure memory usage
            this.freeMemory = Runtime.getRuntime().freeMemory();
            this.totalMemory = Runtime.getRuntime().totalMemory();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getTime() {
        return time;
    }

    public double getCpu() {
        return cpu;
    }

    public double getBandwidthIn() {
        return bandwidthIn;
    }

    public double getBandwidthOut() {
        return bandwidthOut;
    }

    public double getFreeMemory() {
        return freeMemory;
    }

    public double getTotalMemory() {
        return totalMemory;
    }
}
