package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples;

import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.MyGraph;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.Node;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(JQF.class)
public class P4Conditional {
    MyGraph my_g;

    public void run(String inputFile) throws IOException {
        MyGraph g = MyGraph.readGraphFromFile(inputFile);
        runConditions(g);
    }

    public int runConditions(MyGraph g) {
        my_g = g;
        Node firstNode = my_g.getNodes().get(0);

        int value = Integer.parseInt(firstNode.properties.get("value"));
        if (value <= 0) {
            return 1;
        } else if (value <= 10) {
            return 2;
        } else if (value <= 20) {
            return 3;
        } else if (value <= 30) {
            return 4;
        } else if (value <= 40) {
            return 5;
        } else if (value <= 50) {
            return 6;
        } else if (value <= 60) {
            return 7;
        } else if (value <= 70) {
            return 8;
        } else if (value <= 80) {
            return 9;
        } else if (value <= 90) {
            return 10;
        } else if (value <= 100) {
            return 11;
        } else if (value <= 110) {
            return 12;
        } else if (value <= 120) {
            return 13;
        } else if (value <= 130) {
            return 14;
        }

        return -1;
    }

}