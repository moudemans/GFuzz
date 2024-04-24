package P9Constraint;

import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;
import java.util.Set;

public class P9ConstraintLogic {


    public int function_1(MyGraph g, int prev_item_id, String value, int new_item_id, boolean multiple_nodes, boolean at_least_one) {
        //System.out.println("Ping.a001");
        Node prev_item = g.getNode(prev_item_id);
        Set<Edge> relationships_to_maintain = prev_item.getEdges();

        //System.out.println("Ping.a002");
        // TODO: should I be using the label or the value of the label?
        String value_relationship_name = null;
        for (Edge e :
                relationships_to_maintain) {
            //System.out.println("Ping.a006");
            if (g.getNode(e.to).label.equals(value)) {
                //System.out.println("Ping.a003");
                value_relationship_name = e.label;
            }
        }

        //System.out.println("Ping.a004");
        for (Edge e :
                relationships_to_maintain) {

            // TODO: add typing (or other condition) to access function 2 or not
            ArrayList<Node> connected_nodes = function_2(g, prev_item, e.label, value, multiple_nodes, at_least_one);
            if(connected_nodes == null) {
                continue;
            }
            //System.out.println("Ping.a005");
            for (Node n :
                    connected_nodes) {
                //System.out.println("Ping.a007");
                g.addEdge(new Edge( e.label, new_item_id, n.id));
            }
        }

        for (Edge e :
                relationships_to_maintain) {
            //System.out.println("Ping.a008");
            if (!g.checkSingle(prev_item_id, e.label)) {
                //System.out.println("Ping.a009");
//                throw new Exception("Not a single");
                return -1;
            };
            if (!g.checkSingle(new_item_id, e.label)) {
                //System.out.println("Ping.a010");
                //System.out.println("check single: " + new_item_id +", " + e.label + ", " + g.getNode(new_item_id).label);
//                throw new Exception("Not a single");
//                System.exit(-1)
                return -1;
            }

        }

        if (value_relationship_name != null) {
            //System.out.println("Ping.a011");
            //TODO add single for whole graph?
            // TODO Add remove graph to api
        }
        //System.out.println("Ping.a012");

        return 1;
    }

    @Fuzz
    public ArrayList<Node> function_2(MyGraph g, Node prev_item, String value_relationship_name, String value, boolean multiple_nodes, boolean at_least_one) {
        //System.out.println("Ping.b001");
        ArrayList<Node> connected_nodes = g.getConnectedNodes(prev_item, value_relationship_name);

        //System.out.println("Ping.b002");
        ArrayList<String> connected_study_value_rel_name = new ArrayList<>();
        if (!connected_nodes.isEmpty()) {
            //System.out.println("Ping.b003");
            ArrayList<Edge> relationships = g.getRelationships(prev_item);

            // Original code verifies the type of the relationship, but we dont have that here.
//            What we can do is add different edge labels like "owns-v1" and "owns-v2"
            for (Edge e :
                    relationships) {
                //System.out.println("Ping.b004");
                if (g.getNode(e.to).label.equals(value)) {
                    //System.out.println("Ping.b005");
                    connected_study_value_rel_name.add(e.label);
                }
            }
        }
        //System.out.println("Ping.b006");
        ArrayList<Node> connected_nodes_with_value = new ArrayList<>();
        for (Node n :
                connected_nodes) {
            //System.out.println("Ping.b007");
            ArrayList<Node> study_values = g.getConnectedNodes(n, connected_study_value_rel_name);

            for (Node n2 :
                    study_values) {
                //System.out.println("Ping.b008");
                if (n2.label.equals(value)) {
                    //System.out.println("Ping.b009");
                    connected_nodes_with_value.add(n);
                }
            }
        }

        //System.out.println("Ping.b010");
        if (multiple_nodes) {
            //System.out.println("Ping.b011");
            return connected_nodes_with_value;
        }

        if (connected_nodes_with_value.size() > 1) {
            //System.out.println("Ping.b012");
//            throw new Exception("Returned multiple connected {connected_rel_name} nodes and was expecting to match just one");
//            System.exit(-1);
            return null;
        }

        if (at_least_one) {
            //System.out.println("Ping.b013");
            if (connected_nodes_with_value.size() == 0) {
                //System.out.println("Ping.b014");
//                throw new Exception("No connected {connected_rel_name} node was found and it was set as mandatory");
//                System.exit(-1);
                return null;
            }
            return connected_nodes_with_value;
        }
        if (!connected_nodes_with_value.isEmpty()) {
            //System.out.println("Ping.b015");
            return connected_nodes_with_value;
        } else {
            //System.out.println("Ping.b016");
            return null;
        }

    }
}