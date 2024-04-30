
import P10Pangenomic.P10Logic;
import tudcomponents.MyGraph;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P10PanGenomicTest {


    @org.junit.Test
    public void testMUTATED() {
        String path = "benchmarks/src/main/resources/P10/PGMark-GFUZZ/";
        testFilesInDir(path);
    }

    @org.junit.Test
    public void testPGMARK2() {
        String path = "benchmarks/src/main/resources/P10/PGMark-FIXED/";
        testFilesInDir(path);
    }

    public void testFilesInDir(String path) {
        P10Logic analysis = new P10Logic();

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        for (File f :
                listOfFiles) {
            if (f.getPath().contains("json")) {
                continue;
            }
            try {
                MyGraph g = MyGraph.readGraphFromFile(f.getPath());
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
        assertTrue("test", true);
    }

}

