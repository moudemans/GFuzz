package S1Horizontal;


import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)

public class S1Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        S1Logic analysis = new S1Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);



        analysis.run(g);
    }
}
