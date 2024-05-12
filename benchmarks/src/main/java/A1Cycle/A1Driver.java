package A1Cycle;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class A1Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        A1Logic analysis = new A1Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);

        analysis.run(g);
    }
}
