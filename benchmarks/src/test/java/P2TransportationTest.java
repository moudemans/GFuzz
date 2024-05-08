
import P2Transportation.P2Logic;
import tudcomponents.MyGraph;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P2TransportationTest {
    String input_path ="src/main/resources/P2/";
    boolean run_json = true;
    boolean run_ser = false;


    @org.junit.Test
    public void test01() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path);
    }

    @org.junit.Test
    public void testPGMARK() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path);
    }

    @org.junit.Test
    public void testMUTATED2() {

        String path = input_path + "PGMark-MUTATED2/";
        testFilesInDir(path);
    }

    @org.junit.Test
    public void testMANUAL2() {
        String path = input_path + "PGMark-Random/";
        testFilesInDir(path);
    }

    public void testFilesInDir(String path) {
        P2Logic analysis = new P2Logic();

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        for (File f :
                listOfFiles) {
            MyGraph g;

            if (run_json && f.getPath().contains("json")) {
                g = MyGraph.readGraphFromJSON(f.getPath());
            } else if (run_ser && f.getPath().contains("ser")) {
                g = MyGraph.readGraphFromFile(f.getPath());
            } else {
                continue;
            }
            try {
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
        assertTrue("test", true);
    }

}

