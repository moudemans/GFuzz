package edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudgraphs;

import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.Edge;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.Node;
import org.apache.commons.lang.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphByteAnalysis {

    private static final int mutation_count = 10000;
    private static final String input_dir = "examplesCoverage/src/main/resources/P9-GMARK/";
    private static final String output_dir = "examplesCoverage/src/main/resources/P9-GMARK-MUTATED2/";
    private static final String output_file_name = "mutated_";
    private static Random r = new Random();


    static int g_edge_from_change = 0;
    static int g_edge_to_change = 0;
    static int g_edge_label_change = 0;
    static int g_edge_weight_change = 0;
    static int g_edge_property_count_change = 0;
    static int g_edge_property_key_change = 0;
    static int g_edge_property_value_change = 0;

    static int g_node_id_change = 0;
    static int g_node_label_change = 0;
    static int g_node_edge_in_count_change = 0;
    static int g_node_edge_out_count_change = 0;
    static int g_node_property_count_change = 0;
    static int g_node_property_key_change = 0;
    static int g_node_property_value_change = 0;

    static int undefined_change = 0;

    public static void main(String[] args) {
        ArrayList<String> out = new ArrayList<>();
        MyGraph g = new MyGraph();
        MyGraph.writeGraphToJSON("mygraph/src/main/resources/graphs/random/empty.json",g);
        MyGraph.writeGraphToFile("mygraph/src/main/resources/graphs/random/empty.ser",g);

        empty_graph_example(out);
        out.add("\n");
        new_node_differences(out);
        out.add("\n");

        new_edge_example(out);
        out.add("\n");
        label_edge_example(out);
        out.add("\n");

        new_property_example(out);
        out.add("\n");
        property_change_example(out);

        out.add("\n");
        example_byte_mutation(out);

        for (String s :
                out) {
            System.out.print(s);
        }
    }

    private static void example_byte_mutation(ArrayList<String> out) {
        MyGraph g = MyGraph.readGraphFromJSON("mygraph/src/main/resources/graphs/random/test2.json");
        g.setSchema(null);
        print_graph_statistics(out, g);
        int try_count =500;
        int failed_mutation = 0;
        ArrayList<MyGraph> new_graphs = new ArrayList<>();
        for (int i = 0; i < try_count; i++) {

            try {
                MyGraph new_graph = GraphMutator.ByteMutation(g);

                if(!is_graph_valid(new_graph)) {
                    throw new IOException(" invalid graph");
                }

                new_graphs.add(new_graph);
            } catch (IOException e) {
                failed_mutation++;
            }
        }

        out.add(String.format("\n\n tried to mutate [%s] times, amount of failed mutations [%s]", try_count ,failed_mutation));

        for (MyGraph graph : new_graphs) {
            list_changes_graph(g, graph, out);
        }
        out.add(String.format("\n\n tried to mutate [%s] times, amount of failed mutations [%s]", try_count ,failed_mutation));

        print_totals(out);


    }

    private static boolean is_graph_valid(MyGraph newGraph) {
        for (Node node : newGraph.getNodes()) {
            for (Edge edge : node.getEdges()) {
                if(edge.from != node.id && edge.to != node.id) {
                    return false;
                }
                if (edge.from < 0 || edge.to < 0) {
                    return false;
                }
            }
        }
        for (Node node : newGraph.getNodes()) {
            for (Edge edge : node.getEdges()) {
                if (edge.from != node.id) {
                    Node otherNode = newGraph.getNode(edge.from);
                    if (otherNode == null) {
                        return false;
                    }
                    boolean edge_found = false;
                    for (Edge otherEdge : otherNode.getEdges()) {
                        if (otherEdge.from == edge.from && otherEdge.to == edge.to) {
                            edge_found = true;
                        }
                    }
                    if (!edge_found) {
                        return false;
                    }
                }
                if (edge.to != node.id) {
                    Node otherNode = newGraph.getNode(edge.to);
                    boolean edge_found = false;
                    if (otherNode == null) {
                        return false;
                    }
                    for (Edge otherEdge : otherNode.getEdges()) {
                        if (otherEdge.from == edge.from && otherEdge.to == edge.to) {
                            edge_found = true;
                        }
                    }
                    if (!edge_found) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void list_changes_graph(MyGraph g1, MyGraph g2, ArrayList<String> out) {
        out.add("\n\n *** Graph changes *** \n");
        if(g1.getNodes().size() != g2.getNodes().size()) {
            out.add(String.format("Number of nodes has changed, original has [%s] nodes and mutated has [%s]\n", g1.getNodes().size(), g2.getNodes().size()));
        }
        if(graph_out_edge_count(g1) != graph_out_edge_count(g2)) {
            out.add(String.format("Number of outgoing edges has changed, original has [%s] edges and mutated has [%s]\n", graph_out_edge_count(g1), graph_out_edge_count(g2)));
        }
        if(graph_in_edge_count(g1) != graph_in_edge_count(g2)) {
            out.add(String.format("Number of incomming edges has changed, original has [%s] edges and mutated has [%s]\n", graph_in_edge_count(g1), graph_in_edge_count(g2)));
        }
        int before_size = out.size();
        list_node_changes(g1,g2,out);
        list_edge_changes(g1,g2,out);

        if (before_size == out.size()) {
            out.add("\n No changes");
            undefined_change++;
        }

    }

    private static void print_totals(ArrayList<String> out) {
out.add("\n*** SUMMARY ***");
        out.add(String.format("\ntotal changes of g_edge_from_change : [%s] ",g_edge_from_change ));
        out.add(String.format("\ntotal changes of g_edge_to_change : [%s] ",g_edge_to_change ));
        out.add(String.format("\ntotal changes of g_edge_label_change : [%s] ",g_edge_label_change ));
        out.add(String.format("\ntotal changes of g_edge_weight_change : [%s] ",g_edge_weight_change ));
        out.add(String.format("\ntotal changes of g_edge_property_count_change : [%s] ",g_edge_property_count_change ));
        out.add(String.format("\ntotal changes of g_edge_property_key_change : [%s] ",g_edge_property_key_change ));
        out.add(String.format("\ntotal changes of g_edge_property_value_change : [%s] ",g_edge_property_value_change ));
        out.add(String.format("\ntotal changes of g_node_id_change : [%s] ",g_node_id_change ));
        out.add(String.format("\ntotal changes of g_node_label_change : [%s] ",g_node_label_change ));
        out.add(String.format("\ntotal changes of g_node_edge_in_count_change : [%s] ",g_node_edge_in_count_change ));
        out.add(String.format("\ntotal changes of g_node_edge_out_count_change : [%s] ",g_node_edge_out_count_change ));
        out.add(String.format("\ntotal changes of g_node_property_count_change : [%s] ",g_node_property_count_change ));
        out.add(String.format("\ntotal changes of g_node_property_key_change : [%s] ",g_node_property_key_change ));
        out.add(String.format("\ntotal changes of g_node_property_value_change : [%s] ",g_node_property_value_change ));
    }

    private static void list_edge_changes(MyGraph g1, MyGraph g2, ArrayList<String> out) {
        int edge_from_change = 0;
        int edge_to_change = 0;
        int edge_label_change = 0;
        int edge_weight_change = 0;
        int edge_property_count_change = 0;
        int edge_property_key_change = 0;
        int edge_property_value_change = 0;

        boolean change_found = false;
        for (int i = 0; i < g1.getNodes().size(); i++) {
            ArrayList<String> edge_changes = new ArrayList<>();

            ArrayList<Edge> edges1 = new ArrayList<>(g1.getNodes().get(i).getEdges());
            ArrayList<Edge> edges2 = new ArrayList<>(g2.getNodes().get(i).getEdges());

            if (edges1.size() != edges2.size()) {
                continue;
            }

            for (int j = 0; j < Math.min(edges1.size(),edges2.size()); j++) {
                Edge e1 = edges1.get(j);
                Edge e2 = edges2.get(j);
                if (!e1.label.equals(e2.label)) {
                    edge_changes.add(String.format("\n Label changed of edge"));
                    edge_label_change++;
                }
                if (e1.from != e2.from) {
                    edge_from_change++;
                    edge_changes.add(String.format("\n From id changed of edge, [%s] to [%s]", e1.from, e2.from));
                }
                if (e2.to != e1.to) {
                    edge_to_change++;
                    edge_changes.add(String.format("\n To id changed of edge, [%s] to [%s]", e1.to, e2.to));
                }
                if (e2.weight != e1.weight) {
                    edge_weight_change++;
                    edge_changes.add(String.format("\n weight changed of edge"));
                }
                if (e1.properties.size() != e2.properties.size()) {
                    edge_property_count_change++;
                    edge_changes.add(String.format("\n Properties changed of edge"));
                }
                for(String k : e1.properties.keySet()) {

                    if (!e2.properties.containsKey(k) && e2.properties .size()!= e1.properties.size()) {
                        edge_changes.add(String.format("\t property key [%s] no longer in the new graph\n", k));
                        edge_property_count_change++;
                        continue;
                    }
                    if (!e2.properties.containsKey(k) && e2.properties .size()== e1.properties.size()) {
                        edge_changes.add(String.format("\t property key [%s] has a new key \n", k));
                        edge_property_key_change++;
                        continue;
                    }
                    if (!e1.properties.get(k).equals(e2.properties.get(k))) {
                        edge_property_value_change++;
                        edge_changes.add(String.format("\t property key [%s] has new value. olde [%s] --> new [%s] \n",k, g1.getNodes().get(i).properties.get(k), g2.getNodes().get(i).properties.get(k)));
                    }
                }

            }
            if (!edge_changes.isEmpty()) {
                out.add(String.format("\n Edge of node [%s] has changes: ", i));
                out.addAll(edge_changes);
                change_found = true;
            }
        }

        if (change_found) {
            out.add("\n");
            out.add("\n *** Edge changes Summary *** \n");
            if (edge_label_change != 0) {
                out.add(String.format("\n \t Edge label changes: %s", edge_label_change));
            }
            if (edge_from_change != 0) {
                out.add(String.format("\n \t edge from changes: %s", edge_from_change));
            }
            if (edge_to_change != 0) {
                out.add(String.format("\n \t edge to changes: %s", edge_to_change));
            }
            if (edge_weight_change != 0) {
                out.add(String.format("\n \t edge to changes: %s", edge_weight_change));
            }

            if (edge_property_count_change != 0) {
                out.add(String.format("\n \t Edge property changes: %s", edge_property_count_change));
            }
            if (edge_property_key_change != 0) {
                out.add(String.format("\n \t Edge property key changes: %s", edge_property_key_change));
            }
            if (edge_property_value_change != 0) {
                out.add(String.format("\n \t Edge property value changes: %s", edge_property_value_change));
            }
        }

        g_edge_from_change += edge_from_change;
        g_edge_to_change += edge_to_change;
        g_edge_label_change += edge_label_change;
        g_edge_weight_change += edge_weight_change;
        g_edge_property_count_change += edge_property_count_change;
        g_edge_property_key_change += edge_property_key_change;
        g_edge_property_value_change += edge_property_value_change;

    }

    private static void list_node_changes(MyGraph g1, MyGraph g2, ArrayList<String> out) {
        int node_id_change = 0;
        int node_label_change = 0;
        int node_edge_in_count_change = 0;
        int node_edge_out_count_change = 0;
        int node_property_count_change = 0;
        int node_property_key_change = 0;
        int node_property_value_change = 0;

        boolean change_found = false;

        for (int i = 0; i < g1.getNodes().size(); i++) {
            ArrayList<String> node_changes = new ArrayList<>();

            Node n1 = g1.getNodes().get(i);
            Node n2 = g2.getNodes().get(i);

            if (n1.id != n2.id) {
                node_id_change++;
            }
            if (!n1.label.equals(n2.label)) {
                node_label_change++;
            }
            if(n1.getIncomingEdges().size() != n2.getIncomingEdges().size()) {
                node_edge_in_count_change++;
            }
            if(n1.getOutgoingEdges().size() != n2.getOutgoingEdges().size()) {
                node_edge_out_count_change++;
            }


            for(String k : n1.properties.keySet()) {

                if (!n2.properties.containsKey(k) && n2.properties .size()!= n1.properties.size()) {
                    node_changes.add(String.format("\t property key [%s] no longer in the new graph\n", k));
                    node_property_count_change++;
                    continue;
                }
                if (!n2.properties.containsKey(k) && n2.properties .size()== n1.properties.size()) {
                    node_changes.add(String.format("\t property key [%s] has a new key \n", k));
                    node_property_key_change++;
                    continue;
                }
                if (!n1.properties.get(k).equals(n2.properties.get(k))) {
                    node_property_value_change++;
                    node_changes.add(String.format("\t property key [%s] has new value. olde [%s] --> new [%s] \n",k, g1.getNodes().get(i).properties.get(k), g2.getNodes().get(i).properties.get(k)));
                }
            }


            if (!node_changes.isEmpty()) {
                out.add(String.format("\n Node [%s] has changes: ", i));
                out.addAll(node_changes);
                change_found = true;
            }
        }

        if (change_found) {
            out.add("\n");
            out.add("\n *** Nodes changes Summary *** \n");
            if (node_id_change != 0) {
                out.add(String.format("\n \t node id changes: %s", node_id_change));
            }
            if (node_label_change != 0) {
                out.add(String.format("\n \t node label changes: %s", node_label_change));
            }
            if (node_edge_in_count_change != 0) {
                out.add(String.format("\n \t node edge incount changes: %s", node_edge_in_count_change));
            }
            if (node_edge_out_count_change != 0) {
                out.add(String.format("\n \t node edge outcount changes: %s", node_edge_out_count_change));
            }
            if (node_property_count_change != 0) {
                out.add(String.format("\n \t node property changes: %s", node_property_count_change));
            }
            if (node_property_key_change != 0) {
                out.add(String.format("\n \t node property key changes: %s", node_property_key_change));
            }
            if (node_property_value_change != 0) {
                out.add(String.format("\n \t node property value changes: %s", node_property_value_change));
            }
        }

        g_node_id_change += node_id_change;
        g_node_label_change += node_label_change;
        g_node_edge_in_count_change += node_edge_in_count_change;
        g_node_edge_out_count_change += node_edge_out_count_change;
        g_node_property_count_change += node_property_count_change;
        g_node_property_key_change += node_property_key_change;
        g_node_property_value_change += node_property_value_change;

    }

    private static void print_graph_statistics(ArrayList<String> out, MyGraph g) {


        out.add(String.format("\n number of nodes: %,d \n", g.getNodes().size() ));

        int out_edge_count = graph_out_edge_count(g);
        int in_edge_count = graph_in_edge_count(g);
        out.add(String.format("\n number of out edges: %,d \n", out_edge_count ));
        out.add(String.format("\n number of in edges: %,d \n", in_edge_count ));

    }

    private static int graph_out_edge_count(MyGraph g) {
        int edge_count = 0;
        for (int i = 0; i < g.getNodes().size(); i++) {
            edge_count += g.getNodeOnIndex(i).getOutgoingEdges().size();
        }
        return edge_count;
    }

    private static int graph_in_edge_count(MyGraph g) {
        int edge_count = 0;
        for (int i = 0; i < g.getNodes().size(); i++) {
            edge_count += g.getNodeOnIndex(i).getIncomingEdges().size();
        }
        return edge_count;
    }

    private static void property_change_example(ArrayList<String> out) {
        MyGraph g = new MyGraph();


        g.addNewNode();
        byte[] data_1 = SerializationUtils.serialize(g);

        g.getNode(0).properties.put("test_key", "test_value");
        byte[] data_2 = SerializationUtils.serialize(g);
        g.getNode(0).properties.put("test_key", "test_value2");
        byte[] data_3 = SerializationUtils.serialize(g);

        out.add("\nChange in property value single character \n");
        print_byte_difference(data_2,data_3, out);

        g.getNode(0).properties.put("test_key", "a");
        byte[] data_4 = SerializationUtils.serialize(g);

        out.add("\nChange in property value multiple characters + size: \n");
        print_byte_difference(data_3,data_4, out);
    }

    private static void print_byte_difference(byte[] oldD, byte[] newD, ArrayList<String> out) {
        int min_size = oldD.length < newD.length ? oldD.length : newD.length;

        int counter = 0;
        for (int i = 0; i < min_size; i++) {
            if (oldD[i] != newD[i]) {
//                out.add(String.format("\t [%s]: [%s] <--> [%s] \n", i, data_Edge1[i], data_Edge2[i]));
                counter++;
            }
        }
        out.add(String.format("\t + %s bytes are different \n", counter));
        out.add(String.format("\t + %s difference in byte count: \n", newD.length-oldD.length));
    }

    private static void new_property_example(ArrayList<String> out) {
        MyGraph g = new MyGraph();


        g.addNewNode();
        byte[] data_1 = SerializationUtils.serialize(g);

        g.getNode(0).properties.put("test_key", "test_value");
        byte[] data_2 = SerializationUtils.serialize(g);
        g.getNode(0).properties.put("test_kez", "test_value");
        byte[] data_3 = SerializationUtils.serialize(g);
        g.getNode(0).properties.put("test3", "test_value");
        g.getNode(0).properties.put("test4", "test_value");
        g.getNode(0).properties.put("test5", "test_value");
        g.getNode(0).properties.put("test6", "test_value");
        g.getNode(0).properties.put("test7", "test_value");
        g.getNode(0).properties.put("test8", "test_value");
        g.getNode(0).properties.put("test9", "test_value");
        g.getNode(0).properties.put("test10", "test_value");
        byte[] data_4 = SerializationUtils.serialize(g);

        out.add(String.format("size of graph without properties: [%s] \n", data_1.length));
        out.add(String.format("size of 1  property graph: [%s] - (%,d bytes per node, bigger because the node list is initialised) \n", data_2.length, (data_2.length-data_1.length)/1));
        out.add(String.format("size of 2   node graph: [%s] - (%,d bytes per node) \n", data_3.length, (data_3.length-data_2.length)/1));
        out.add(String.format("size of 10   node graph: [%s] - (%,d bytes per node) \n", data_4.length, (data_4.length-data_2.length)/9));

    }

    private static void label_edge_example(ArrayList<String> out) {
        MyGraph g = new MyGraph();

        g.addNewNode();

        g.addNewNodes(4);

        g.addEdge(new Edge("test", 0, 1));
        byte[] data_Edge1 = SerializationUtils.serialize(g);

        g.addEdge(new Edge("test", 0, 2));
        byte[] data_Edge2 = SerializationUtils.serialize(g);

        Edge e = g.getNodes().get(0).getEdges().stream().findFirst().orElse(null);
        e.label = "tesr";
        byte[] data_Edge_new_label = SerializationUtils.serialize(g);


        out.add(String.format("\nDifferences in graphs with 1 and 2 edges: \n"));
        int counter = 0;

        for (int i = 0; i < data_Edge1.length; i++) {
            if (data_Edge1[i] != data_Edge2[i]) {
//                out.add(String.format("\t [%s]: [%s] <--> [%s] \n", i, data_Edge1[i], data_Edge2[i]));
                counter++;
            }
        }
        out.add(String.format("\t + %s bytes are different \n", counter));
        out.add(String.format("\t + %s extra bytes \n", data_Edge2.length-data_Edge1.length));

        out.add(String.format("\nDifferences in graphs with change in edge label: \n"));
        counter = 0;
        for (int i = 0; i < data_Edge2.length; i++) {
            if (data_Edge2[i] != data_Edge_new_label[i]) {
                counter++;
            }
        }
        out.add(String.format("\t + %s bytes are different \n", counter));
        out.add(String.format("\t + %s extra bytes \n\n", data_Edge_new_label.length-data_Edge2.length));

    }

    private static void new_edge_example(ArrayList<String> out) {
        MyGraph g = new MyGraph();


        g.addNewNode();

        g.addNewNodes(4);
        byte[] data_5 = SerializationUtils.serialize(g);

        g.addEdge(new Edge("test", 0, 1));
        byte[] data_Edge1 = SerializationUtils.serialize(g);

        g.addEdge(new Edge("test", 0, 2));
        byte[] data_Edge2 = SerializationUtils.serialize(g);

        g.addEdge(new Edge("test", 0, 3));

        g.addEdge(new Edge("test", 0, 4));
        g.addEdge(new Edge("test", 0, 0));

        g.addEdge(new Edge("test", 1, 1));
        g.addEdge(new Edge("test", 1, 2));
        g.addEdge(new Edge("test", 1, 3));
        g.addEdge(new Edge("test", 1, 4));
        g.addEdge(new Edge("test", 2, 3));
        byte[] data_Edge10 = SerializationUtils.serialize(g);




        out.add(String.format("size of graph without edges: [%s] \n", data_5.length));
        out.add(String.format("size of 1 edge in graph: [%s]  \n", data_Edge1.length));
        out.add(String.format("size of 2 edge in graph: [%s] - (%,d) \n", data_Edge2.length, (data_Edge2.length-data_Edge1.length)/1));
        out.add(String.format("size of 10 edge in graph: [%s] - (%,d) \n", data_Edge10.length, (data_Edge10.length-data_Edge1.length)/9));
        out.add("Even with the same amount of characters in edge label but one different, there can be already 2 bytes extra\n");

    }

    private static void new_node_differences(ArrayList<String> out) {
        MyGraph g = new MyGraph();

        g.addNewNode();
        byte[] data2 = SerializationUtils.serialize(g);

        g.addNewNode();
        byte[] data3 = SerializationUtils.serialize(g);

        out.add(String.format("Differences in graphs of size 1 and 2: \n"));
        for (int i = 0; i < data2.length; i++) {
            if (data2[i] != data3[i]) {
                out.add(String.format("\t [%s]: [%s] <--> [%s] \n", i, data2[i], data3[i]));
            }
        }
        out.add(String.format("\t\t + %s extra bytes \n\n", data3.length-data2.length));


        g.addNewNodes(98);
        byte[] data4 = SerializationUtils.serialize(g);
        g.addNewNode();
        byte[] data5 = SerializationUtils.serialize(g);

        out.add(String.format("Differences in graphs of size 100 and 101: \n"));
        for (int i = 0; i < data4.length; i++) {
            if (data4[i] != data5[i]) {
                out.add(String.format("\t [%s]: [%s] <--> [%s] \n", i, data4[i], data5[i]));
            }
        }
        out.add(String.format("\t\t + %s extra bytes \n\n", data5.length-data4.length));

    }

    private static void empty_graph_example(ArrayList<String> out) {
        MyGraph g = new MyGraph();

        byte[] data_0 = SerializationUtils.serialize(g);

        g.addNewNode();
        byte[] data_1 = SerializationUtils.serialize(g);

        g.addNewNode();
        byte[] data_2 = SerializationUtils.serialize(g);

        g.addNewNodes(8);
        byte[] data_10 = SerializationUtils.serialize(g);

        g.addNewNodes(90);
        byte[] data_100 = SerializationUtils.serialize(g);

        out.add(String.format("size of empty graph: [%s] \n", data_0.length));
        out.add(String.format("size of 1   node graph: [%s] - (%,d bytes per node, bigger because the node list is initialised) \n", data_1.length, (data_1.length-data_0.length)/1));
        out.add(String.format("size of 2   node graph: [%s] - (%,d bytes per node) \n", data_2.length, (data_2.length-data_1.length)/1));
        out.add(String.format("size of 10  node graph: [%s] - (%,d bytes per node)  \n", data_10.length, (data_10.length-data_1.length)/9));
        out.add(String.format("size of 100 node graph: [%s] - (%,d bytes per node)  \n", data_100.length, (data_100.length-data_1.length)/99));
    }

    private static Set<String> get_files_in_dir() {
        return Stream.of(new File(input_dir).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> file.getName().contains(".ser"))
                .filter(file -> file.getName().contains("test"))
                .map(File::getName)
                .collect(Collectors.toSet());
    }

}
