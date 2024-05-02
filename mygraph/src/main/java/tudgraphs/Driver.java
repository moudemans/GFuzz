package tudgraphs;

import org.apache.commons.lang.SerializationUtils;
import tudcomponents.Edge;
import tudcomponents.MyGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Driver {

    private static final int mutation_count = 10000;
    private static final String input_dir = "examplesCoverage/src/main/resources/P9-GMARK/";
    private static final String output_dir = "examplesCoverage/src/main/resources/P9-GMARK-MUTATED2/";
    private static final String output_file_name = "mutated_";
    private static Random r = new Random();

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


        for (String s :
                out) {
            System.out.print(s);
        }
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
