package P9GConstraint;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class P9Driver {


    public void test1(String fileName) throws IOException {
        P9Constraint analysis = new P9Constraint();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }

    public static void main(String[] args) throws IOException {
        P9Constraint analysis = new P9Constraint();

        analysis.run("examplesCoverage/src/main/resources/P9Examples/test02.ser");
    }
}
