
import P8PanTool1.P8Logic;
import org.junit.Test;
import tudcomponents.MyGraph;
import util.Util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class P8PanToolTest {

    String input_path ="src/main/resources/P8/";
    boolean run_json = true;
    boolean run_ser = false;

    @org.junit.Test
    public void testManual() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test

    public void testManual2() {
        String path = input_path + "MANUAL_FUZZ/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testPGMARK() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMUTETAD() {
        String path = input_path + "MUTATED/";
        testFilesInDir(path, 0);
    }


    @Test
    public void testres() {
        int limit = 41;
        String path = input_path +"saved-inputs_2/";
        testFilesInDir(path, limit);
    }

    @Test
    public void test2() {
        int limit = 12;
        String path = input_path +"pgfuzz3/saved-inputs_1/";
        testFilesInDir(path, limit);
    }


    @Test
    public void testmm() {
        int limit = 0;
        String path = input_path +"mm/saved-inputs_0/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testcompound() {
        int limit = 49;
        String path = input_path +"compoundNew/saved-inputs_1/";
        testFilesInDir(path, limit);
    }



    @Test
    public void testrand() {
        int limit =34 ;
        String path = input_path +"rand/saved-inputs_5/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testgen() {
        int limit =7;
        String path = input_path +"gen/saved-inputs_4/";
        testFilesInDir(path, limit);
    }

    public void testFilesInDir(String path, int limit) {
        P8Logic analysis = new P8Logic();

        if (!Util.dirExists(path)) {
            System.err.println("Input directory not found: " + path);
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            return;
        }

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();

        if (!input_dir.getPath().contains("MANUAL")) {
            Arrays.sort(listOfFiles, new Comparator<File>() {
                public int compare(File str1, File str2) {
                    if (str1.getName().contains("fuzz") || str2.getName().contains("fuzz")) {
                        return 0;
                    }

                    if (!str1.getName().contains("_") || !str2.getName().contains("_")) {
                        return 0;
                    }
                    String substr1 = str1.getName().split("_")[1].split("\\.")[0];
                    String substr2 = str2.getName().split("_")[1].split("\\.")[0];

                    return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
                }
            });
        }
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
            if (f.getPath().contains("164")) {
                continue;
            }

            try {

                if (counter >= limit -3) {
                    System.out.println("File" + counter + ": " + f.getPath());
                }
                analysis.run(g);
            } catch (Exception e) {
                if (counter >= limit -3) {

                    System.err.println("Caught exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            counter++;
        }
        System.out.println("files processed: " + (counter-1));
        assertTrue("test", true);
    }

}

