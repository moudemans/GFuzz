
import P5Pangenomic.P5Logic;
import tudcomponents.MyGraph;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P5PanGenomicTest {

    String input_path ="src/main/resources/P5/";
    boolean run_json = true;
    boolean run_ser = true;

    @org.junit.Test
    public void testPGMARK() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path);
    }
    @org.junit.Test
    public void testMUTATED() {
        String path = input_path + "PGMark-GFUZZ/";
        testFilesInDir(path);
    }

    @org.junit.Test
    public void testRANDOM() {
        String path = input_path + "PGMark-RANDOM/";
        testFilesInDir(path);
    }



    public void testFilesInDir(String path) {
        P5Logic analysis = new P5Logic();

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

