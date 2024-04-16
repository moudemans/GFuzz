
import Components.MyGraph;
import P4Value.P4Conditional;
import P9GConstraint.P9Constraint;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class P4ConditionalTest {

    @Test
    public void test01() {
        P4Conditional analysis = new P4Conditional();

        try {
            analysis.run("src/main/resources/P4Examples/simple.ser");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertTrue("test", true);
    }
}
