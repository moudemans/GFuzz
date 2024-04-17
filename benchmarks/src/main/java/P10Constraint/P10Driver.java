
import tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(JQF.class)
public class P10Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P10ConstraintLogic analysis = new P10ConstraintLogic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);
        analysis.function_1(g, 0, "Data1", 2, false, false);
        assertTrue("holder", true);
    }
}
