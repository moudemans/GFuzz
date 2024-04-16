package P2Label;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class P2Driver {


        public void test1(String fileName) throws IOException {
            P2Label analysis = new P2Label();
            List<String> fileList = Files.readAllLines(Paths.get(fileName));
            analysis.run(fileList.get(0));
        }

        public static void main(String[] args) throws IOException {
            P2Driver a = new P2Driver();
            a.test1("fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/fuzz-input/inputLocation.txt");

//            analysis.run("fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/seed.txt");
        }
}
