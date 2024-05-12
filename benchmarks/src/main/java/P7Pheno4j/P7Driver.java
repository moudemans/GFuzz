package P7Pheno4j;



import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)
public class P7Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P7Logic analysis = new P7Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);

        analysis.run(g);
    }
}
