package pt.ulisboa.tecnico.thesis.benchmarks.master.service;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import pt.ulisboa.tecnico.thesis.benchmarks.master.domain.BenchmarkResult;

import java.io.*;
import java.util.List;

public class CsvService {

    public static void write(List<BenchmarkResult> results, String protocol, Integer numReplicas, Integer batchSize, Integer payloadSize) {
        String outputFile = String.format("%s-%d-%d-%d", protocol, numReplicas, batchSize, payloadSize);
        try (Writer writer = new FileWriter(outputFile)) {
            StatefulBeanToCsv<BenchmarkResult> statefulBeanToCsv = new StatefulBeanToCsvBuilder<BenchmarkResult>(writer).build();
            statefulBeanToCsv.write(results);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
    }



    /*public static void write() {
        // declare csv file
        String outputFileName = "hb-4-faultfree.csv";

        List<BenchmarkResult> results = new ArrayList<>();
        results.add(new BenchmarkResult(0, 3242.6354556803994, 933.1881195290251));
        results.add(new BenchmarkResult(1, 3057.7877947295424, 928.0519530803363));
        results.add(new BenchmarkResult(2, 3217.4716267339218, 934.2634925280489));
        results.add(new BenchmarkResult(3, 4255.712230215828, 920.1710996622721));

        try (Writer writer = new FileWriter(outputFileName)) {
            StatefulBeanToCsv<BenchmarkResult> statefulBeanToCsv = new StatefulBeanToCsvBuilder<BenchmarkResult>(writer).build();
            statefulBeanToCsv.write(results);
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
    }

    public static void read() {
        String inputFileName = "hb-4-faultfree.csv";

        try (Reader reader = new FileReader(inputFileName)) {
            List<BenchmarkResult> results = new CsvToBeanBuilder<BenchmarkResult>(reader)
                    .withType(BenchmarkResult.class).build().parse();
            for (BenchmarkResult result: results) System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
