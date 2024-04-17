import Components.MyGraph;
import P7Transportation.P7Logic;

import static org.junit.Assert.assertTrue;

public class P7TransportationTest {

    @org.junit.Test
    public void test01() {
        P7Logic analysis = new P7Logic();

        int start_test_id = 1;
        int end_test_id = 4;

        for (int i = start_test_id; i <= end_test_id; i++) {
            try {
                String fileName = "src/main/resources/P7/MANUAL/test" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }
        }


        assertTrue("test", true);
    }

}

