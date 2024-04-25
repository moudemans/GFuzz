package tudgraphs;

import tudcomponents.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static util.Util.computeTimePassed;
import static util.Util.dirExists;

public class GraphGenerator {

    private static final String DEFAULT_GRAPH_SCHEMA_DIR = "mygraph/src/main/resources/graphSchemas/cardinalityExamples/";
    private static final String DEFAULT_GRAPH_SCHEMA_FILENAME = "simpleschema.json";
    private static final String DEFAULT_GRAPH_SCHEMA_PATH = DEFAULT_GRAPH_SCHEMA_DIR + DEFAULT_GRAPH_SCHEMA_FILENAME;

    private static final String DEFAULT_OUTPUT_FILENAME = "simple.ser";
    private static final String DEFAULT_OUTPUT_FILENAME2 = "simple.json";



    private static final int DEFAULT_GENERATION_TRIES = 1000;

    private static final Random r = new Random();

    static int generationMethod = 3;
    static String INPUT_DIR_PATH = "tmp/";
    static String OUTPUT_DIR = INPUT_DIR_PATH;
    static String INPUT_FILE_NAME = "";
    // 0: random simple graph
    // 1: random labeled graph, with cat and val
    // 4: Generate graph from Schema
    // 5: Copy from JSON
    // 6: Copy from GMARK
    static int nodeCount = 10;
    static int edgeCount = 4;

    /**
     * Program arguments:
     * - Generation method
     * - Input dir
     * - input file name
     * - output dir
     * - node count
     * - edge count
     */
    public static void main(String[] args) {
        printStart(args);
        System.out.println();
        processProgramArguments(args);
        SelectProcess();
    }

    private static String generationMethodString() {
        switch (generationMethod) {
            case 0:
                return "random Simple";
            case 1:
                return "random Labeled";
            case 2:
                return "Generate From Schema";
            case 3:
                return "Copy From JSON";
            case 4:
                return "Copy From GMARK";
            case 5:
                return "Copy From .ser";
            case 6:
                return "Copy From PGMARK";
            default:
                return "N/A";
        }
    }

    private static void processProgramArguments(String[] args) {
        if (args.length > 0) {
            System.out.println("Applying system parameters");
        }

        if (args.length > 0) {
            try {
                generationMethod = Integer.parseInt(args[0]);
                System.out.printf("\tGeneration method set to [%s], [%s] \n", generationMethod, generationMethodString());
            } catch (Exception e) {
                System.err.printf("Could not read the generation method [%s], please provide a number between [1-6]\n", args[0]);
                System.exit(-1);
            }
        }
        if (args.length > 1) {
            INPUT_DIR_PATH = args[1];
            System.out.printf("\tInput directory set to: %s\n", INPUT_DIR_PATH);
        }
        if (!dirExists(INPUT_DIR_PATH)) {
            System.err.printf("Input directory does not exist [%s]\n", INPUT_DIR_PATH);
            System.exit(-1);
        }

        if (args.length > 2) {
            INPUT_FILE_NAME = args[2];
            System.out.printf("\tInput file name set to [%s] (file should contains string, empty string will use all files in folder) \n", INPUT_FILE_NAME);
        }

        if (args.length > 3) {
            OUTPUT_DIR = args[3];
        } else {
            OUTPUT_DIR = INPUT_DIR_PATH;
        }

        if (!dirExists(OUTPUT_DIR)) {
            System.err.printf("Output directory does not exist [%s]\n", OUTPUT_DIR);
            System.exit(-1);
        }

        if (args.length > 5) {
            try {
                nodeCount = Integer.parseInt(args[4]);
                edgeCount = Integer.parseInt(args[5]);
                System.out.printf("\tgraph size set to:  nodes=[%s], edges=[%s]\n", nodeCount, edgeCount);
            } catch (Exception e) {
                System.err.printf("Could not load the graph size, please use whole numbers for 5th and 6th argument: args[4]=[%s], args[5]=[%s]\n", args[4], args[5]);
                System.exit(-1);
            }
        }
    }


