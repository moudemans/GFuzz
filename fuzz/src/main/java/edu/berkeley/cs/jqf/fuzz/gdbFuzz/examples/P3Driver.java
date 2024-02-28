package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(JQF.class)
public class P3Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P3WeightAlgorithm analysis = new P3WeightAlgorithm();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }

    public static void main(String[] args) throws IOException {

        P3WeightAlgorithm analysis = new P3WeightAlgorithm();

        analysis.run("fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/seed.txt");
    }
