package tudgraphs;

import tudcomponents.MyGraph;

public class Driver {

    public static void main(String[] args) {

        MyGraph myGraph = MyGraph.readGraphFromJSON("mygraph/src/main/resources/graphs/Coverage_1.json");

        GraphMutator.mutateGraph(myGraph, GraphMutations.MutationMethod.ChangePropertyType);


    }
}
