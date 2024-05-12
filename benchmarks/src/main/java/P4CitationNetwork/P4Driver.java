package P4CitationNetwork;



import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)
public class P4Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P4Logic analysis = new P4Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);

        analysis.run(g);
    }
}
