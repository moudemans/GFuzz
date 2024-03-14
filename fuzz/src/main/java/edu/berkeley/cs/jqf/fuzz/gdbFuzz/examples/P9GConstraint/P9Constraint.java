package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P9GConstraint;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.MyGraph;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.Node;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.Edge;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

@RunWith(JQF.class)
public class P9Constraint {
    MyGraph my_g;

    @Fuzz
    public void run(String inputFile) throws IOException {
        MyGraph g = MyGraph.readGraphFromFile(inputFile);

//        TODO: Additional input file in order to pass more parameters besides the graph
        function_1(g, 0, "", 1);
    }

    public int function_1(MyGraph g, int prev_item_id, String value, int new_item_id) {

        Node prev_item = g.getNodes().get(prev_item_id);
        Set<Edge> relationships_to_maintain = prev_item.getEdges();

        // TODO: should I be using the label or the value of the label?
        String value_relationship_name = null;
        for (Edge e :
                relationships_to_maintain) {
            if (e.to.label.equals(value)) {
                value_relationship_name = e.label;
            }
        }

        for (Edge e :
                relationships_to_maintain) {

            // TODO: add typing (or other condition) to access function 2 or not
            ArrayList<Node> connected_nodes = function_2(g, prev_item, value_relationship_name, value, true, false);
        }


        return 1;
    }

    public ArrayList<Node> function_2(MyGraph g, Node prev_item, String value_relationship_name, String value, boolean multiple_nodes, boolean at_least_one) {

        ArrayList<Node> connected_nodes = my_g.getConnectedNodes(prev_item, value_relationship_name);

        ArrayList<String> connected_study_value_rel_name = new ArrayList<>();
        if (!connected_nodes.isEmpty()) {
            ArrayList<Edge> relationships = my_g.getRelationships(connected_nodes.get(0));

            for (Edge e :
                    relationships) {
                if (e.label.equals(value)) {
                    connected_study_value_rel_name.add(e.label);
                }
            }
        }

        for (Node connected_node :
                connected_nodes) {

//            ArrayList<String> study_values = get


        }




        return null;

    }
}