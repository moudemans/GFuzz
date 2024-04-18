package tudcomponents;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class MyGraph implements Serializable {

    GraphSchema graphSchema;
    ArrayList<Node> nodes = new ArrayList<>();
//    Random r = new Random();

    public MyGraph() {
    }

    public MyGraph(GraphSchema gs) {
        this.graphSchema = gs;
    }

    public MyGraph(ArrayList<Node> nodes, ArrayList<Edge> edges) {
        this.nodes = nodes;

        for (Edge edge : edges) {
            int from = edge.from;
            int to = edge.to;

            Node node_1 = null;
            Node node_2 = null;

            for (Node node : nodes) {
                if (node.id == from) {
                    node_1 = node;
                }
                if (node.id == to) {
                    node_2 = node;
                }

                if (node_1 != null && node_2 != null) {
                    break;
                }
            }

            if(node_1 == null) {
                System.out.printf("Could not add edge to graph due from node not present. Edge going from [%s] to [%s]", edge.from, edge.to);
            }
            if(node_2 == null) {
                System.out.printf("Could not add edge to graph due to node not present. Edge going from [%s] to [%s]", edge.from, edge.to);
            }

            assert node_1 != null;
            node_1.edges.add(edge);
            assert node_2 != null;
            node_2.edges.add(edge);

        }
    }


    public int nodeCount() {
        return nodes.size();
    }


    public static MyGraph readGraphFromFile(String fileName) {
        MyGraph mg;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            mg = (MyGraph) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            System.err.printf("Could not read file from [%s] \n", fileName);
            System.err.println("Current working dir: " + System.getProperty("user.dir"));
            System.err.printf("Error message: [%s] \n", i.getMessage());
            System.exit(-1);
            return null;
        }
        return mg;
    }

    public static MyGraph readGraphFromGMARK(String inputFile) {

        HashSet<Integer> ids_found = new HashSet<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();

        HashMap<Integer, Integer> id_mask = new HashMap<>();


        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            System.err.println("Could not find the file: " + inputFile);
            throw new RuntimeException(e);
        }

        try {
            String holder = reader.readLine();
            int counter = 1;

            int node_counter = 0;
            while (holder != null) {
                checkGMARKSyntax(holder, counter);
                String[] cols = holder.strip().split(" ");
                String[] from_node = cols[0].split("_");
                String[] to_node = cols[2].split("_");


                String node_label_from = from_node[0];
                int id_from = Integer.parseInt(from_node[1]);

                String node_label_to = to_node[0];
                int id_to = Integer.parseInt(to_node[1]);



                if (!ids_found.contains(id_from)) {
                    ids_found.add(id_from);
                    int mask_id = node_counter;
                    node_counter++;
                    Node n = new Node(mask_id, node_label_from);
                    nodes.add(n);
                    id_mask.put(id_from, mask_id);
                }
                if (!ids_found.contains(id_to)) {
                    ids_found.add(id_to);
                    int mask_id = node_counter;
                    node_counter++;
                    Node n = new Node(mask_id, node_label_to);
                    nodes.add(n);
                    id_mask.put(id_to, mask_id);
                }

                int masked_id_from  = id_mask.get(id_from);
                int masked_id_to  = id_mask.get(id_to);

                Edge e = new Edge(cols[1], masked_id_from, masked_id_to);
                edges.add(e);

                counter++;
                holder = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new MyGraph(nodes, edges);

    }

    private static void checkGMARKSyntax(String holder, int atLine) {
        String[] cols = holder.strip().split(" ");
        if (cols.length != 3) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected <node edge node>");
        }
        String[] from_node = cols[0].split("_");
        if (from_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected from_node to be <label>_<id>");
        }
        String[] to_node = cols[2].split("_");
        if (to_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected to_node to be <label>_<id>");
        }
    }

    public static void writeGraphToFile(String fileName, MyGraph g) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(g);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            System.err.printf("Could not write the graph to [%s]", fileName);
        }
    }


    public static void writeGraphsVF2File(String fileName, ArrayList<MyGraph> graphs) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            for (int i = 0; i < graphs.size(); i++) {
                MyGraph g = graphs.get(i);

                writer.write("t # " + i);
                writer.write("\n");
                for (Node n : g.getNodes()) {
                    writer.write("v " + n.id + " " + n.label);
                    writer.write("\n");
                }
                for (Node n : g.getNodes()) {
                    for (Edge e : n.getOutgoingEdges()) {
                        writer.write("e " + e.from + " " + e.to + " " + e.label);
                        writer.write("\n");
                    }
                }
            }

            writer.write("t # -1" );
            writer.close();
        } catch (IOException i) {
            System.err.printf("Could not write the graph to [%s]", fileName);
        }
    }

    public static void writeGraphVF2File(String fileName, MyGraph g) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            writer.write("t # 0");
            writer.write("\n");
            for (Node n : g.getNodes()) {
                writer.write("v " + n.id + " " + n.label);
                writer.write("\n");
            }
            for (Node n : g.getNodes()) {
                for (Edge e : n.getOutgoingEdges()) {
                    writer.write("e " + e.from + " " + e.to + " " + e.label);
                    writer.write("\n");
                }
            }

            writer.close();
        } catch (IOException i) {
            System.err.printf("Could not write the graph to [%s]", fileName);
        }
    }

    public static MyGraph readGraphFromJSON(String fileName) {
        Gson gson = createGSon();

        try {
            return gson.fromJson(new FileReader(fileName), MyGraph.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void writeGraphToJSON(String fileName, MyGraph g) {
        Gson gson = createGSon();

        try {
            FileWriter fw = new FileWriter(fileName);
            gson.toJson(g, fw);

            // Flush is needed as there are occurrences where not everything was written to the file
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Gson createGSon() {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        for (Node n :
                nodes) {
            res.append("n").append(n.id).append(", (").append(n.outgoingEdgesText()).append(")\n");
        }
        return res.toString();
    }

    public void insertCycle(int size) {
        assert nodes.size() > size;
        Random r = new Random();
        int sourceNode = r.nextInt(nodes.size());
        Set<Integer> candidates = new HashSet<>();
        candidates.add(sourceNode);

        while (candidates.size() < size) {
            int nextNode = r.nextInt(nodes.size());
            candidates.add(nextNode);
        }

        int[] cycle = candidates.stream().mapToInt(value -> value).toArray();

        for (int i = 0; i < cycle.length; i++) {
            Node from = nodes.get(cycle[i]);
            int toIndex = i + 1 >= cycle.length ? 0 : cycle[i + 1];
            from.addEdge(new Edge(cycle[i], toIndex));
        }

    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }


    public Node addNewNode() {
        Node newNode = new Node(nodes.size());
        nodes.add(newNode);
        return newNode;
    }

    public Node getNode(int id) {
        if (nodes.size() > id && nodes.get(id).getID() == id) {
            return nodes.get(id);
        }
        for (Node n :
                nodes) {
            if (n.id == id) {
                return n;
            }
        }
        throw new RuntimeException("Could not find node with id: " + id);
    }

    public Node getNodeOnIndex(int id) {
        if (nodes.size() > id) {
            return nodes.get(id);
        }
        throw new RuntimeException("index given is larger than the size of the nodes in graph: " + id);
    }

    public ArrayList<Node> getConnectedNodes(Node prevItem, String valueRelationshipName) {
        ArrayList<Node> connected_nodes = new ArrayList<>();
        for (Node n :
                nodes) {
            if (n.equals(prevItem)) {
                for (Edge e :
                        n.edges) {
                    if (e.label.equals(valueRelationshipName)) {
                        connected_nodes.add(getNode(e.to));
                        break;
                    }
                }
            }

        }
        return connected_nodes;
    }

    public ArrayList<Node> getConnectedNodes(Node n, ArrayList<String> connectedStudyValueRelName) {
        ArrayList<Node> conn_nodes = new ArrayList<>();
        for (Edge e :
                n.getEdges()) {
            if (connectedStudyValueRelName.contains(e.label)) {
                conn_nodes.add(n);
            }
        }
        return conn_nodes;
    }

    public ArrayList<Edge> getRelationships(Node node) {
        for (Node n :
                nodes) {
            if (n.equals(node)) {
                return new ArrayList<>(n.edges);
            }
        }
        return new ArrayList<>();
    }


    public void addEdge(Edge e) {
        Node n = getNode(e.from);
        Node n2 = getNode(e.to);
        n.addEdge(e);
        n2.addEdge(e);
    }

    public void addNewNodes(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {
            addNewNode();
        }
    }

    public Node addLabeledNode(String label) {
        Node newNode = addNewNode();
        newNode.label = label;
        return newNode;
    }

    public void setSchema(GraphSchema gs) {
        this.graphSchema = gs;
    }

    public GraphSchema getSchema() {
        return this.graphSchema;
    }


    public ArrayList<Node> getNodes(String label) {
        ArrayList<Node> res = new ArrayList<>();
        for (Node n :
                nodes) {
            if (n.label.equals(label)) {
                res.add(n);
            }
        }
        return res;
    }

    public boolean checkSingle(int nodeID, String edgeLabel) {
        Node n = getNode(nodeID);
        ArrayList<Edge> edges = getRelationships(n);
        int counter = 0;
        for (Edge e : edges) {
            if (e.label.equals(edgeLabel)) {
                counter++;
            }
        }
        return counter == 1;
    }

    public OptionalInt getMaxID() {
        return nodes.stream().mapToInt(value -> value.id).max();
    }

    public void addNodes(ArrayList<Node> subsetNodes) {
        for (Node n :
                subsetNodes) {
            addNode(n);
        }
        for (Node n :
                subsetNodes) {
            applyEdges(n);
        }
    }

    private void applyEdges(Node n) {
        for (Edge e :
                n.edges) {
            if (e.to == n.id) {
                Node from = getNode(e.from);
                from.addEdge(e);
            } else {
                Node to = getNode(e.to);
                to.addEdge(e);
            }
        }
    }


    public void addNode(Node n) {
        nodes.add((n));
    }

    public void removeNode(int randomNodeID) {
        Node del_node = getNode(randomNodeID);
        if (nodes.size() > randomNodeID && nodes.get(randomNodeID).getID() == randomNodeID) {
            nodes.remove(randomNodeID);
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).id == randomNodeID) {
                    nodes.remove(i);
                    break;
                }
            }
        }

        for (Edge e :
                del_node.edges) {
            if (e.to == del_node.id) {
                getNode(e.from).removeEdge(e);
            } else {
                getNode(e.to).removeEdge(e);
            }
        }
    }

    public void removeEdge(Edge e) {
        getNode(e.from).removeEdge(e);
        getNode(e.to).removeEdge(e);
    }
}
