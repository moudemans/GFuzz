package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P9GConstraint;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P4Value.P4Conditional;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(JQF.class)
public class P9Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P9Constraint analysis = new P9Constraint();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }
}
