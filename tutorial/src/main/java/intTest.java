import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class intTest {

    @Fuzz
    public void test( String s) {
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        BufferedReader brTest = null;
        try {
            brTest = new BufferedReader(new FileReader(s));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String text_number;
        try {
            text_number = brTest.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("Firstline is : " + text_number);
        // Assume that the date is Feb 29
        intLogic.testBranch(text_number);

        // Under this assumption, validate leap year rules
        assertTrue("holder", true);
    }
}
