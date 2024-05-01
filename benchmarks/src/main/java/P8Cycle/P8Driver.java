package P8Cycle;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class P8Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P8Logic analysis = new P8Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);

        analysis.run(g);
    }
}
