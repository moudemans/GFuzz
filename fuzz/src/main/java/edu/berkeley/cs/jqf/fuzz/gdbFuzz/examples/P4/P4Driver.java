package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P4;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(JQF.class)
public class P4Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P4Conditional analysis = new P4Conditional();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }
}
