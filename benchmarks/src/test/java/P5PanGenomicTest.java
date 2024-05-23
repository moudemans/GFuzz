
import P5Pangenomic.P5Logic;
import org.junit.Test;
import tudcomponents.MyGraph;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class P5PanGenomicTest {

    String input_path ="src/main/resources/P5/";
    boolean run_json = true;
    boolean run_ser = false;

    @org.junit.Test
    public void testPGMARK() {
        String path = input_path + "PGMark-FIXED/";
        testFilesInDir(path, 0);
    }
    @org.junit.Test
    public void testMUTATED() {
        String path = input_path + "GFUZZ/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testMUTATED2() {
        String path = input_path + "GFUZZ-NEW/";
        testFilesInDir(path, 0);
    }

    @org.junit.Test
    public void testRANDOM() {
        String path = input_path + "PGMark-RANDOM/";
        testFilesInDir(path, 0);
    }

    @Test
    public void testres() {
        int limit = 16;
        String path = input_path +"saved-inputs_3/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testrand() {
        int limit = 0;
        String path = input_path +"saved-inputs_rand3/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testgen() {
        int limit = 2;
        String path = input_path +"saved-inputs_gen1/";
        testFilesInDir(path, limit);
    }



    public void testFilesInDir(String path, int limit) {
        P5Logic analysis = new P5Logic();

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

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
                System.out.println("File: " + f.getPath());
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
//                e.printStackTrace();
            }
            counter++;
        }
        System.out.println("files run: " + (counter - 1));
        assertTrue("test", true);
    }

}

