package edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudgraphs;

import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;

public class Driver {

    public static void main(String[] args) {

        MyGraph myGraph = MyGraph.readGraphFromJSON("mygraph/src/main/resources/graphs/Coverage_1.json");

        GraphMutator.mutateGraph(myGraph, GraphMutations.MutationMethod.ChangePropertyType);


    }
}
