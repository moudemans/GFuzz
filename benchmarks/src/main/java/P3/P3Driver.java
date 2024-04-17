package P3;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class P3Driver {

    public void test1(String fileName) throws IOException {
        P3Weight analysis = new P3Weight();
        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        analysis.run(fileList.get(0));
    }

    public static void main(String[] args) throws IOException {

        P3Weight analysis = new P3Weight();

        analysis.run("fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/seed.txt");
    }
}
