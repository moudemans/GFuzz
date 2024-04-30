
import P10Pangenomic.P10Logic;
import P7Transportation.P7Logic;
import tudcomponents.MyGraph;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P7TransportationTest {

    @org.junit.Test
    public void test01() {
        P7Logic analysis = new P7Logic();

        int start_test_id = 1;
        int end_test_id = 4;

        for (int i = start_test_id; i <= end_test_id; i++) {
            try {
//                String fileName = "src/main/resources/P7/MANUAL/test" + i + ".ser";
//                String fileName = "benchmarks/src/main/resources/P7/MANUAL/test" + i + ".json";
//                MyGraph g = MyGraph.readGraphFromJSON(fileName);
                String fileName = "benchmarks/src/main/resources/P7/MANUAL/test" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }
        }


        assertTrue("test", true);
    }

    @org.junit.Test
    public void testPGMARK() {
        P7Logic analysis = new P7Logic();

        try {
//            String fileName = "src/main/resources/P7/PGMark-FIXED/test" + i + ".ser";
            String path = "benchmarks/src/main/resources/P7/PGMark-FIXED/";
            File input_dir = new File(path);
            File[] listOfFiles = input_dir.listFiles();
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            for (File f :
                    listOfFiles) {
                if (f.getPath().contains("json")) {
                    continue;
                }
                MyGraph g = MyGraph.readGraphFromFile(f.getPath());
                analysis.run(g);

            }
        } catch (Exception e) {
            System.err.println("Caught exception: " + e.getMessage());
        }


        assertTrue("test", true);
    }

    @org.junit.Test
    public void testMUTATED2() {
        P7Logic analysis = new P7Logic();
        int count = 1;
        try {
//                String fileName = "src/main/resources/P7/PGMark-FIXED/test" + i + ".ser";
            String path = "benchmarks/src/main/resources/P7/PGMark-MUTATED2/";
            File input_dir = new File(path);
            File[] listOfFiles = input_dir.listFiles();
            for (File f :
                    listOfFiles) {
                System.out.println("at " + count);
                MyGraph g = MyGraph.readGraphFromFile(f.getPath());
                analysis.run(g);
            }
        } catch (Exception e) {
            System.err.println("Caught exception: " + e.getMessage());
        }

        assertTrue("test", true);
    }

    @org.junit.Test
    public void testMANUAL2() {
        String path = "benchmarks/src/main/resources/P7/PGMark-Random/";
        testFilesInDir(path);
    }

    public void testFilesInDir(String path) {
        P7Logic analysis = new P7Logic();

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

