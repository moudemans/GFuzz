package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P3;


import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.*;

import java.io.IOException;

public class P3Weight {


    public void run(String inputFile) throws IOException {
        MyGraph g = MyGraph.readGraphFromFile(inputFile);
        calcAverage(g);
    }

    private int calcAverage(MyGraph g) {
        int average = g.nodeCount() / g.getNodes().get(0).getEdges().stream().mapToInt(e -> e.weight).reduce(0, Integer::sum);
        return  average;
    }

}
