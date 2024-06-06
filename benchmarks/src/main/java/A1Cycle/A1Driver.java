package A1Cycle;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class A1Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        A1Logic analysis = new A1Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);

        analysis.run(g);
    }
}
