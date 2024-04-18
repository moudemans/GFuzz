package tudgraphs;

import tudcomponents.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class GraphGenerator {

    private static final String DEFAULT_GRAPH_SCHEMA_DIR = "mygraph/src/main/resources/graphSchemas/cardinalityExamples/";
    private static final String DEFAULT_GRAPH_SCHEMA_FILENAME = "simpleschema.json";
    private static final String DEFAULT_GRAPH_SCHEMA_PATH = DEFAULT_GRAPH_SCHEMA_DIR + DEFAULT_GRAPH_SCHEMA_FILENAME;

    private static final String DEFAULT_OUTPUT_DIR = "mygraph/src/main/resources/graphs/";
    private static final String DEFAULT_OUTPUT_FILENAME = "simple.ser";
    private static final String DEFAULT_OUTPUT_FILENAME2 = "simple.json";

    static String GMARK_DIR = "examplesCoverage/src/main/resources/P9-GMARK/";
    static String GMARK_FILE = "P9";
    static int GMARK_DS_COUNT = 9;

    static String GMARK_DIR2 = "examplesCoverage/src/main/resources/VF2/GMARK/";
    static String GMARK_DIR2_out = "examplesCoverage/src/main/resources/VF2/GMARK-FIXED/";
    static String GMARK_FILE2 = "VF2";
    static int GMARK_DS2_COUNT = 50;


    private static final int DEFAULT_GENERATION_TRIES = 1000;

    private static final Random r = new Random();

    static int generationMethod = 5;
    // 0: random simple graph
    // 1: random labeled graph, with cat and val
    // 4: Generate graph from Schema
    // 5: Copy from JSON
    // 6: Copy from GMARK
    static int nodeCount = 10;
    static int edgeCount = 4;

    /**
     * Program arguments:
     * - Input dir
     * - input file name
     * -
     */
    public static void main(String[] args) {
        printStart(args);
        generateGraph();
    }

    private static void generateGraph() {
        MyGraph g = new MyGraph();
        switch (generationMethod) {
            case 0:
                generateRandomSimpleGraph(g, nodeCount, edgeCount);
                break;
            case 1:
                generateRandomLabeledGraph(g, nodeCount, edgeCount);
                break;
            case 4:
                generateGraphFromSchema(g, nodeCount, edgeCount);
                break;
            case 5:
                copyGraphFromJson();
                break;
            case 6:
                copyGraphFromGMARK();
                break;
            case 7:
                copyGraphFromGMARK2();
                break;
            case 8:
                copyGraphFromSer();
                break;
            default:
                throw new RuntimeException("Generation method not recognized: " + generationMethod);
        }

        MyGraph.writeGraphToFile(DEFAULT_OUTPUT_DIR + DEFAULT_OUTPUT_FILENAME, g);
        MyGraph.writeGraphToJSON(DEFAULT_OUTPUT_DIR + DEFAULT_OUTPUT_FILENAME2, g);
    }

    private static void copyGraphFromSer() {
//        String dir = "benchmarks/src/main/resources/P9/P9Examples-MANUAL/";
        String dir = "benchmarks/src/main/java/P10Constraint/fuzz-dir/saved-inputs/";
//        String name = "test01";
        String name = "Coverage_6";
        String extension_json = ".json";
        String extension_ser = ".ser";

        String input_file = dir + name + extension_ser;
        String output_file = dir + name + extension_json;

        MyGraph g = MyGraph.readGraphFromFile(input_file);
        MyGraph.writeGraphToJSON(output_file, g);

    }

    private static void copyGraphToVF2() {


    }


    private static void copyGraphFromGMARK2() {
        String input_file = GMARK_DIR2 + GMARK_FILE2;
        ArrayList<MyGraph> graphs = new ArrayList<>();


        for (int i = 1; i < GMARK_DS2_COUNT; i++) {

            input_file = GMARK_DIR2 + GMARK_FILE2 + "-" + i + "-0.txt";

            String zeroPaddedI = (i + 1) + "";
            if (i < 9) {
                zeroPaddedI = "0" + (i + 1);
            }
            String output_file = GMARK_DIR2_out + "test" + zeroPaddedI + ".ser";

            MyGraph g = MyGraph.readGraphFromGMARK(input_file);
            GraphSchema gs = GraphSchema.readFromFile(GMARK_DIR + "GFuzzGraphSchema.json");
            g.setSchema(gs);
            graphs.add(g);
//            MyGraph.writeGraphToFile(output_file, g);
//            MyGraph.writeGraphVF2File(output_file, g);
        }
        String output_file = GMARK_DIR2_out + "testinput" + ".txt";

        MyGraph.writeGraphsVF2File(output_file, graphs);
    }

    private static void copyGraphFromGMARK() {
        String input_file = GMARK_DIR + GMARK_FILE;

        if (GMARK_DS_COUNT == 1) {
            String output_file = GMARK_DIR + GMARK_FILE.split("\\.")[0] + ".ser";

            MyGraph g = MyGraph.readGraphFromGMARK(input_file);
            MyGraph.writeGraphToFile(output_file, g);
            return;
        }

        for (int i = 0; i < GMARK_DS_COUNT; i++) {

            input_file = GMARK_DIR + GMARK_FILE + "-" + i + ".txt";

            String zeroPaddedI = (i + 1) + "";
            if (i < 9) {
                zeroPaddedI = "0" + (i + 1);
            }
            String output_file = GMARK_DIR + "test" + zeroPaddedI + ".ser";

            MyGraph g = MyGraph.readGraphFromGMARK(input_file);
            GraphSchema gs = GraphSchema.readFromFile(GMARK_DIR + "GFuzzGraphSchema.json");
            g.setSchema(gs);
            MyGraph.writeGraphToFile(output_file, g);
        }
    }

    private static void copyGraphFromJson() {
//        String dir = "mygraph/src/main/resources/graphs/P9Examples-MANUAL/";
        String dir = "benchmarks/src/main/resources/P8/MANUAL/";
        String name = "test3";
        String extension_json = ".json";
        String extension_ser = ".ser";

        String input_file = dir + name + extension_json;
        String output_file = dir + name + extension_ser;

        MyGraph g = MyGraph.readGraphFromJSON(input_file);
        MyGraph.writeGraphToFile(output_file, g);

    }

    /**
     * Generate a graph using the provided graph schema of sizes node count and edge count. The algorithm works as follows:
     * - Generate nCount nodes, randomly selecting labels from the nodeLabels
     * - Generate eCount edges, randomly selecting edges from the edgeLabels
     * - Generate properties for each of the nodes/edges
     */
    private static void generateGraphFromSchema(MyGraph g, int nCount, int eCount) {
        long flag;

        flag = System.currentTimeMillis();
        System.out.println("Loading graph schema from : " + DEFAULT_GRAPH_SCHEMA_PATH);
        GraphSchema gs = GraphSchema.readFromFile(DEFAULT_GRAPH_SCHEMA_PATH);

        if (gs == null) {
            throw new RuntimeException("Something went wrong reading the schema: " + DEFAULT_GRAPH_SCHEMA_PATH);
        }

        g.setSchema(gs);
        System.out.println("Finished in " + computeTimePassed(flag));

        flag = System.currentTimeMillis();
        System.out.printf("Generating [%s] nodes%n", nCount);
        generateNodesFromSchema(g, nCount);
        System.out.println("Finished in " + computeTimePassed(flag));


        flag = System.currentTimeMillis();
        System.out.printf("Generating [%s] edges%n", eCount);
        generateEdgesFromSchema(g, eCount);
        System.out.println("Finished in " + computeTimePassed(flag));

        flag = System.currentTimeMillis();
        System.out.println("Generating properties");
        generatePropertiesFromSchema(g);
        System.out.println("Finished in " + computeTimePassed(flag));

    }

    private static void generatePropertiesFromSchema(MyGraph g) {
        GraphSchema gs = g.getSchema();

        // Go over each node , find the properties and generate a random string for said property
        for (Node n : g.getNodes()) {
            ArrayList<Property> nProps = gs.getNodeProperties().get(n.label);
            for (Property p : nProps) {
                String new_p = generatePropertyValue(p);
                n.properties.put(p.name, new_p);
            }
        }

        // go over each edge, find the properties and generate a random string for said property
        // Go over each node , find the properties and generate a random string for said property
        for (Node n : g.getNodes()) {
            for (Edge e : n.getEdges()) {
                ArrayList<Property> nProps = gs.getEdgeProperties().get(e.label);
                if (nProps == null) {
                    continue;
                }
                for (Property p : nProps) {
                    String new_p = generatePropertyValue(p);
                    e.properties.put(p.name, new_p);
                }
            }
        }
    }

    public static String generatePropertyValue(Property p) {
        if (p.type == Type.INT) {
            int random_val = r.nextInt(Integer.MAX_VALUE) - (Integer.MIN_VALUE / 2);
            return random_val + "";
        } else {
            byte[] array = new byte[10]; // length is bounded by 7
            r.nextBytes(array);
            return new String(array, StandardCharsets.UTF_8);
        }
    }

    private static String computeTimePassed(long flag) {
        long end = System.currentTimeMillis();
        float sec = (end - flag) / 1000F;
        return String.format("(%f2 s)", sec);

    }

    private static void generateEdgesFromSchema(MyGraph g, int eCount) {
        GraphSchema gs = g.getSchema();
        ArrayList<String> edgeLabels = new ArrayList<>(gs.getEdgeLabels());
        ArrayList<Relationship> relationships = new ArrayList<>(gs.getRelationships());
        HashMap<String, Relationship> edgeConnectionMap = new HashMap<>();

        for (Relationship rel :
                relationships) {
            edgeConnectionMap.put(rel.getLabel(), rel);
        }

        int try_counter = 0;
        int generated_edges = 0;
        while (generated_edges < eCount) {
            if (try_counter > DEFAULT_GENERATION_TRIES) {
                System.err.println("Maximum tries reached for generating edges, stopping edge generation...");
                break;
            }

            int randomIndex = r.nextInt(edgeLabels.size());
            String random_edge_label = edgeLabels.get(randomIndex);
            Relationship rel = edgeConnectionMap.get(random_edge_label);
            ArrayList<Node> candidatesFrom = g.getNodes(rel.getFrom());
            ArrayList<Node> candidatesTo = g.getNodes(rel.getTo());

            // If one of the candidate lists is empty, we can't make an edge between the nodes, apparently the nodes do not exist to make it
            if (candidatesFrom.isEmpty() || candidatesTo.isEmpty()) {
                try_counter++;
                continue;
            }

            ArrayList<Edge> candidate_edges = new ArrayList<>();
            for (Node from : candidatesFrom) {
                for (Node to : candidatesTo) {
                    candidate_edges.add(new Edge(rel.getLabel(), from.id, to.id));
                }
            }

            // TODO: With very large graphs, straightforward filtering will become very inefficient
            //Check for cardinality for these candidates, filter out those who can not accept this relationship
            candidate_edges = filterOnCardinality(candidate_edges, rel, g);

            if (candidate_edges.isEmpty()) {
                try_counter++;
                continue;
            }

            try_counter = 0;

            int random_edge_index = r.nextInt(candidate_edges.size());
            Edge random_edge = candidate_edges.get(random_edge_index);
            int random_from_id = random_edge.from;
            int random_to_id = random_edge.to;


            Edge newEdge = new Edge(random_edge_label, random_from_id, random_to_id);
            g.addEdge(newEdge);

            generated_edges++;
        }

    }

    /**
     * * Perform following filters depending on the cardinality of the relationship.
     */
    private static ArrayList<Edge> filterOnCardinality(ArrayList<Edge> candidateEdges, Relationship rel, MyGraph g) {
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

    private static boolean checkNewEdgeCardinality(Edge new_edge, Node from_node, Node to_node, Relationship relationship) {
        switch (relationship.getCardinality()) {
            case MULTI -> {
                return  true;
            }
            case SIMPLE -> {
                return checkSimpleCardinality(new_edge, from_node);
            }
            case MANY2ONE -> {
                return  checkMany2OneCardinality(new_edge, from_node);
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


    public static void generateNodesFromSchema(MyGraph g, int nCount) {
        GraphSchema gs = g.getSchema();
        ArrayList<String> nodeLabels = new ArrayList<>(gs.getNodeLabels());
        // Generate random nodes
        while (g.getNodes().size() < nCount) {
            int randomIndex = r.nextInt(nodeLabels.size());
            String randomNodeLabel = nodeLabels.get(randomIndex);
            g.addLabeledNode(randomNodeLabel);
        }
    }

    private static void printStart(String[] args) {
        System.out.println("Started graph generation with parameters: ");

        if (args.length == 0) {
            System.out.println("\t No parameters given. Using default...");
            System.out.println();
            System.out.println("Graph schema input file: " + DEFAULT_GRAPH_SCHEMA_DIR + DEFAULT_GRAPH_SCHEMA_FILENAME);
            System.out.println("Graph output file: " + DEFAULT_OUTPUT_DIR + DEFAULT_OUTPUT_FILENAME);
        }

        for (int i = 0; i < args.length; i++) {
            String s = String.format("\t arg[%s]: %s", i, args[i]);
            System.out.println(s);
        }
    }

    public static void generateRandomLabeledGraph(MyGraph g, int i, int i1) {
        generateRandomSimpleGraph(g, i, i1);

        for (int j = 0; j < 20; j++) {
            Node n = g.getNodes().get(0);
            String k = "cat" + r.nextInt(5);
            String v = "val" + r.nextInt(100);
            n.properties.put(k, v);
        }
    }



    public static void generateRandomSimpleGraph(MyGraph g, int nodeCount, int edgeCount) {
        g.addNewNodes(nodeCount);

        ArrayList<Node> nodes = g.getNodes();
        for (int i = 0; i < edgeCount; i++) {
            int fromId = r.nextInt(nodes.size());
            int toId = r.nextInt(nodes.size());

            Edge e = new Edge(nodes.get(fromId), nodes.get(toId));
            g.addEdge(e);
        }
    }

    //TODO: fix random parameter
    public static void generateNodeProperties(Node newNode, GraphSchema gs) {
        ArrayList<Property> properties = gs.getNodeProperties().get(newNode.label);
        if (properties.isEmpty()) {
            return;
        }

        for (Property p :
                properties) {
            String value = generatePropertyValue(p);
            newNode.properties.put(p.name, value);
        }

    }




    public static ArrayList<Node> generatePattern(MyGraph g, Pattern p) {

        OptionalInt max_id_opt = g.getMaxID();
        int available_id = max_id_opt.orElse(0);
        // Loop over every relationship, starting at a random one. If all starting relationships have been checked, the generation failed
        ArrayList<Relationship> shuffledRels = new ArrayList<>(p.getRelationships());
        Collections.shuffle(shuffledRels);
        for (Relationship rel : shuffledRels){
            String from_label = rel.getFrom();
            Node start_node = new Node(available_id, from_label);
            available_id++;
            ArrayList<Node> filled_pattern = new ArrayList<>();
            filled_pattern.add(start_node);
            ArrayList<Node> pattern_generated = generateRecPattern(available_id, p, start_node, filled_pattern,g.getSchema());
            if (pattern_generated != null) {
                return pattern_generated;
            }
        }

        return null;
    }

    private static ArrayList<Node> generateRecPattern(int available_id, Pattern p, Node currNodeO, ArrayList<Node> nodesO, GraphSchema gs) {
        if (nodesO.size() == p.getMaxSize()) {

            return nodesO;
        }

        Node currNode = new Node(currNodeO);
        ArrayList<Node> nodes = new ArrayList<>(nodesO);

        ArrayList<Relationship> shuffledRels = new ArrayList<>(p.getRelationships());
        Collections.shuffle(shuffledRels);
        for (Relationship rel : shuffledRels
        ) {
            if (rel.getFrom().equals(currNode.label) || rel.getTo().equals(currNode.label)) {
                Node new_node = null;
                Edge new_edge = null;
                if (rel.getFrom().equals(currNode.label)) {


                    new_node = new Node(available_id, rel.getTo());
                    new_edge = new Edge(rel.getLabel(), currNode.id, new_node.id);
                }
                if (rel.getTo().equals(currNode.label)) {
                    new_node = new Node(available_id, rel.getFrom());
                    new_edge = new Edge(rel.getLabel(), new_node.id, currNode.id);
                }
                if (new_node == null || new_edge == null) {
                    System.err.println("Something went wrong durring the generation of pattern, variables are null which shold not happen at relationship: " +rel );
                    return null;
                }
                //TODO: check cardinality
                boolean validCardinality;
                if (rel.getFrom().equals(currNode.label)) {
                    validCardinality = checkNewEdgeCardinality(new_edge, currNode, new_node, rel);
                }
                else {
                    validCardinality = checkNewEdgeCardinality(new_edge, new_node, currNode, rel);
                }
                if(!validCardinality) {
                    continue;
                }

                available_id++;
                new_node.addEdge(new_edge);
                currNode.addEdge(new_edge);
                nodes.add(new_node);

                ArrayList<Node> candidate = generateRecPattern(available_id, p, new_node, nodes, gs);
                if (candidate != null) {
                    return candidate;
                }
                candidate = generateRecPattern(available_id, p, currNode, nodes, gs);
                if (candidate != null) {
                    return candidate;
                }
            }
        }

        return null;


    }

}
