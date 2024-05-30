package P6PhenoOut;



import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import tudcomponents.MyGraph;

import java.io.IOException;


@RunWith(JQF.class)
public class P6Driver {

    @Fuzz
    public void test1(String fileName) throws IOException {
        P6Logic analysis = new P6Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromJSON(fileName);

        analysis.run(g);
    }

//    public static void main(String[] args) {
//        P6Logic analysis = new P6Logic();
////        List<String> fileList = Files.readAllLines(Paths.get(fileName));
//        MyGraph g = MyGraph.readGraphFromJSON("benchmarks/src/main/resources/P6/pgfuzzNew/saved-inputs_0/Coverage_292.json");
//
//        analysis.run(g);
//    }
}
