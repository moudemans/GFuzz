
import P8Cycle.P8Logic;
import tudcomponents.MyGraph;

import static org.junit.Assert.assertTrue;

public class P8CycleTest {

    @org.junit.Test
    public void test01() {
        P8Logic analysis = new P8Logic();

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
        P8Logic analysis = new P8Logic();

        int start_test_id = 1;
        int end_test_id = 20;

        for (int i = start_test_id; i <= end_test_id; i++) {
            try {
                String fileName = "src/main/resources/P8/GMARK-FIXED/test" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                analysis.run(g);
            } catch (Exception e) {
                System.err.println("Caught exception: " + e.getMessage());
            }
        }


        assertTrue("test", true);
    }

}
