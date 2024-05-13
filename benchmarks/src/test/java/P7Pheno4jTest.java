
import P7Pheno4j.P7Logic;
import org.junit.Test;
import tudcomponents.MyGraph;
import util.Util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class P7Pheno4jTest {

    String input_path ="src/main/resources/P7/";
    boolean run_json = true;
    boolean run_ser = false;

    @org.junit.Test
    public void testManual() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path, 0);
    }
    @org.junit.Test
    public void testSeed() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMutated() {
        String path = input_path + "GFUZZ/";
        testFilesInDir(path, 0);
    }


    @Test
    public void testres() {
        int limit = 34;
        String path = input_path +"saved-inputs_3/";
        testFilesInDir(path, limit);
    }

    public void testFilesInDir(String path, int limit) {
        P7Logic analysis = new P7Logic();

        if (!Util.dirExists(path)) {
            System.err.println("Input directory not found: " + path);
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            return;
        }



        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();


        Arrays.sort(listOfFiles, new Comparator<File>() {
            public int compare(File str1, File str2) {
                if (str1.getName().contains("fuzz") || str2.getName().contains("fuzz")) {
                    return 1;
                }
                String substr1 = str1.getName().split("_")[1].split("\\.")[0];
                String substr2 = str2.getName().split("_")[1].split("\\.")[0];

                return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
            }
        });


        int counter = 1;
        for (File f :
                listOfFiles) {
            MyGraph g;

            if (limit > 0 && counter > limit) {
                break;
            }
            if (run_json && f.getPath().contains("json")) {
                g = MyGraph.readGraphFromJSON(f.getPath());
            } else if (run_ser && f.getPath().contains("ser")) {
                g = MyGraph.readGraphFromFile(f.getPath());
            } else {
                continue;
            }


            try {
                System.out.println("File"+counter+": " + f.getPath());
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
                e.printStackTrace();
            }

            counter++;
        }
        System.out.println("files processed: " + (counter-1));
        assertTrue("test", true);
    }

}

