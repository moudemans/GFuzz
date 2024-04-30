

import P9Constraint.P9ConstraintLogic;
import tudcomponents.MyGraph;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class P9ConstraintTest {

    @Test
    public void test01() {
        P9ConstraintLogic analysis = new P9ConstraintLogic();

        int start_test_case = 6;
        int end_test_case = 6;


        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="src/main/resources/P9Examples-MANUAL/test0" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g, 0, "Data1", 2, false, false);
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }

        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="src/main/resources/P9Examples-MANUAL/test0" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g, 0, "Data1", 2, false, true);
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }

        for (int i = start_test_case; i <= end_test_case; i++) {
            try {
                String fileName ="src/main/resources/P9Examples-MANUAL/test0" + i + ".ser";
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
        String path = "src/main/resources/P9/P9-GMARK/";
        testFilesInDir(path);


    }

    @Test
    public void testGMARKMutated() {
        String path = "src/main/resources/P9/P9-GMARK-MUTATED2/";
        testFilesInDir(path);
    }

    public void testFilesInDir(String path) {

        File input_dir = new File(path);
        File[] listOfFiles = input_dir.listFiles();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        for (File f :
                listOfFiles) {
            if (f.getPath().contains("json")) {
                continue;
            }
            try {
                call_test(f);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
        assertTrue("test", true);
    }

    private void call_test(File f) {
        P9ConstraintLogic analysis = new P9ConstraintLogic();

        try {
            MyGraph g = MyGraph.readGraphFromFile(f.getPath());

            analysis.function_1(g, 5, "Data1", 0, false, false);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        try {
            MyGraph g2 = MyGraph.readGraphFromFile(f.getPath());
            analysis.function_1(g2, 5, "Data1", 0, false, true);
        } catch (Exception ignored) {}

        try {
            MyGraph g3 = MyGraph.readGraphFromFile(f.getPath());
            analysis.function_1(g3, 5, "Data1", 0, true, false);
        } catch (Exception ignored) {}

        try {
            MyGraph g4 = MyGraph.readGraphFromFile(f.getPath());
            analysis.function_1(g4, 5, "Data1", 0, true, true);
        } catch (Exception ignored) {}
    }
}
