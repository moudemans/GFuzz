

import P1Medical.P1Logic;
import tudcomponents.MyGraph;
import org.junit.Test;
import util.Util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

public class P1MedicalTest {
    String input_path ="src/main/resources/P1/";
    boolean run_json = true;
    boolean run_ser = false;
    @Test
    public void test01() {
        P1Logic analysis = new P1Logic();

        int start_test_case = 1;
        int end_test_case = 6;


        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="benchmarks/src/main/resources/P1/P9Examples-MANUAL/test0" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g, 0, "Data1", 2, false, false);
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }

        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="benchmarks/src/main/resources/P9/P9Examples-MANUAL/test0" + i + ".ser";

                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g, 0, "Data1", 2, false, true);
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }

        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="benchmarks/src/main/resources/P9/P9Examples-MANUAL/test0" + i + ".ser";

                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g, 0, "Data1", 2, true, false);
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }



        assertTrue("test", true);
    }

    @Test
    public void testGMARK() {
        String path = input_path + "GMark-FIXED/";
        testFilesInDir(path, 0);
    }
    @Test
    public void testGMARKINV() {
        String path = input_path + "GMark-INV-FIXED/";
        testFilesInDir(path, 0);
    }

    @Test
    public void testGMARKRandom() {
        String path = input_path + "GMark-RANDOM/";
        testFilesInDir(path, 0);
    }

    @Test
    public void testGMARKRandom2() {
        String path =input_path + "GMark-RANDOM2/";
        testFilesInDir(path, 0);
    }
    @Test
    public void testGMARKMutation() {
        String path = input_path + "GMark-MUTATED2/";
        testFilesInDir(path, 0);
    }

    @Test
    public void testres() {
        int limit = 30;
        String path = input_path +"saved-inputs_3/";
        testFilesInDir(path, limit);
    }
    @Test
    public void test2() {
        int limit = 18;
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
        int limit = 0;
        String path = input_path +"compound3/saved-inputs_1/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testrand() {
        int limit = 10;
        String path = input_path +"saved-inputs_rand3/";
        testFilesInDir(path, limit);
    }

    @Test
    public void testgmark2() {
        int limit = 5;
        String path = input_path +"saved-inputs_gen1/";
        testFilesInDir(path, limit);
    }

    public void testFilesInDir(String path, int limit) {

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
                if (!str1.getName().contains("_") || !str2.getName().contains("_")) {
                    return 1;
                }
                String[] tmp1 = str1.getName().split("_");
                String[] tmp2 = tmp1[1].split("\\.");


                String substr1 = str1.getName().split("_")[1].split("\\.")[0];
                String substr2 = str2.getName().split("_")[1].split("\\.")[0];

                return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
            }
        });

        int counter = 0;
        for (File f :
                listOfFiles) {

            if (limit > 0 && counter >= limit) {
                break;
            }
            MyGraph g;
            if (run_json && f.getPath().contains("json")) {
                g = MyGraph.readGraphFromJSON(f.getPath());
            } else if (run_ser && f.getPath().contains("ser")) {
                g = MyGraph.readGraphFromFile(f.getPath());
            } else {
                continue;
            }


            try {
                System.out.println("File input: " + f);
                call_test(f);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
                e.printStackTrace();
            }
            counter++;
        }
        System.out.println("Executed " + counter + " tests" );
        assertTrue("test", true);
    }

    public MyGraph readGraph(File f) {
        if (run_json && f.getPath().contains("json")) {
            return MyGraph.readGraphFromJSON(f.getPath());
        } else if (run_ser && f.getPath().contains("ser")) {
            return MyGraph.readGraphFromFile(f.getPath());
        } else {
            return null;
        }
    }

    private void call_test(File f) {
        P1Logic analysis = new P1Logic();
        int prev_item_id = 0;
        int new_item_id = 2;
        String value = "Data1";
//        int prev_item_id = 5;
//        int new_item_id = 0;

        if (readGraph(f) == null) {
            return;
        }

        try {
            MyGraph g = readGraph(f);

            analysis.function_1(g, prev_item_id, value, new_item_id, false, false);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        try {
            MyGraph g2 =  readGraph(f);
            analysis.function_1(g2, prev_item_id, value, new_item_id, false, true);
        } catch (Exception ignored) {
            ignored.printStackTrace();

        }

        try {
            MyGraph g3 =  readGraph(f);
            analysis.function_1(g3, prev_item_id, value, new_item_id, true, false);
        } catch (Exception ignored) {
            ignored.printStackTrace();

        }

        try {
            MyGraph g4 =  readGraph(f);
            analysis.function_1(g4, prev_item_id, value, new_item_id, true, true);
        } catch (Exception ignored) {
            ignored.printStackTrace();

        }
    }
}
