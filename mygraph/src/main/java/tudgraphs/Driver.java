package tudgraphs;

import tudcomponents.MyGraph;
import tudcomponents.Node;
import tudcomponents.Type;

public class Driver {

    public static void main(String[] args) {

        MyGraph myGraph = MyGraph.readGraphFromJSON("mygraph/src/main/resources/test1.json");

        GraphMutator.mutateGraph(myGraph, GraphMutations.MutationMethod.ChangePropertyType);
        Node n = myGraph.getNodes().getFirst();

        for (String key : n.properties.keySet()) {
            System.out.println("key: " + key + " value: " + n.properties.get(key));
        }


        for (String key : n.propertyTypes.keySet()) {
            System.out.println("key: " + key + " value: " + n.propertyTypes.get(key));
            Type t = myGraph.getNodeProperty(n, key).type;
            System.out.println("type: " + t);
        }
        System.out.println("Done!");

    }
}
