package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples;

import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.*;

import java.io.IOException;

public class P2LabelAlgorithm {
    MyGraph my_g;

    public void run(String inputFile) throws IOException {
        MyGraph g = MyGraph.readGraphFromFile(inputFile);
        System.out.println("Running program!");
        runConditions(g);
    }

    public boolean runConditions(MyGraph g) {
        my_g = g;
        Node firstNode = my_g.getNodes().get(0);

        if (firstNode.properties.containsKey("cat1")) {
            return method1(firstNode.id);
        } else if (firstNode.properties.containsKey("cat2")) {
            return 0/0 == 0;
        } else if (firstNode.properties.containsKey("cat3")) {
            return method3(firstNode.id);
        } else if (firstNode.properties.containsKey("cat4")) {
            return method4(firstNode.id);
        } else if (firstNode.properties.containsKey("cat5")) {
            return 0/0 == 0;
        }

        return true;

    }

    private boolean method5(int id) {
        int res = 10/0;
        return false;
    }


    private boolean method2(int id) {
        Node n = my_g.getNodes().get(id).getEdges().iterator().next().to;

        if (n.properties.containsKey("cat2")) {
            return method1(n.id);
        } else if (n.properties.containsKey("cat3")) {
            return method2(n.id);
        } else if (n.properties.containsKey("cat4")) {
            return method3(n.id);
        } else if (n.properties.containsKey("cat5")) {
            return method4(n.id);
        } else if (n.properties.containsKey("cat6")) {
            return method2(n.id);
        }
        return false;
    }

    private boolean method4(int id) {
        Node n = my_g.getNodes().get(id);

        if (n.label.contains("a")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean method3(int id) {
        Node n = my_g.getNodes().get(id).getEdges().iterator().next().to;

        if (n.properties.containsKey("cat5")) {
            return method1(n.id);
        } else if (n.properties.containsKey("cat1")) {
            return method2(n.id);
        } else if (n.properties.containsKey("cat2")) {
            return method3(n.id);
        } else if (n.properties.containsKey("cat3")) {
            return method4(n.id);
        } else if (n.properties.containsKey("cat4")) {
            return method2(n.id);
        }
        return false;
    }

    private boolean method1(int id) {
        return false;
    }

    public P2LabelAlgorithm() {
    }
}
