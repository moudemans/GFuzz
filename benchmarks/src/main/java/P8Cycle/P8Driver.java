package P8Cycle;


import tudcomponents.MyGraph;

import java.io.IOException;


public class P8Driver {

    public void test1(String fileName) throws IOException {
        P8Logic analysis = new P8Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);

        analysis.run(g);
    }
}
