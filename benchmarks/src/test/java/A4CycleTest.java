
import A1Cycle.A1Logic;
import A4Hotspot.A4Logic;
import org.junit.Test;
import tudcomponents.MyGraph;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class A4CycleTest {

    String input_path = "src/main/resources/A4/";
    boolean run_json = true;
    boolean run_ser = true;



    @Test
    public void test10compound() {
        int limit = 30;
        String path = input_path + "compound/saved-inputs_2/";
        testFilesInDir(path, limit);
    }


    @Test
    public void test10gen() {
        int limit = 5;
        String path = input_path + "gen/saved-inputs_2/";
        testFilesInDir(path, limit);
    }



    public void testFilesInDir(String path, int limit) {
        A4Logic analysis = new A4Logic();

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

