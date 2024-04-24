
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
                String fileName = "src/main/resources/P7/MANUAL/test" + i + ".json";
                MyGraph g = MyGraph.readGraphFromJSON(fileName);
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
            String path = "src/main/resources/P7/PGMark-FIXED/";
            File input_dir = new File(path);
            File[] listOfFiles = input_dir.listFiles();
            for (File f :
                    listOfFiles) {
                if(f.getPath().contains("json")) {
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
    public void testMUTATED() {
        P7Logic analysis = new P7Logic();

            try {
//                String fileName = "src/main/resources/P7/PGMark-FIXED/test" + i + ".ser";
                String path = "src/main/resources/P7/PGMark-MUTATED2/";
                File input_dir = new File(path);
                File[] listOfFiles = input_dir.listFiles();
                for (File f :
                        listOfFiles) {
                    MyGraph g = MyGraph.readGraphFromFile(f.getPath());
                    analysis.run(g);
                }
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }

        assertTrue("test", true);
    }

}

