package P9Constraint;


import tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(JQF.class)
public class P9Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P9ConstraintLogic analysis = new P9ConstraintLogic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);
        analysis.function_1(g, 0, "Data1", 2, false, false);
        analysis.function_1(g, 0, "Data1", 2, true, false);
        analysis.function_1(g, 0, "Data1", 2, false, true);
        analysis.function_1(g, 0, "Data1", 2, true, true);
    }
}
