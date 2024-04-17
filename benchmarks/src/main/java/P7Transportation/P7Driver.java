package P7Transportation;


import Components.MyGraph;

import java.io.IOException;


public class P7Driver {

    public void test1(String fileName) throws IOException {
        P7Logic analysis = new P7Logic();
//        List<String> fileList = Files.readAllLines(Paths.get(fileName));
        MyGraph g = MyGraph.readGraphFromFile(fileName);

        analysis.run(g);
    }
}
