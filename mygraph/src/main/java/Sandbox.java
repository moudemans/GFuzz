import Components.MyGraph;
import Graphs.GraphMutator;

import java.util.Random;

public class Sandbox {

    public static void main(String[] args) {
        Random r = new Random();
        MyGraph g = MyGraph.readGraphFromFile("mygraph/src/main/resources/graphs/simple.ser");
        GraphMutator.mutateFile("mygraph/src/main/resources/graphs/","simple.ser","mygraph/src/main/resources/graphs/","mutated.ser", null);

    }
}
