package P4;



import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)
public class P4Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P4Logic analysis = new P4Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);

        analysis.run(g);
    }
}
