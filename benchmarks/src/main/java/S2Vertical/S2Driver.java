package S2Vertical;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class S2Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        S2Logic analysis = new S2Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);



        analysis.run(g);
    }
}
