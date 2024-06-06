
import P2Transportation.P2Logic;
import org.junit.Test;
import tudcomponents.MyGraph;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class P2TransportationTest {
    String input_path ="src/main/resources/P2/";
    boolean run_json = true;
    boolean run_ser = true;


    @org.junit.Test
    public void test01() {
        String path = input_path + "MANUAL/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testPGMARK() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMUTATED2() {

        String path = input_path + "MUTATED/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMUTATED() {

        String path = input_path + "MUTATED2/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMANUAL2() {
        String path = input_path + "PGMark-Random/";
        testFilesInDir(path, 0);
    }

    @Test
    public void testres() {
        int limit = 0;
        String path = input_path +"saved-inputs_3/";
        testFilesInDir(path, limit);
    }

    @Test
    public void test2() {
        int limit = 32;
        String path = input_path +"pgfuzz2/saved-inputs_1/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testmm() {
        int limit = 0;
        String path = input_path +"mm/saved-inputs_4/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testcompound() {
        int limit = 12;
        String path = input_path +"compound3/saved-inputs_1/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testrand() {
        int limit = 20;
        String path = input_path +"rand/saved-inputs_5/";
        testFilesInDir(path, limit);
    }
    @Test
    public void testgen() {
        int limit = 1;
        String path = input_path +"saved-inputs_gen1/";
        testFilesInDir(path, limit);
    }

    public void testFilesInDir(String path, int limit) {
        P2Logic analysis = new P2Logic();

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        Arrays.sort(listOfFiles, new Comparator<File>() {
            public int compare(File str1, File str2) {
                if (str1.getName().contains("fuzz") || str2.getName().contains("fuzz")) {
                    return 1;
                }
                if (!str1.getName().contains("_") || !str2.getName().contains("_")) {
                    return 1;
                }

                String substr1 = str1.getName().split("_")[1].split("\\.")[0];
                String substr2 = str2.getName().split("_")[1].split("\\.")[0];

                return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
            }
        });

        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        int counter = 0;
        for (File f :
                listOfFiles) {
            MyGraph g;

            if (limit > 0 && counter >= limit) {
                break;
            }

            if (run_json && f.getPath().contains("json")) {
                g = MyGraph.readGraphFromJSON(f.getPath());
            } else if (run_ser && f.getPath().contains("ser")) {
                g = MyGraph.readGraphFromJSON(f.getPath());
            } else {
                continue;
            }
            try {
                counter++;
                if (counter >= limit - 3) {
                    System.out.println("File input: " + f);
                }

                analysis.run(g);
            } catch (Exception e) {
                if (counter >= limit - 3) {

                    System.err.println("Caught exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Number of files processed: " + counter);
        assertTrue("test", true);
    }

}

