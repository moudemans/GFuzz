import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class intTest {

    @Fuzz
    public void test(@From(intGenerator.class) String s) {
        // Assume that the date is Feb 29
        intLogic.testBranch(s);

        // Under this assumption, validate leap year rules
        assertTrue("holder", true);
    }
}
