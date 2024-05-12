package P1Medical;


import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(JQF.class)
public class P1Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P1Logic analysis = new P1Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);
        analysis.function_1(g, 0, "Data1", 2, false, false);
        MyGraph g2 = MyGraph.readGraphFromJSON(fileName);
        analysis.function_1(g2, 0, "Data1", 2, true, false);
        MyGraph g3 = MyGraph.readGraphFromJSON(fileName);
        analysis.function_1(g3, 0, "Data1", 2, false, true);
        MyGraph g4 = MyGraph.readGraphFromJSON(fileName);
        analysis.function_1(g4, 0, "Data1", 2, true, true);
    }
}
