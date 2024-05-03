
import A1Cycle.A1Logic;
import tudcomponents.MyGraph;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class A1CycleTest {

    @org.junit.Test
    public void test01() {
        A1Logic analysis = new A1Logic();

        int start_test_id = 1;
        int end_test_id = 3;

        for (int i = start_test_id; i <= end_test_id; i++) {
            try {
                String fileName = "src/main/resources/P8/MANUAL/test" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }
        }


        assertTrue("test", true);
    }

    @org.junit.Test
    public void GMARK() {

        String fileName = "benchmarks/src/main/resources/P8/GMARK-FIXED/";
        testFilesInDir(fileName, "");

    }

    @org.junit.Test
    public void GMARKMUTATED() {

        String fileName = "benchmarks/src/main/resources/P8/GMARK-GFUZZ/";
        testFilesInDir(fileName, "");

    }

    @org.junit.Test
    public void GMARKMUTATED2() {

        String fileName = "benchmarks/src/main/resources/P8/GMARK-GFUZZ2/";
        testFilesInDir(fileName, "");

    }

    @org.junit.Test
    public void GMARKRandom() {

        String fileName = "benchmarks/src/main/resources/P8/GMARK-RANDOM/";
        testFilesInDir(fileName, "");

    }

    public void testFilesInDir(String path, String pattern) {
        A1Logic analysis = new A1Logic();
        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        for (File f :
                listOfFiles) {
            if (f.getPath().contains("json")) {
                continue;
            }
            if (pattern != null && !pattern.isEmpty() && !f.getPath().contains(pattern)) {
                continue;
            }

            try {
                System.out.println("File input: " + f);
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
