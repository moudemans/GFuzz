package P4Value;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class P4Driver {

    public void test1(String fileName) throws IOException {
        P4Conditional analysis = new P4Conditional();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }
}
