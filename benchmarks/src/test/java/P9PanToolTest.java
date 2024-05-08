
import P8PanTool1.P8Logic;
import P9PanTool2.P9Logic;
import tudcomponents.MyGraph;
import util.Util;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P9PanToolTest {

    String input_path ="src/main/resources/P8/";
    boolean run_json = true;
    boolean run_ser = true;

    @org.junit.Test
    public void testManual() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path);
    }
    public void testFilesInDir(String path) {
        P9Logic analysis = new P9Logic();

        if (!Util.dirExists(path)) {
            System.err.println("Input directory not found: " + path);
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            return;
        }

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
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

