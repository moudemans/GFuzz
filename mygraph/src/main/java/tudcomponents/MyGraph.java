package tudcomponents;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

            if (node_1 == null) {
                System.out.printf("Could not add edge to graph due from node not present. Edge going from [%s] to [%s]", edge.from, edge.to);
            }
            if (node_2 == null) {
                System.out.printf("Could not add edge to graph due to node not present. Edge going from [%s] to [%s]", edge.from, edge.to);
            }

            assert node_1 != null;
            node_1.edges.add(edge);
            assert node_2 != null;
            node_2.edges.add(edge);

        }
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

    public static MyGraph readGraphFromPGMARK(String inputFile) {

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
        int counter = 1;

        try {
            String holder = reader.readLine();

            if (!holder.startsWith("###")) {
                System.err.println("Expected the file to begin with node relationships, indicated by [### NODE RELATIONS ###]");
                System.err.println("First Line: " + holder);
                System.exit(-1);
            }
            holder = reader.readLine();

            int node_counter = 0;
            while (holder != null) {
                if (holder.startsWith("###")) {
                    break;
                }
                if (!checkPGMARKSyntax1(holder, counter)) {
                    counter++;
                    holder = reader.readLine();
                    continue;
                }

                String[] cols = holder.strip().split(",");
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

                HashMap<String,String> attributes = new HashMap<>();
                if (holder.contains("$Attributes$")) {
                    attributes = parsePGMARKEdgeProperties(holder, counter);
                }

                int masked_id_from = id_mask.get(id_from);
                int masked_id_to = id_mask.get(id_to);

                Edge e = new Edge(cols[1], masked_id_from, masked_id_to);
                e.properties = attributes;
                edges.add(e);

                counter++;
                holder = reader.readLine();
            }
            boolean stop = false;
            if (holder == null || !holder.startsWith("###")) {
                System.err.println("Only relationships found in the PG Mark file");
                stop = true;
            } else {
                holder = reader.readLine();

            }
            HashMap<String, String> previous_properties = null;
            String previous_property_key = "";

            while (!stop && holder != null) {
                if (holder.startsWith("###")) {

                    counter++;
                    holder = reader.readLine();
                    continue;
                }
                if (!checkPGMARKSyntax2(holder, counter)) {

                    // Value of property can contain line ending character, in that case it needs to be added to the previous property
                    if (!previous_property_key.isEmpty() && !holder.isEmpty()) {
                        String value = previous_properties.get(previous_property_key);
                        value += "\n" + holder;
                        previous_properties.put(previous_property_key, value);
                    }

                    counter++;
                    holder = reader.readLine();
                    continue;
                }

                String[] cols = holder.strip().split(",");

                // value can contain , character. In that case it needs to be fixed
                String value = cols[2];
                if( cols.length > 3) {
                    for (int i = 3; i < cols.length; i++) {
                        value += "," + cols[i];
                    }
                }

                String[] from_node = cols[0].split("_");
                String node_label_from = from_node[0];
                int id_from = Integer.parseInt(from_node[1]);

                if (!ids_found.contains(id_from)) {
                    ids_found.add(id_from);
                    int mask_id = node_counter;
                    node_counter++;
                    Node n = new Node(mask_id, node_label_from);
                    nodes.add(n);
                    id_mask.put(id_from, mask_id);
                }

                String propertyKey = cols[1];

                nodes.get(id_mask.get(id_from)).properties.put(propertyKey, value);
                previous_properties = nodes.get(id_mask.get(id_from)).properties;
                previous_property_key = propertyKey;


                counter++;
                holder = reader.readLine();

            }

            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Error at line: " + counter);
            throw new RuntimeException(e);
        }

        return new MyGraph(nodes, edges);

    }

    private static HashMap<String, String> parsePGMARKEdgeProperties(String holder, int counter) {

        String[] cols = holder.strip().split(",");
        if (!Objects.equals(cols[3], "$Attributes$")) {
            System.err.printf("Called to parse edge attributes at line [%s] but no attribute column found: %s", counter, holder);
            return new HashMap<>();
        }

        if (cols.length <= 4) {
            System.err.printf("Called to parse edge attributes at line [%s] but no attributes found after $attribute$ column: %s", counter, holder);
            return new HashMap<>();
        }
        if ((cols.length -3) % 2 == 0 ) {
            System.err.printf("Called to parse edge attributes at line [%s] but there is not an even amount of key value pairs: %s", counter, holder);
            return new HashMap<>();
        }
        HashMap<String, String> properties = new HashMap<>();
        //TODO: what to do if there is a , in the property value
        for (int i = 4; i < cols.length; i = i+2) {
            String key = cols[i];
            String value = cols[i+ 1] ;
            properties.put(key, value);
        }
        return properties;

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
                if (!checkGMARKSyntax(holder, counter)) {
                    counter++;
                    holder = reader.readLine();
                    continue;
                }
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

                int masked_id_from = id_mask.get(id_from);
                int masked_id_to = id_mask.get(id_to);

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

    private static boolean checkGMARKSyntax(String holder, int atLine) {
        boolean isValid = true;

        String[] cols = holder.strip().split(" ");
        if (cols.length != 3) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected <node edge node>");
            isValid = false;
        }
        String[] from_node = cols[0].split("_");
        if (from_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected from_node to be <label>_<id>");
            isValid = false;
        }
        String[] to_node = cols[2].split("_");
        if (to_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected to_node to be <label>_<id>");
            isValid = false;
        }
        return isValid;
    }

    private static boolean checkPGMARKSyntax1(String holder, int atLine) {
        boolean isValid = true;
        String[] cols = holder.strip().split(",");
        if ( !(cols.length == 3 || holder.contains("$Attributes$")) ) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected <node,edge,node>");
            isValid = false;
        }
        String[] from_node = cols[0].split("_");
        if (from_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected from_node to be <label>_<id>");
            isValid = false;
        }
        String[] to_node = cols[2].split("_");
        if (to_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected to_node to be <label>_<id>");
            isValid = false;
        }
        return isValid;
    }

    private static boolean checkPGMARKSyntax2(String holder, int atLine) {
        boolean isValid = true;
        String[] cols = holder.strip().split(",");
        if (cols.length < 3) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected <node,property,value>");
            isValid = false;
        }
        String[] from_node = cols[0].split("_");
        if (from_node.length != 2) {
            System.err.println("Incorrect syntax on line [" + atLine + "], expected from_node to be <label>_<id>");
            isValid = false;
        }
        return isValid;
    }


    public static void writeGraphToFile(String fileName, MyGraph g) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(g);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            System.err.printf("Could not write the graph to [%s]\n", fileName);
            System.err.println(i.getMessage());
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

            writer.write("t # -1");
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
            MyGraph g = gson.fromJson(new FileReader(fileName), MyGraph.class);
            g.updateOutgoingIncommingEdges();
            return g;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateOutgoingIncommingEdges() {
        for (Node n : nodes) {
            for (Edge e : n.getOutgoingEdges()) {
                Node to = getNode(e.to);
                if (!to.getEdges().contains(e)) {
                    to.addEdge(e);
                }

            }
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
//        throw new RuntimeException("Could not find node with id: " + id);
        return null;
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


    public ArrayList<Edge> getEdges() {
        HashSet<Edge> edges = new HashSet<>();
        for (Node n :
                nodes) {
            edges.addAll(n.getEdges());
        }
        return new ArrayList<>(edges);
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
            Node n;
            if (e.to == del_node.id) {
                n = getNode(e.from);
            } else {
                n = getNode(e.to);
            }
            if (n != null) {
                n.removeEdge(e);
            }
        }
    }

    public void removeEdge(Edge e) {
        getNode(e.from).removeEdge(e);
        getNode(e.to).removeEdge(e);
    }

    /**
     * * Perform following filters depending on the cardinality of the relationship.
     */
    public static ArrayList<Edge> filterOnCardinality(ArrayList<Edge> candidateEdges, Relationship rel, MyGraph g) {
        switch (rel.getCardinality()) {
            case MULTI -> {
                return candidateEdges;
            }
            case SIMPLE -> {
                return filterSimpleCardinality(candidateEdges, g);
            }
            case MANY2ONE -> {
                return filterMany2OneCardinality(candidateEdges, g);
            }
            case ONE2MANY -> {
                return filterOne2ManyCardinality(candidateEdges, g);
            }
            case ONE2ONE -> {
                return filterOne2OneCardinality(candidateEdges, g);
            }
            default ->
                    throw new RuntimeException("Cardinality generation not yet implemented: " + rel.getCardinality());
        }
    }

    /**
     * Allows at most one incoming and one outgoing edge of such label on any vertex in the graph.
     * The edge label marriedTo is an example with ONE2ONE multiplicity since a person is married to exactly one other person.
     */
    private static ArrayList<Edge> filterOne2OneCardinality(ArrayList<Edge> candidateEdges, MyGraph g) {
        ArrayList<Edge> filtered_candidates = new ArrayList<>();

        for (Edge e : candidateEdges) {
            if (checkOne2OneCardinality(e, g.getNode(e.from), g.getNode(e.to))) {
                filtered_candidates.add(e);
            }
            Set<Edge> current_edges_from_out = g.getNode(e.from).getOutgoingEdges();
            Set<String> edge_labels_from_out = current_edges_from_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

            Set<Edge> current_edges_from_in = g.getNode(e.from).getIncomingEdges();
            Set<String> edge_labels_from_in = current_edges_from_in.stream().map(edge -> edge.label).collect(Collectors.toSet());

            Set<Edge> current_edges_to_in = g.getNode(e.to).getIncomingEdges();
            Set<String> edge_labels_to_in = current_edges_to_in.stream().map(edge -> edge.label).collect(Collectors.toSet());
            Set<Edge> current_edges_to_out = g.getNode(e.to).getOutgoingEdges();
            Set<String> edge_labels_to_out = current_edges_to_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

            if (!edge_labels_from_out.contains(e.label) && !edge_labels_from_in.contains(e.label)
                    && !edge_labels_to_out.contains(e.label) && !edge_labels_to_in.contains(e.label)) {
                filtered_candidates.add(e);
            }
        }
        return filtered_candidates;
    }

    private static boolean checkOne2OneCardinality(Edge e, Node from_node, Node to_node) {
        Set<Edge> current_edges_from_out = from_node.getOutgoingEdges();
        Set<String> edge_labels_from_out = current_edges_from_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

        Set<Edge> current_edges_from_in = from_node.getIncomingEdges();
        Set<String> edge_labels_from_in = current_edges_from_in.stream().map(edge -> edge.label).collect(Collectors.toSet());

        Set<Edge> current_edges_to_in = to_node.getIncomingEdges();
        Set<String> edge_labels_to_in = current_edges_to_in.stream().map(edge -> edge.label).collect(Collectors.toSet());
        Set<Edge> current_edges_to_out = to_node.getOutgoingEdges();
        Set<String> edge_labels_to_out = current_edges_to_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

        if (!edge_labels_from_out.contains(e.label) && !edge_labels_from_in.contains(e.label)
                && !edge_labels_to_out.contains(e.label) && !edge_labels_to_in.contains(e.label)) {
            return true;
        }
        return false;
    }

    /**
     * Allows at most one incoming edge of such label on any vertex in the graph but places no constraint on outgoing edges.
     * The edge label winnerOf is an example with ONE2MANY multiplicity since each contest is won by at most one person but a person can win multiple contests.
     */
    private static ArrayList<Edge> filterOne2ManyCardinality(ArrayList<Edge> candidateEdges, MyGraph g) {
        ArrayList<Edge> filtered_candidates = new ArrayList<>();

        for (Edge e : candidateEdges) {
            if (checkOne2ManyCardinality(e, g.getNode(e.to))) {
                filtered_candidates.add(e);
            }
        }
        return filtered_candidates;
    }

    private static boolean checkOne2ManyCardinality(Edge e, Node to_node) {
        Set<Edge> current_edges = to_node.getIncomingEdges();
        Set<String> edge_labels = current_edges.stream().map(edge -> edge.label).collect(Collectors.toSet());
        if (!edge_labels.contains(e.label)) {
            return true;
        }
        return false;
    }

    /**
     * Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
     * The edge label mother is an example with MANY2ONE multiplicity since each person has at most one mother but mothers can have multiple children.
     */
    private static ArrayList<Edge> filterMany2OneCardinality(ArrayList<Edge> candidateEdges, MyGraph g) {
        ArrayList<Edge> filtered_candidates = new ArrayList<>();

        for (Edge e : candidateEdges) {
            if (checkMany2OneCardinality(e, g.getNode(e.from))) {
                filtered_candidates.add(e);
            }
        }
        return filtered_candidates;
    }

    private static boolean checkMany2OneCardinality(Edge e, Node from_node) {
        Set<Edge> current_edges = from_node.getOutgoingEdges();
        Set<String> edge_labels = current_edges.stream().map(edge -> edge.label).collect(Collectors.toSet());
        if (!edge_labels.contains(e.label)) {
            return true;
        }
        return false;
    }

    /**
     * Allows at most one edge of such label between any pair of vertices. In other words, the graph is a simple graph with respect to the label.
     * Ensures that edges are unique for a given label and pairs of vertices.
     */
    private static ArrayList<Edge> filterSimpleCardinality(ArrayList<Edge> candidateEdges, MyGraph g) {
        ArrayList<Edge> filtered_candidates = new ArrayList<>();

        for (Edge e : candidateEdges) {
            if (checkSimpleCardinality(e, g.getNode(e.from))) {
                filtered_candidates.add(e);
            }
        }
        return filtered_candidates;
    }

    private static boolean checkSimpleCardinality(Edge e, Node from_node) {
        Set<Edge> current_edges = from_node.getOutgoingEdges();
        if (!current_edges.contains(e)) {
            return true;
        }
        return false;
    }

    public static boolean checkNewEdgeCardinality(Edge new_edge, Node from_node, Node to_node, Relationship relationship) {
        switch (relationship.getCardinality()) {
            case MULTI -> {
                return true;
            }
            case SIMPLE -> {
                return checkSimpleCardinality(new_edge, from_node);
            }
            case MANY2ONE -> {
                return checkMany2OneCardinality(new_edge, from_node);
            }
            case ONE2MANY -> {
                return checkOne2ManyCardinality(new_edge, to_node);
            }
            case ONE2ONE -> {
                return checkOne2OneCardinality(new_edge, from_node, to_node);
            }
        }
        return false;
    }

}
