package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components;

public class GraphGenerator {

    static String path = "fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/";
    static String fileName = "seed.txt";

    static int[] cycles = new int[]{10,20,30};
    static int generationMethod = 1;
    static int nodeCount = 100;
    static int edgeCount = 50;

    public static void main(String[] args) {
        MyGraph g = new MyGraph();

        switch (generationMethod) {
            case 0: g.generateRandomSimpleGraph(nodeCount, edgeCount);
            case 1: g.generateRandomLabeledGraph(nodeCount, edgeCount);
            case 2: g.generateRandomWeightedGraph(nodeCount, edgeCount, 100, true);
        }
        for (int c :
                cycles) {
            g.insertCycle(c);
        }

        MyGraph.writeGraphToFile(path + fileName,g);

    }
}