    private static void printStart(String[] args) {
        System.out.println("Started graph generation with parameters: ");

        if (args.length == 0) {
            System.out.println("\t No parameters given. Using default...");
            System.out.println();
            ;
        }

        for (int i = 0; i < args.length; i++) {
            String s = String.format("\t arg[%s]: %s", i, args[i]);
            System.out.println(s);
        }
    }

    private static void SelectProcess() {
        switch (generationMethod) {
            case 0, 1, 2:
                generateGraph();
                break;
            case 3, 4, 5, 6:
                copyGraph();
                break;
            default:
                throw new RuntimeException("Generation method not recognized: " + generationMethod);
        }
    }

    private static void copyGraph() {
        switch (generationMethod) {
            case 3:
                copyGraphFromJson();
                break;
            case 4:
                copyGraphFromGMARK();
                break;
            case 5:
                copyGraphFromSer();
                break;
            case 6:
                copyGraphFromPGMARK();
                break;
            default:
                throw new RuntimeException("Copy method not recognized: " + generationMethod);
        }
    }

    private static void generateGraph() {
        MyGraph g = new MyGraph();
        System.out.println("Selecting graph generation method: " + generationMethod);
        switch (generationMethod) {
            case 0:
                generateRandomSimpleGraph(g, nodeCount, edgeCount);
                break;
            case 1:
                generateRandomLabeledGraph(g, nodeCount, edgeCount);
                break;
            case 2:
                generateGraphFromSchema(g, nodeCount, edgeCount);
                break;
        }

        MyGraph.writeGraphToFile(OUTPUT_DIR + DEFAULT_OUTPUT_FILENAME, g);
        MyGraph.writeGraphToJSON(OUTPUT_DIR + DEFAULT_OUTPUT_FILENAME2, g);
    }



