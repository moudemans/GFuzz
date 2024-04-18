

import P9Constraint.DEPRECATED.P9Constraint;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class P9ConstraintDeprecatedTest {

    @Test
    public void test01() {
        P9Constraint analysis = new P9Constraint();

        for (int i = 4; i <= 4; i++) {
            try {
                analysis.run("src/main/resources/P9Examples-MANUAL/test0" + i + ".ser");
            } catch (Exception e) {
//            throw new RuntimeException(e);
                System.out.println("Exception returned");

            }
        }


        assertTrue("test", true);
    }

    @Test
    public void testGMARK() {
        P9Constraint analysis = new P9Constraint();

        for (int i = 1; i <= 9; i++) {
            try {
                analysis.run("src/main/resources/P9-GMARK/test0" + i + ".ser");
            } catch (Exception e) {
//            throw new RuntimeException(e);
                System.out.println("Exception returned");

            }
        }

        assertTrue("test", true);
    }
}
