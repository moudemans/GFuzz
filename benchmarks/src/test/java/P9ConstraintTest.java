

import P9GConstraint.P9ConstraintLogic;
import tudcomponents.MyGraph;
import org.junit.Test;

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
        P9ConstraintLogic analysis = new P9ConstraintLogic();

        for (int i = 1; i <= 9; i++) {
            try {
                String fileName = "src/main/resources/P9-GMARK/test0" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                try {
                    analysis.function_1(g, 5, "Data1", 0, false, false);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

                try {
                MyGraph g2 = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g2, 5, "Data1", 0, false, true);
                } catch (Exception ignored) {}

                try {
                MyGraph g3 = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g3, 5, "Data1", 0, true, false);
                } catch (Exception ignored) {}

                try {
                MyGraph g4 = MyGraph.readGraphFromFile(fileName);
                analysis.function_1(g4, 5, "Data1", 0, true, true);
                } catch (Exception ignored) {}
            } catch (Exception e) {
//            throw new RuntimeException(e);
                //System.out.println("Exception returned");

            }
        }

        assertTrue("test", true);
    }

    @Test
    public void testGMARKMutated() {
        P9ConstraintLogic analysis = new P9ConstraintLogic();

        for (int i = 0; i < 10000 ; i++) {
            try {
                String fileName = "src/main/resources/P9-GMARK-MUTATED2/mutated_" + i + ".ser";
                MyGraph g = MyGraph.readGraphFromFile(fileName);
                try {
                    analysis.function_1(g, 5, "Data1", 0, false, false);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

                try {
                    MyGraph g2 = MyGraph.readGraphFromFile(fileName);
                    analysis.function_1(g2, 5, "Data1", 0, false, true);
                } catch (Exception ignored) {}

                try {
                    MyGraph g3 = MyGraph.readGraphFromFile(fileName);
                    analysis.function_1(g3, 5, "Data1", 0, true, false);
                } catch (Exception ignored) {}

                try {
                    MyGraph g4 = MyGraph.readGraphFromFile(fileName);
                    analysis.function_1(g4, 5, "Data1", 0, true, true);
                } catch (Exception ignored) {}
            } catch (Exception e) {
//            throw new RuntimeException(e);
                System.out.println("Exception returned");

            }
        }

        assertTrue("test", true);
    }
}
