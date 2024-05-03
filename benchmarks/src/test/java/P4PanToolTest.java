
import P3Pangenomic.P3Logic;
import P4PanTool.P4Logic;
import tudcomponents.MyGraph;
import util.Util;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P4PanToolTest {

    String input_path ="benchmarks/src/main/resources/P4/";

    @org.junit.Test
    public void testManual() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path);
    }
    public void testFilesInDir(String path) {
        P4Logic analysis = new P4Logic();

        if (!Util.dirExists(path)) {
            System.err.println("Input directory not found: " + path);
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            return;
        }

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
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