    private static void copyGraphFromPGMARK() {
        String input_extension = ".csv";
        String output_extension = ".ser";
        String output_extension2 = ".json";

        File input_dir = new File(INPUT_DIR_PATH);
        File[] listOfFiles = input_dir.listFiles();
        ArrayList<File> schema_files = new ArrayList<>();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.getName().endsWith("Schema.json") || file.getName().endsWith("schema.json")) {
                schema_files.add(file);
            }
        }
        GraphSchema gs = null;
        if(schema_files.size() == 1) {
            gs = GraphSchema.readFromFile(schema_files.get(0).getPath());
        } else {
            System.err.printf("found [%s] schemas in the input folder. Please add single schema, ending with [schema.json] \n", schema_files.size());
            System.exit(-1);
        }

        List<File> files = loadFilesInDir(input_extension);
        for (File f :
                files) {
            System.out.printf("Loading file: %s\n", f.getPath());
            MyGraph g = MyGraph.readGraphFromPGMARK(f.getPath());
            g.setSchema(gs);

            String output_file_name =f.getPath().replace(input_extension, output_extension);
            String output_file_name2 =f.getPath().replace(input_extension, output_extension2);

            output_file_name = output_file_name.replace(input_dir.getPath(), OUTPUT_DIR);
            output_file_name2 = output_file_name2.replace(input_dir.getPath(), OUTPUT_DIR);

            MyGraph.writeGraphToFile(output_file_name, g);
            MyGraph.writeGraphToJSON(output_file_name2, g);
            System.out.println();
        }

    }
    private static void copyGraphFromGMARK() {
        String input_extension = ".txt";
        String output_extension = ".ser";
        String output_extension2 = ".json";

        File input_dir = new File(INPUT_DIR_PATH);
        File[] listOfFiles = input_dir.listFiles();
        ArrayList<File> schema_files = new ArrayList<>();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.getName().endsWith("Schema.json") || file.getName().endsWith("schema.json")) {
                schema_files.add(file);
            }
        }
        GraphSchema gs = null;
        if(schema_files.size() == 1) {
            gs = GraphSchema.readFromFile(schema_files.get(0).getPath());
        } else {
            System.err.printf("found [%s] schemas in the input folder. Please add single schema, ending with [schema.json]", schema_files.size());
            System.exit(-1);
        }

        List<File> files = loadFilesInDir(input_extension);
        for (File f :
                files) {

            MyGraph g = MyGraph.readGraphFromGMARK(f.getPath());
            g.setSchema(gs);

            String output_file_name =f.getPath().replace(input_extension, output_extension);
            String output_file_name2 =f.getPath().replace(input_extension, output_extension2);
            MyGraph.writeGraphToFile(output_file_name, g);

            MyGraph.writeGraphToJSON(output_file_name2, g);
        }

    }

    private static void copyGraphFromJson() {
        String input_extension = ".json";
        String output_extension = ".ser";
        List<File> files = loadFilesInDir(input_extension);
        for (File f :
                files) {

            MyGraph g = MyGraph.readGraphFromJSON(f.getPath());

            String output_file_name =f.getPath().replace(input_extension, output_extension);
            MyGraph.writeGraphToFile(output_file_name, g);
        }
    }
    private static void copyGraphFromSer() {
        String input_extension = ".ser";
        String output_extension = ".json";
        List<File> files = loadFilesInDir(input_extension);
        for (File f :
                files) {
            MyGraph g = MyGraph.readGraphFromFile(f.getPath());

            String output_file_name =f.getPath().replace(input_extension, output_extension);
            MyGraph.writeGraphToJSON(output_file_name, g);
        }
    }

    private static List<File> loadFilesInDir(String extension) {
        File input_dir = new File(INPUT_DIR_PATH);

        System.out.printf("Files found in dir: [%s] \n", INPUT_DIR_PATH);
        System.out.printf("Filtering on extension: [%s] \n", extension);
        if (!INPUT_FILE_NAME.isEmpty()) {
            System.out.printf("Filtering on file name contains: [%s] \n", INPUT_FILE_NAME);
        }

        File[] listOfFiles = input_dir.listFiles();
        if (listOfFiles == null || listOfFiles.length == 0) {
            System.err.printf("No files found in directory: [%s] \n", input_dir.getAbsolutePath());
            System.exit(-1);
        }

        ArrayList<File> files = new ArrayList<>();
        for (File file : listOfFiles) {
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(extension)) {
                continue;
            }
            if (!INPUT_FILE_NAME.isEmpty()) {
                if (!file.getName().contains(INPUT_FILE_NAME)) {
                    continue;
                }
            }
            System.out.println("\t " + file.getPath() + " --> " + file.getName());

            files.add(file);
        }
        return files;
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
            candidate_edges = MyGraph.filterOnCardinality(candidate_edges, rel, g);

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

    public static void generateNodeProperties(Node newNode, GraphSchema gs) {
        ArrayList<Property> properties = gs.getNodeProperties().get(newNode.label);
        if (properties == null || properties.isEmpty()) {
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
        for (Relationship rel : shuffledRels) {
            String from_label = rel.getFrom();
            Node start_node = new Node(available_id, from_label);
            available_id++;
            ArrayList<Node> filled_pattern = new ArrayList<>();
            filled_pattern.add(start_node);
            ArrayList<Node> pattern_generated = generateRecPattern(available_id, p, start_node, filled_pattern, g.getSchema());
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
                    System.err.println("Something went wrong durring the generation of pattern, variables are null which shold not happen at relationship: " + rel);
                    return null;
                }
                //TODO: check cardinality
                boolean validCardinality;
                if (rel.getFrom().equals(currNode.label)) {
                    validCardinality = MyGraph.checkNewEdgeCardinality(new_edge, currNode, new_node, rel);
                } else {
                    validCardinality = MyGraph.checkNewEdgeCardinality(new_edge, new_node, currNode, rel);
                }
                if (!validCardinality) {
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
