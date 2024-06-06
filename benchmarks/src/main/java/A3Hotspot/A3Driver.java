package A3Hotspot;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class A3Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        A3Logic analysis = new A3Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);

        analysis.run(g);

    }
}
