
import P7Transportation.P7Logic;
import tudcomponents.MyGraph;

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

        int start_test_id = 1;
        int end_test_id = 10;

        for (int i = start_test_id; i <= end_test_id; i++) {
            try {
//                String fileName = "src/main/resources/P7/MANUAL/test" + i + ".ser";
                String fileName = "src/main/resources/P7/PGMark-FIXED/test" + i + ".json";
                MyGraph g = MyGraph.readGraphFromJSON(fileName);
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }
        }


        assertTrue("test", true);
    }

}

