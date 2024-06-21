package tudgraphs;

import tudcomponents.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import util.Util;

public class GraphMutator {


    // Random objects for random mutations. Seed is stored for reproducibility;
    static private final Random SEED_GENERATOR_FOR_RANDOM = new Random();
    static private long SEED_FOR_RANDOM = SEED_GENERATOR_FOR_RANDOM.nextLong();
    static private Random r = new Random(SEED_FOR_RANDOM);

    private static HashMap<Relationship, Boolean> cardinalityMutationsPerformed = new HashMap<>();
    private static HashSet<Property> propertyNullMutationPerformed = new HashSet<>();


    private static String applyMutationMethod(MyGraph g, GraphMutations.MutationMethod mm, Set<String> breaking_mutations) {
        String mutation_message = "";

        if (breaking_mutations == null) {
            breaking_mutations = new HashSet<>();
        }

        switch (mm) {
            // C1
            case AddNode -> addNodeMutation(g); // M1
            case AddEdge -> addEdgeMutation(g); // M2
            case AddProperty -> addPropertyKey(g); // M3

            // C2
            case RemoveNode -> removeNodeMutation(g); // M4
            case RemoveEdge -> removeEdgeMutation(g); // M5
            case RemoveProperty -> removePropertyKey(g); // M6

            // C3
            case ChangeLabelNode -> changeNodeLabelMutation(g); // M7
            case ChangeLabelEdge -> changeEdgeLabelMutation(g); // M8
            case ChangePropertyKey -> changePropertyKey(g); // M9
            case ChangeEdge -> changeEdgeMutation(g); // M9

            // C4
            case ChangePropertyValue -> changePropertyValue(g); // M10

            // C5
            case BreakSchema -> mutation_message = breakSchemaMutation(g, breaking_mutations);
            case BreakCardinality -> mutation_message = breakCardinalityMutation(g, breaking_mutations); // M11
            case BreakUnique -> mutation_message = breakUniqueMutation(g, breaking_mutations); // M12
            case BreakNull -> mutation_message = breakNullMutation(g, breaking_mutations); // M13
            case ChangePropertyType -> changePropertyType(g); // M14
            case RemoveNodesOfLabel -> removeNodesOfLabelMutation(g); // M15
            case RemoveEdgesOfLabel -> removeEdgesOfLabelMutation(g); // M16

            // DEV
            case CopyNode -> copyNodeMutation(g);
            case CopySubset -> applyCopySubSetMutation(g);

            default ->
                    printString(String.format("Mutation method not implemented: " + mm), System.Logger.Level.WARNING);
        }
        return mutation_message;
    }

    /**
     * Mutate a serialized file which is stored in the given path+input file. The file is written to the output path + file
     */
    public static void mutateFile(String input_path, String input_file, String output_path, String output_file, GraphMutations.MutationMethod mm) {
        // Check if input / output exist
        if (!Util.fileExists(input_path + input_file)) {
            System.err.println("Provided input file does not exists for mutation: " + input_path + input_file);
            return;
        }
        if (!Util.dirExists(output_path)) {
            System.err.println("Provided output dir does not exists for mutation: " + output_path);
            return;
        }

        MyGraph g = MyGraph.readGraphFromFile(input_path + input_file);
        mutateGraph(g, mm);
        MyGraph.writeGraphToFile(output_path + output_file, g);

    }

    public static GraphMutations.MutationMethod mutateGraph(MyGraph g, GraphMutations.MutationMethod mm) {
        assert g.getSchema() != null : "Graph schema not available for graph mutation";

        if (mm == null) {
            mm = selectMutationMethod();
        }
        printString(String.format("Mutation method selected: " + mm), System.Logger.Level.INFO);

        applyMutationMethod(g, mm, null);
        return mm;
    }

    public static String mutateGraphLimit(MyGraph g, Set<String> breaking_mutations) {
        assert g.getSchema() != null : "Graph schema not available for graph mutation";

        GraphMutations.MutationMethod mm = selectMutationMethod();
        printString(String.format("Mutation method selected: " + mm), System.Logger.Level.INFO);

        return applyMutationMethod(g, mm, breaking_mutations);
    }


    private static String breakSchemaMutation(MyGraph g, Set<String> breaking_mutations) {
        // There are 2 types of constraint that can be broken, relationship or property
        ArrayList<GraphMutations.MutationMethod> breaking_mutations_methods = new ArrayList<>();
        breaking_mutations_methods.add(GraphMutations.MutationMethod.BreakCardinality);
        breaking_mutations_methods.add(GraphMutations.MutationMethod.BreakNull);
        breaking_mutations_methods.add(GraphMutations.MutationMethod.BreakUnique);
        breaking_mutations_methods.add(GraphMutations.MutationMethod.ChangePropertyType);

        ArrayList<GraphMutations.MutationMethod> active_mutations = GraphMutations.getActiveMutationMethodList();
        List<GraphMutations.MutationMethod> active_breaking_mutations = breaking_mutations_methods.stream().filter(active_mutations::contains).toList();
        if (active_breaking_mutations.isEmpty()) {
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.BreakSchema, false);
            return "";
        }
        int random_schema_break_mutation = r.nextInt(active_breaking_mutations.size());


        if (breaking_mutations == null) {
            breaking_mutations = new HashSet<>();
        }
        String constraint = applyMutationMethod(g, active_breaking_mutations.get(random_schema_break_mutation), breaking_mutations);

        return active_breaking_mutations.get(random_schema_break_mutation) + "$" + constraint;
    }

    private static String breakNullMutation(MyGraph g, Set<String> breaking_mutations) {
        HashMap<String, ArrayList<Property>> nullprops = new HashMap<>(g.getSchema().getNonNullNodeProperties());


        if (nullprops.isEmpty()) {
            printString("Could not perform a break null property mutation. There are no null properties", System.Logger.Level.WARNING);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.BreakNull, false);
            return "";
        }

        HashMap<String, ArrayList<Property>> filtered_properties = new HashMap<>();
        // Remove properties that are already mutated
        for (String key : nullprops.keySet()) {
            ArrayList<Property> notMutated = new ArrayList<>();
            // Check each property whether is it in the broken mutations
            for (Property p : nullprops.get(key)) {
                if (!breaking_mutations.contains(key + "-" + p.name)) {
                    notMutated.add(p);
                }
            }
            // If there is at least one node which has not been mutated, add it to the filtered list
            if (!notMutated.isEmpty()) {
                filtered_properties.put(key, notMutated);
            }
        }

        ArrayList<String> node_labels = new ArrayList<>(filtered_properties.keySet());
        Collections.shuffle(node_labels, r);

        String node_label = node_labels.getFirst();
        assert !filtered_properties.get(node_label).isEmpty();

        ArrayList<Property> properties = new ArrayList<>(filtered_properties.get(node_label));
        Collections.shuffle(properties, r);


        Property property = properties.getFirst();

        ArrayList<Node> nodes = new ArrayList<>(g.getNodes(node_label));
        if (nodes.isEmpty()) {
            printString(String.format("Could not perform a break unique property mutations, not enough nodes for label: " + node_label), System.Logger.Level.WARNING);
            return "";
        }

        Collections.shuffle(nodes, r);
        Node node1 = nodes.getFirst();

//        if (!node1.properties.containsKey(property.name)) {
//            throw new RuntimeException(String.format("Node [%s] does not have property: [%s]", node1.label, property.name));
//            return;
//        }
//        assert node1.properties.containsKey(property.name);

        //TODO: check if this should be null or ""
        node1.properties.put(property.name, null);

        propertyNullMutationPerformed.add(property);
        return node_label + "-" + property.name;
    }

    private static String breakUniqueMutation(MyGraph g, Set<String> breaking_mutations) {

        //Collect all node+properties
        HashMap<String, ArrayList<Property>> uniqueprops = new HashMap<>(g.getSchema().getUniqueNodeProperties());


        if (uniqueprops.isEmpty()) {
            printString("Could not perform a break unique property mutation. There are no unique properties", System.Logger.Level.WARNING);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.BreakUnique, false);
            return "";
        }

        HashMap<String, ArrayList<Property>> filtered_properties = new HashMap<>();
        // Remove properties that are already mutated
        for (String key : uniqueprops.keySet()) {
            ArrayList<Property> notMutated = new ArrayList<>();
            // Check each property whether is it in the broken mutations
            for (Property p : uniqueprops.get(key)) {
                if (!breaking_mutations.contains(key + "-" + p.name)) {
                    notMutated.add(p);
                }
            }
            // If there is at least one node which has not been mutated, add it to the filtered list
            if (!notMutated.isEmpty()) {
                filtered_properties.put(key, notMutated);
            }
        }


        if (filtered_properties.isEmpty()) {
            printString("Could not perform a break unique property mutation. all the unique properties have already been mutated", System.Logger.Level.WARNING);
            return "";
        }
        ArrayList<String> node_labels = new ArrayList<>(filtered_properties.keySet());
        Collections.shuffle(node_labels, r);

        String node_label = node_labels.getFirst();
        assert !filtered_properties.get(node_label).isEmpty();

        ArrayList<Property> properties = new ArrayList<>(filtered_properties.get(node_label));
        Collections.shuffle(properties, r);

        Property property = properties.getFirst();

        ArrayList<Node> nodes = new ArrayList<>(g.getNodes(node_label));
        if (nodes.size() <= 1) {
            printString(String.format("Could not perform a break unique property mutations, not enough nodes for label: " + node_label), System.Logger.Level.WARNING);
            return "";
        }

        Collections.shuffle(nodes, r);
        Node node1 = nodes.get(0);
        Node node2 = nodes.get(1);

        assert node1.properties.containsKey(property.name);
        assert node2.properties.containsKey(property.name);

        node2.properties.put(property.name, node1.properties.get(property.name));
        return node_label + "-" + property.name;
    }

    private static String breakCardinalityMutation(MyGraph g, Set<String> breaking_mutations) {
        // Select relationships where there is a cardinality
        GraphSchema gs = g.getSchema();
        ArrayList<Relationship> relationships_with_cardinality = new ArrayList<>(gs.getRelationships()
                .stream()
                .filter(relationship -> relationship.getCardinality() != Cardinality.MULTI).toList());


        if (relationships_with_cardinality.isEmpty()) {
            printString("There are no relationships with cardinality which can be broken, no cardinality mutation performed", System.Logger.Level.DEBUG);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.BreakCardinality, false);
            return "";
        }

        Set<String> cardinality_mutations_applied = breaking_mutations.stream().filter(s -> s.contains(GraphMutations.MutationMethod.BreakCardinality.toString())).collect(Collectors.toSet()); // Filter on cardinality mutation messages
        ArrayList<Relationship> filtered_relationships = new ArrayList<>(relationships_with_cardinality.stream().filter(relationship -> !cardinality_mutations_applied.contains(relToConstraintString(relationship))).toList());

        if (filtered_relationships.isEmpty()) {
            printString("All relationships with cardinality have already been broken, no mutation performed", System.Logger.Level.DEBUG);
            return "";
        }

        int random_relationship_index = r.nextInt(relationships_with_cardinality.size());
        Relationship rel = relationships_with_cardinality.get(random_relationship_index);

        breakCardinality(g, rel);

        cardinalityMutationsPerformed.put(rel, true);
        return relToConstraintString(rel);
    }

    private static String relToConstraintString(Relationship rel) {
        return "(" + rel.getFrom() + "-" + rel.getLabel() + "-" + rel.getTo() + "):" + rel.getCardinality();
    }


    // SIMPLE  -->  Allows at most one edge of such label between any pair of vertices. MUTATION: add two the same edges between any two nodes
//    MANY2ONE -->  Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
//    ONE2MANY --> Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
//    ONE2ONE  -->  Allows at most one incoming and one outgoing edge of such label on any vertex in the graph.
    private static void breakCardinality(MyGraph g, Relationship rel) {
        switch (rel.getCardinality()) {
            case MULTI -> {
            }
            case SIMPLE -> breakSimpleCardinality(g, rel);
            case MANY2ONE -> breakMany2OneCardinality(g, rel);
            case ONE2MANY -> breakOne2ManyCardinality(g, rel);
            case ONE2ONE -> breakOne2OneCardinality(g, rel);
            case NxM -> breakN2MCardinality(g, rel);
            default -> throw new RuntimeException("Cardinality mutation not yet implemented: " + rel.getCardinality());
        }

    }

    private static void breakOne2OneCardinality(MyGraph g, Relationship rel) {
        if (r.nextBoolean()) {
            breakOne2ManyCardinality(g, rel);
        } else {
            breakMany2OneCardinality(g, rel);
        }
    }

    private static void breakOne2ManyCardinality(MyGraph g, Relationship rel) {
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_nodes = g.getNodes(from_label);
        ArrayList<Node> to_nodes = g.getNodes(to_label);


        if (from_nodes.size() < 2) {
            printString(String.format("Not enough from nodes found to break one2Many cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            return;
        }

        if (to_nodes.isEmpty()) {
            printString(String.format("No to nodes found to break one2Many cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            return;
        }

        int random_from_node_index = r.nextInt(from_nodes.size());
        int random_from_node_index2 = r.nextInt(from_nodes.size());
        int random_to_node_index = r.nextInt(to_nodes.size());

        // In case the same to node is selected
        while (random_from_node_index2 == random_from_node_index) {
            random_from_node_index2 = r.nextInt(from_nodes.size());
        }

        Node from_node = from_nodes.get(random_from_node_index);
        Node from_node2 = from_nodes.get(random_from_node_index2);
        Node to_node = to_nodes.get(random_to_node_index);

        Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
        Edge e2 = new Edge(rel.getLabel(), from_node2.id, to_node.id);

        g.addEdge(e1);
        g.addEdge(e2);

    }

    private static void breakN2MCardinality(MyGraph g, Relationship rel) {
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_nodes = g.getNodes(from_label);
        ArrayList<Node> to_nodes = g.getNodes(to_label);

        if (rel.getN() <= 0) {
            printString(String.format("Relationship has NxM cardinality, but size of N is smaller than 0 [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
        }

        if (rel.getM() <= 0) {
            printString(String.format("Relationship has NxM cardinality, but size of M is smaller than 0 [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
        }

        if (from_nodes.size() < rel.getN()) {
            printString(String.format("Not enough from nodes found to break NxM cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            return;
        }
        if (to_nodes.size() < rel.getM()) {
            printString(String.format("Not enough to nodes found to break NxM cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            return;
        }

        if(r.nextBoolean()) {
            HashSet<Integer> random_from_nodes = new HashSet<>();
            int tries = 0;
            while (random_from_nodes.size() <= rel.getN()) {
                random_from_nodes.add(r.nextInt(from_nodes.size()));

                tries++;
                if (tries > Math.max(100, rel.getN() * 2)) {
                    printString(String.format("Exceeded amount of tries to collect from nodes to break NxM cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
                    return;
                }
            }

            int random_to_node_index = r.nextInt(to_nodes.size());
            Node to_node = to_nodes.get(random_to_node_index);


            for (Integer id : random_from_nodes) {
                Node from_node = from_nodes.get(id);
                Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
                g.addEdge(e1);
            }
        } else {
            HashSet<Integer> random_to_nodes = new HashSet<>();
            int tries = 0;
            while (random_to_nodes.size() <= rel.getN()) {
                random_to_nodes.add(r.nextInt(to_nodes.size()));

                tries++;
                if (tries > Math.max(100, rel.getM() * 2)) {
                    printString(String.format("Exceeded amount of tries to collect from nodes to break NxM cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
                    return;
                }
            }

            int random_from_node_index = r.nextInt(from_nodes.size());
            Node from_node = from_nodes.get(random_from_node_index);


            for (Integer id : random_to_nodes) {
                Node to_node = to_nodes.get(id);
                Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
                g.addEdge(e1);
            }
        }
    }

    /**
     * Allows at most one outgoing edge of such label on any vertex in the graph but places no constraint on incoming edges.
     * The edge label mother is an example with MANY2ONE multiplicity since each person has at most one mother but mothers can have multiple children.
     */
    private static void breakMany2OneCardinality(MyGraph g, Relationship rel) {
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_nodes = g.getNodes(from_label);
        ArrayList<Node> to_nodes = g.getNodes(to_label);


        if (from_nodes.isEmpty()) {
            printString(String.format("No from nodes found to break many2One cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            // TODO: make from node
            return;
        }

        if (to_nodes.size() < 2) {
            //TODO: make to node

            printString(String.format("Not enough to nodes found to break many2One cardinality for relationship [%s] -- [%s] --> [%s]", rel.getFrom(), rel.getLabel(), rel.getTo()), System.Logger.Level.DEBUG);
            return;
        }

        int random_from_node_index = r.nextInt(from_nodes.size());
        int random_to_node_index = r.nextInt(to_nodes.size());
        int random_to_node_index2 = r.nextInt(to_nodes.size());

        // In case the same to node is selected
        while (random_to_node_index2 == random_to_node_index) {
            random_to_node_index2 = r.nextInt(to_nodes.size());
        }

        Node from_node = from_nodes.get(random_from_node_index);
        Node to_node = to_nodes.get(random_to_node_index);
        Node to_node2 = to_nodes.get(random_to_node_index2);

        Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
        Edge e2 = new Edge(rel.getLabel(), from_node.id, to_node2.id);

        g.addEdge(e1);
        g.addEdge(e2);
    }

    //
    private static void breakSimpleCardinality(MyGraph g, Relationship rel) {
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_nodes = g.getNodes(from_label);
        ArrayList<Node> to_nodes = g.getNodes(to_label);

        if (from_nodes.isEmpty()) {
            printString(String.format("No from nodes found to break simple cardinality for relationship [%s]", rel), System.Logger.Level.DEBUG);
            return;
        }

        if (to_nodes.isEmpty()) {
            printString(String.format("No to nodes found to break simple cardinality for relationship [%s]", rel), System.Logger.Level.DEBUG);
            return;
        }

        int random_from_node_index = r.nextInt(from_nodes.size());
        int random_to_node_index = r.nextInt(to_nodes.size());

        Node from_node = from_nodes.get(random_from_node_index);
        Node to_node = to_nodes.get(random_to_node_index);

        Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
        Edge e2 = new Edge(rel.getLabel(), from_node.id, to_node.id);

        printString(String.format("Breaking SIMPLE cardinality between node [%s] and [%s] by adding two edges with label [%s]", from_node.id, to_node.id, rel.getLabel()), System.Logger.Level.INFO);

        g.addEdge(e1);
        g.addEdge(e2);
    }


    private static void addPropertyKey(MyGraph g) {
        GraphSchema gs = g.getSchema();
        ArrayList<String> node_labels = new ArrayList<>(gs.getNodeProperties().keySet());
        ArrayList<String> edge_labels = new ArrayList<>(gs.getEdgeProperties().keySet());

        String label = generateString(50);
        String value = generateString(15);

        int pool_size = node_labels.size() + edge_labels.size();

        if (pool_size > 0 && r.nextBoolean()) {
            int random_index = r.nextInt(pool_size);

            String random_element_label = (random_index < node_labels.size()) ? node_labels.get(random_index) : edge_labels.get(random_index - node_labels.size());
            ArrayList<Property> random_element = (random_index < node_labels.size()) ? gs.getNodeProperties().get(random_element_label) : gs.getEdgeProperties().get(random_element_label);
            if (!random_element.isEmpty()) {
                Property random_property = random_element.get(r.nextInt(random_element.size()));
                label = random_property.name;
                value = GraphGenerator.generatePropertyValue(random_property);
            }
        }
        ArrayList<Node> nodes = g.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        int random_index = r.nextInt(nodes.size());
        Node random_node = nodes.get(random_index);

        //if true, add to node properties, otherwise to edge
        ArrayList<Edge> edges = new ArrayList<>(random_node.getEdges());
        if (r.nextBoolean() && !edges.isEmpty()) {
            Edge edge = edges.get(r.nextInt(edges.size()));
            if (edge.properties == null) {
                edge.properties = new HashMap<>();
            }
            edge.properties.put(label, value);
        } else {
            if (random_node.properties == null) {
                random_node.properties = new HashMap<>();
            }
            random_node.properties.put(label, value);
        }
    }

    private static void removePropertyKey(MyGraph g) {
        ArrayList<Node> nodes = getNodesWithProperties(g);
        ArrayList<Edge> edges = getEdgesWithProperties(g);

        int pool_size = 0;
        int node_pool_size = 0;
        int edge_pool_size = 0;

        if (nodes != null) {
            node_pool_size = nodes.size();
            pool_size += node_pool_size;
        }
        if (edges != null) {
            edge_pool_size = edges.size();
            pool_size += edge_pool_size;
        }

        if (pool_size == 0) {
            printString("No properties on node which can be removed", System.Logger.Level.WARNING);
//            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.RemoveProperty, false);
            return;
        }

        int random_index = r.nextInt(pool_size);

        if (random_index < node_pool_size) {
            Node n = nodes.get(random_index);
            ArrayList<String> properties = new ArrayList<>(n.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String value_to_be_removed = n.properties.get(prop_key);
            n.properties.remove(prop_key);

            printString(String.format("Removed Property [%s] with value [%s], from node [%s] \n", prop_key, value_to_be_removed, n.id), System.Logger.Level.INFO);

        } else {
            Edge e = edges.get(random_index - node_pool_size);
            ArrayList<String> properties = new ArrayList<>(e.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String value_to_be_removed = e.properties.get(prop_key);
            e.properties.remove(prop_key);

            printString(String.format("Removed Property [%s] with value [%s], from edge [%s] -- [%s] --> [%s] \n", prop_key, value_to_be_removed, e.from, e.labels, e.to), System.Logger.Level.INFO);
        }
    }


    private static void removeEdgeMutation(MyGraph g) {
        Node n = getRandomNodeWithEdges(g);
        if (n == null) {
            printString("Could not apply removeEdgeMutation an edge due to there not being nodes with edge", System.Logger.Level.INFO);
            return;
        }
        Edge e = getRandomEdge(n);

        if (e == null) {
            printString("Could not apply removeEdgeMutation an edge due to there not being edges on the selected node", System.Logger.Level.WARNING);
            return;
        }

        printString(String.format("Removed Edge [%s], from [%s] --> to [%s] \n", e.labels, e.from, e.to), System.Logger.Level.INFO);
        g.removeEdge(e);
    }

    private static Edge getRandomEdge(Node random_node) {
        ArrayList<Edge> edges = new ArrayList<>(random_node.getEdges());

        if (edges.isEmpty()) {
            return null;
        }

        int random_edge_index = r.nextInt(edges.size());
        return edges.get(random_edge_index);
    }

    private static Node getRandomNodeWithEdges(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        ArrayList<Node> filtered_nodes = new ArrayList<>(nodes.stream().filter(node -> !node.getEdges().isEmpty()).toList());

        if (filtered_nodes.isEmpty()) {
            printString("There are no nodes with edges that can be removed", System.Logger.Level.INFO);
            return null;
        }

        int random_node_index = r.nextInt(filtered_nodes.size());
        int node_id = filtered_nodes.get(random_node_index).id;
        return g.getNode(node_id);
    }

    private static void changeNodeLabelMutation(MyGraph g) {
        Node n = getRandomNode(g);

        if (n == null) {
            printString("Could not apply change node label due to not being nodes ", System.Logger.Level.INFO);
            return;
        }

        ArrayList<String> old_labels = n.labels;
        int random_label_index = r.nextInt(old_labels.size());
        String old_label = old_labels.get(random_label_index);
        String new_label = generateString(old_label.length() * 2);
        n.labels.set(random_label_index, new_label);

        printString(String.format("Changed label on Node [%s], from [%s] --> to [%s] \n", n.id + "_" + n.labels, old_label, n.labels), System.Logger.Level.INFO);
    }

    private static void changeEdgeLabelMutation(MyGraph g) {
        Node n = getRandomNodeWithEdges(g);

        if (n == null) {
            printString("Could not apply changeEdgeMutation an edge due to there not being nodes with edge", System.Logger.Level.INFO);
            return;
        }

        Edge e = getRandomEdge(n);

        if (e == null) {
            printString("Could not apply changeEdgeMutation an edge due to there not being edges on the selected node", System.Logger.Level.WARNING);
            return;
        }

        ArrayList<String> old_labels = e.labels;
        int random_label_index = r.nextInt(old_labels.size());
        String old_label = old_labels.get(random_label_index);
        String new_label = generateString(old_label.length() * 2);
        e.labels.set(random_label_index, new_label);

        printString(String.format("Changed Edge label on Node [%s], from [%s] --> to [%s] \n", n.id + "_" + n.labels, old_label, e.labels), System.Logger.Level.INFO);
    }

    private static void changeEdgeMutation(MyGraph g) {
        Node n = getRandomNodeWithEdges(g);

        if (n == null) {
            printString("Could not apply changeEdgeMutation an edge due to there not being nodes with edge", System.Logger.Level.INFO);
            return;
        }

        Edge e = getRandomEdge(n);

        if (e == null) {
            printString("Could not apply changeEdgeMutation an edge due to there not being edges on the selected node", System.Logger.Level.WARNING);
            return;
        }

        int old_from = e.from;
        int old_to = e.to;
        int new_from = old_from;
        int new_to = old_to;
        while (new_from == old_from) {
            new_from = r.nextInt(g.getMaxID().orElse(old_from + 1));
        }
        while (new_to == old_to) {
            new_to = r.nextInt(g.getMaxID().orElse(old_to + 1));
        }
        e.from = new_from;
        e.to = new_to;

        printString(String.format("Changed Edge reference on Node [%s], from [%s] --> [%s] to [%s] --> [%s] \n", n.id + "_" + n.labels, old_from, new_from, old_to, new_to), System.Logger.Level.INFO);
    }


    public  static ArrayList<Node> getNodesWithProperties(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        ArrayList<Node> filtered_nodes = new ArrayList<>(nodes.stream().filter(node -> !node.properties.isEmpty()).toList());

        if (filtered_nodes.isEmpty()) {
            printString("There are no nodes with properties", System.Logger.Level.INFO);
            return null;
        }
        return filtered_nodes;
    }


    private static ArrayList<Edge> getEdgesWithProperties(MyGraph g) {
        ArrayList<Edge> edges = g.getEdges();
        ArrayList<Edge> filtered_edges = new ArrayList<>(edges.stream().filter(e -> !e.properties.isEmpty()).toList());

        if (filtered_edges.isEmpty()) {
            printString("There are no edges with properties", System.Logger.Level.INFO);
            return null;
        }
        return filtered_edges;
    }


    private static Node getRandomNode(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        int random_node_index = r.nextInt(nodes.size());
        return g.getNodeOnIndex(random_node_index);
    }


    private static String generateString(int maxSize) {
        int size = r.nextInt(maxSize) + 1;
        byte[] array = new byte[size]; // length is bounded by 7
        r.nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    private static void addEdgeMutation(MyGraph g) {
        GraphSchema gs = g.getSchema();

        ArrayList<Relationship> rels = new ArrayList<>(gs.getRelationships());

        if (rels.isEmpty()) {
            printString("Add edge mutation failed, no relationships defined in schema", System.Logger.Level.WARNING);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.AddEdge, false);
            return;
        }

        // Select random relationship to generate
        Collections.shuffle(rels, r);
        Relationship rel = rels.getFirst();
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_candidates = g.getNodes(from_label);
        ArrayList<Node> to_candidates = g.getNodes(to_label);

        if (from_candidates.isEmpty() || to_candidates.isEmpty()) {
            printString(String.format("Could not add a new edge, to few available nodes - from=[%s]:[%s], to=[%s]:[%s] \n", from_label, from_candidates.size(), to_label, to_candidates.size()), System.Logger.Level.DEBUG);
            return;
        }

        int random_from_index = r.nextInt(from_candidates.size());
        int random_to_index = r.nextInt(to_candidates.size());

        Node from = from_candidates.get(random_from_index);
        Node to = to_candidates.get(random_to_index);

        Edge new_edge = new Edge(rel.getLabel(), from.id, to.id);

        // Create node, add properties
        GraphGenerator.generateEdgeProperties(new_edge, gs);

        g.addEdge(new_edge);
        printString(String.format("Add Edge [%s], from [%s] --> to [%s] \n", new_edge.labels, new_edge.from, new_edge.to), System.Logger.Level.INFO);
    }


    private static void changePropertyType(MyGraph g) {
        ArrayList<Node> nodes = getNodesWithProperties(g);
        ArrayList<Edge> edges = getEdgesWithProperties(g);

        int pool_size = 0;
        int node_pool_size = 0;
        int edge_pool_size = 0;

        if (nodes != null) {
            node_pool_size = nodes.size();
            pool_size += node_pool_size;
        }
        if (edges != null) {
            edge_pool_size = edges.size();
            pool_size += edge_pool_size;
        }

        if (pool_size == 0) {
            printString("No properties on node/edge which a property can be changed", System.Logger.Level.WARNING);
//            System.exit(-1);
//            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.ChangePropertyType, false);
            return;
        }

        int random_index = r.nextInt(pool_size);

        if (random_index < node_pool_size) {
            Node n = nodes.get(random_index);

            HashMap<String, String> non_string_properties = new HashMap<>();
            for (String p_label : n.properties.keySet()) {
                non_string_properties.put(p_label, n.properties.get(p_label));
            }

            ArrayList<String> properties = new ArrayList<>(non_string_properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);


            String old_value = n.properties.get(prop_key);

            Property old_prop = g.getNodeProperty(n, prop_key);
            Type old_type = old_prop.type;

            Type[] possible_types = Type.values();
            Type new_type = possible_types[r.nextInt(possible_types.length)];

            while (new_type == old_type) {
                new_type = possible_types[r.nextInt(possible_types.length)];
            }

            Property new_prop = new Property(prop_key, new_type, old_prop.isUnique, old_prop.isNotNull, old_prop.valueIsConstraint, old_prop.min, old_prop.max);
            String new_value = GraphGenerator.generatePropertyValue(new_prop);

            n.properties.put(prop_key, new_value);

            if (n.propertyTypes == null) {
                n.propertyTypes = new HashMap<>();
            }
            n.propertyTypes.put(prop_key, new_type);

            printString(String.format("Changed Property type of property [%s] with value [%s] to value [%s] for node [%s] \n", prop_key, old_value, new_value, n.id), System.Logger.Level.INFO);

        } else {
            assert edges != null;
            Edge e = edges.get(random_index - node_pool_size);


            HashMap<String, String> non_string_properties = new HashMap<>();
            for (String p_label : e.properties.keySet()) {
                non_string_properties.put(p_label, e.properties.get(p_label));

            }


            ArrayList<String> properties = new ArrayList<>(non_string_properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String old_value = e.properties.get(prop_key);

            Type old_prop = g.getEdgeProperty(e, prop_key).type;

            Type[] possible_types = Type.values();
            Type new_type = possible_types[r.nextInt(possible_types.length)];

            while (new_type == old_prop) {
                new_type = possible_types[r.nextInt(possible_types.length)];
            }


            Property new_prop = new Property(prop_key, new_type);
            String new_value = GraphGenerator.generatePropertyValue(new_prop);

            e.properties.put(prop_key, new_value);
            if (e.propertyTypes == null) {
                e.propertyTypes = new HashMap<>();
            }
            e.propertyTypes.put(prop_key, new_type);

            printString(String.format("Changed Property type of property [%s] with value [%s] to value [%s] for edge [%s] -- [%s] --> [%s]\n", prop_key, old_value, new_value, e.from, e.labels, e.to), System.Logger.Level.INFO);
        }
    }

    public static void changePropertyKey(MyGraph g) {
        ArrayList<Node> nodes = getNodesWithProperties(g);
        ArrayList<Edge> edges = getEdgesWithProperties(g);

        int pool_size = 0;
        int node_pool_size = 0;
        int edge_pool_size = 0;

        if (nodes != null) {
            node_pool_size = nodes.size();
            pool_size += node_pool_size;
        }
        if (edges != null) {
            edge_pool_size = edges.size();
            pool_size += edge_pool_size;
        }

        if (pool_size == 0) {
            printString("No properties on node whith a property key that can be changed", System.Logger.Level.WARNING);
//            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.ChangePropertyKey, false);
            return;
        }

        int random_index = r.nextInt(pool_size);

        if (random_index < node_pool_size) {
            Node n = nodes.get(random_index);
            ArrayList<String> properties = new ArrayList<>(n.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String new_prop_key = generateString(prop_key.length() * 2);

            String value = n.properties.get(prop_key);
            n.properties.put(new_prop_key, value);
            n.properties.remove(prop_key);


            if (n.propertyTypes == null) {
                n.propertyTypes = new HashMap<>();
            }
            n.propertyTypes.put(new_prop_key, g.getNodeProperty(n, prop_key).type);
            n.propertyTypes.remove(prop_key);

            ArrayList<String> labels = new ArrayList<>(n.labels);
            Collections.shuffle(labels);
            int label_index= -1;
            for (int i = 0; i < labels.size(); i++) {
                if (g.getSchema().getNodeProperties().get(labels.get(i)) == null) {
                    label_index = i;
                }
            }



            if (label_index == -1) {
                printString("No properties in schema on node " + n.labels, System.Logger.Level.WARNING);
                return;
            }
            Property p = g.getSchema().getNodeProperties().get(labels.get(label_index)).stream().filter(property -> property != null && property.name.equals(prop_key)).findFirst().orElse(null);

            if (p != null) {
                Property new_prop = new Property(new_prop_key, p.type, p.isUnique, p.isNotNull, p.valueIsConstraint, p.min, p.max);
                g.getSchema().getNodeProperties().get(labels.get(label_index)).add(new_prop);
            }

            printString(String.format("Changed Property key [%s] with value [%s] to  new key:  [%s] for node [%s] \n", prop_key, value, new_prop_key, n.id), System.Logger.Level.INFO);

        } else {
            assert edges != null;
            Edge e = edges.get(random_index - node_pool_size);
            ArrayList<String> properties = new ArrayList<>(e.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String new_prop_key = generateString(prop_key.length() * 2);

            String value = e.properties.get(prop_key);

            e.properties.put(new_prop_key, value);
            e.properties.remove(prop_key);

            if (e.propertyTypes == null) {
                e.propertyTypes = new HashMap<>();
            }
            e.propertyTypes.put(new_prop_key, g.getEdgeProperty(e, prop_key).type);
            e.propertyTypes.remove(prop_key);

            ArrayList<String> labels = new ArrayList<>(e.labels);
            Collections.shuffle(labels);
            int label_index= -1;
            for (int i = 0; i < labels.size(); i++) {
                if (g.getSchema().getNodeProperties().get(e.labels.get(i)) == null) {
                    label_index = i;
                }
            }

            if (label_index == -1) {
                printString("No properties in schema on edge " + e.labels, System.Logger.Level.WARNING);
                return;
            }
            Property p = g.getSchema().getEdgeProperties().get(labels.get(label_index)).stream().filter(property -> property != null && property.name.equals(prop_key)).findFirst().orElse(null);

            if (p != null) {
                Property new_prop = new Property(new_prop_key, p.type, p.isUnique, p.isNotNull, p.valueIsConstraint, p.min, p.max);
                g.getSchema().getEdgeProperties().get(labels.get(label_index)).add(new_prop);
            }

            printString(String.format("Changed Property key [%s] with value [%s] to  new key:  [%s] for edge [%s] -- [%s] --> [%s]\n", prop_key, value, new_prop_key, e.from, e.labels, e.to), System.Logger.Level.INFO);
        }
    }

    private static void changePropertyValue(MyGraph g) {
        ArrayList<Node> nodes = getNodesWithProperties(g);
        ArrayList<Edge> edges = getEdgesWithProperties(g);

        int pool_size = 0;
        int node_pool_size = 0;
        int edge_pool_size = 0;

        if (nodes != null) {
            node_pool_size = nodes.size();
            pool_size += node_pool_size;
        }
        if (edges != null) {
            edge_pool_size = edges.size();
            pool_size += edge_pool_size;
        }

        if (pool_size == 0) {
            printString("No properties on node which a property can be changed", System.Logger.Level.WARNING);
//            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.ChangePropertyValue, false);
            return;
        }

        int random_index = r.nextInt(pool_size);

        if (random_index < node_pool_size) {
            Node n = nodes.get(random_index);
            ArrayList<String> properties = new ArrayList<>(n.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);


            String old_value = n.properties.get(prop_key);

//            Property p = g.getSchema().getNodeProperties().get(n.label).stream().filter(property -> property != null && property.name.equals(prop_key)).findFirst().orElse(null);
            Property p = g.getNodeProperty(n, prop_key);
            // No property defined in schema
            String new_value = GraphGenerator.generatePropertyValue(p);


            n.properties.put(prop_key, new_value);

            printString(String.format("Changed Property [%s] with value [%s] to value [%s] for node [%s] \n", prop_key, old_value, new_value, n.id), System.Logger.Level.INFO);

        } else {
            assert edges != null;
            Edge e = edges.get(random_index - node_pool_size);
            ArrayList<String> properties = new ArrayList<>(e.properties.keySet());
            int random_property_index = r.nextInt(properties.size());
            String prop_key = properties.get(random_property_index);

            String old_value = e.properties.get(prop_key);
            Property p =  g.getEdgeProperty(e, prop_key);
//            System.out.println("Property found: " + p.name + ", " + p.type);

            // No property defined in schema
            String new_value = GraphGenerator.generatePropertyValue(p);

            e.properties.put(prop_key, new_value);

            printString(String.format("Changed Property [%s] with value [%s] to value [%s] for edge [%s] -- [%s] --> [%s]\n", prop_key, old_value, new_value, e.from, e.labels, e.to), System.Logger.Level.INFO);
        }
    }

    private static void removeNodeMutation(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();

        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        int random_node_index = r.nextInt(nodes.size());
        Node n = g.getNodeOnIndex(random_node_index);
        printString(String.format("Removed Node [%s] \n", n.id), System.Logger.Level.INFO);
        g.removeNode(n.id);
    }

    private static void copyNodeMutation(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();

        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        int random_node_index = r.nextInt(nodes.size());
        Node n = g.getNodeOnIndex(random_node_index);
        Node copy = g.addCopyNode(n);
        printString(String.format("Copy Node [%s] --> [%s]\n", n.id, copy.id), System.Logger.Level.INFO);
    }

    private static void addNodeMutation(MyGraph g) {
        GraphSchema gs = g.getSchema();

        // Select random node label from schema
        ArrayList<String> labels = new ArrayList<>(gs.getNodeLabels());
        String random_node_label = labels.get(r.nextInt(labels.size()));
        Node new_node = g.addLabeledNode(random_node_label);

        // Create node, add properties
        GraphGenerator.generateNodeProperties(new_node, gs);

        // Select random other nodes to connect said node to
        ArrayList<Relationship> relationships = selectRelationships(random_node_label, gs);
        createEdgesForNode(g, new_node, relationships);
    }

    private static void removeNodesOfLabelMutation(MyGraph g) {
        GraphSchema gs = g.getSchema();
        ArrayList<String> labels = new ArrayList<>(gs.getNodeLabels());
        if (gs.getNodeLabels() == null || labels.isEmpty()) {
            printString("No node labels defined, nodes of label mutation disabled", System.Logger.Level.WARNING);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.RemoveNodesOfLabel, false);
            return;
        }
        String random_node_label = labels.get(r.nextInt(labels.size()));
        ArrayList<Node> nodes = g.getNodes(random_node_label);
        for (Node n : nodes) {
            g.removeNode(n.id);
        }
    }

    private static void removeEdgesOfLabelMutation(MyGraph g) {
        GraphSchema gs = g.getSchema();
        ArrayList<String> labels = new ArrayList<>(gs.getEdgeLabels());
        if (gs.getEdgeLabels() == null || labels.isEmpty()) {
            printString("No edge labels defined, edges of label mutation disabled", System.Logger.Level.WARNING);
            GraphMutations.changeMutationStatus(GraphMutations.MutationMethod.RemoveEdgesOfLabel, false);
            return;
        }
        String random_edge_label = labels.get(r.nextInt(labels.size()));
        ArrayList<Node> nodes = g.getNodes();
        for (Node n : nodes) {
            Set<Edge> edges = n.getEdges();
            Set<Edge> remove_edges = new HashSet<>();
            for (Edge e : edges) {
                if (e.labels.contains(random_edge_label)) {
                    remove_edges.add(e);
                }

            }
            for (Edge e : remove_edges) {

                n.removeEdge(e);
            }
        }


    }

    private static void createEdgesForNode(MyGraph g, Node newNode, ArrayList<Relationship> relationships) {
        for (Relationship rel :
                relationships) {

            Node candidate_n = createCandidateForEdge(g, rel, newNode);
            if (candidate_n == null) {
                return;
            }

            Edge e = new Edge(rel, newNode, candidate_n);
            newNode.addEdge(e);
        }
    }

    private static Node createCandidateForEdge(MyGraph g, Relationship rel, Node newNode) {
        String from_node_label = rel.getFrom();
        String to_node_label = rel.getTo();


        ArrayList<Node> candidates;
        if (newNode.labels.contains(from_node_label)) {
            candidates = g.getNodes(to_node_label);
        } else {
            candidates = g.getNodes(from_node_label);
        }

        // Shuffle to have some randomness in the first node that is selected
        Collections.shuffle(candidates, r);

        for (Node candidate : candidates) {
            if (checkCandidateCardinality(newNode, candidate, rel)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean checkCandidateCardinality(Node from, Node to, Relationship rel) {
        Cardinality c = rel.getCardinality();

        if (!from.labels.contains(rel.getFrom())) {
            Node holder = from;
            from = to;
            to = holder;
        }

        // TODO:
        Node finalTo = to;
        switch (c) {

            case MULTI -> {
                return true;
            }
            case SIMPLE -> {
                // Check if one of the edges has an edge which corresponds to the label of rel.
                return !(from.getEdges().stream().filter(edge -> edge.to == finalTo.id).map(edge -> edge.labels).collect(Collectors.toSet()).contains(rel.getLabel()));
            }
            case MANY2ONE -> {
                Set<Edge> current_edges = from.getOutgoingEdges();
                Set<String> edge_labels = current_edges.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());
                return !edge_labels.contains(rel.getLabel());

            }
            case ONE2MANY -> {
                Set<Edge> current_edges = to.getIncomingEdges();
                Set<String> edge_labels = current_edges.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());
                return (!edge_labels.contains(rel.getLabel()));
            }
            case ONE2ONE -> {
                Set<Edge> current_edges_from_out = from.getOutgoingEdges();
                Set<String> edge_labels_from_out = current_edges_from_out.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());

                Set<Edge> current_edges_from_in = from.getIncomingEdges();
                Set<String> edge_labels_from_in = current_edges_from_in.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());

                Set<Edge> current_edges_to_in = to.getIncomingEdges();
                Set<String> edge_labels_to_in = current_edges_to_in.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());
                Set<Edge> current_edges_to_out = to.getOutgoingEdges();
                Set<String> edge_labels_to_out = current_edges_to_out.stream().map(edge -> edge.labels).flatMap(Collection::stream).collect(Collectors.toSet());

                return (!edge_labels_from_out.contains(rel.getLabel()) && !edge_labels_from_in.contains(rel.getLabel())
                        && !edge_labels_to_out.contains(rel.getLabel()) && !edge_labels_to_in.contains(rel.getLabel()));
            }
        }
        return true;
    }

    private static ArrayList<Relationship> selectRelationships(String nodeLabel, GraphSchema gs) {
        ArrayList<Relationship> relationships = new ArrayList<>(gs.getRelationships());
        ArrayList<Relationship> selected_relationships = new ArrayList<>();
        // Add all relationships that need to be fulfilled. Maybe changing dependent on cardinality
        for (Relationship rel :
                relationships) {
            if (rel.getFrom().equals(nodeLabel) || rel.getTo().equals(nodeLabel)) {
                selected_relationships.add(rel);
            }
        }
        return selected_relationships;
    }

    private static void applyCopySubSetMutation(MyGraph g) {
        ArrayList<Node> subset_nodes = selectSubGraphNodes(g);
        printString(String.format("Selected [%s] nodes for subgraph%n", subset_nodes.size()), System.Logger.Level.INFO);
        if (subset_nodes.size() <= 1) {
            return;
        }
        HashMap<Integer, Integer> mapping = updateNodeIDs(subset_nodes, g);
        printString("subset mapping: ", System.Logger.Level.DEBUG);
        for (Integer k :
                mapping.keySet()) {
            printString(String.format("\t Node [%s] --> Node [%s]", k, mapping.get(k)), System.Logger.Level.DEBUG);

        }

        process_edges_in_subset(subset_nodes, mapping);
        g.addNodes(subset_nodes);
    }

    private static HashMap<Integer, Integer> updateNodeIDs(ArrayList<Node> subsetNodes, MyGraph g) {
        OptionalInt max_id_opt = g.getMaxID();
        int next_open_id = max_id_opt.orElse(0) + 1;
        HashMap<Integer, Integer> new_id_mapping = new HashMap<>();

        // First go over each node, re-assigning node ID's
        for (Node n : subsetNodes) {
            // TODO: get a new id from graph --> Problem, id's are not necessarily incrementing... Especially if imported from GMARK
            if (!new_id_mapping.containsKey(n.id)) {
                new_id_mapping.put(n.id, next_open_id);
                next_open_id++;
            }

            // assign id to node
            n.id = new_id_mapping.get(n.id);
        }
        return new_id_mapping;
    }

    private static void process_edges_in_subset(ArrayList<Node> subsetNodes, HashMap<Integer, Integer> mapping) {
        for (Node n : subsetNodes) {
            for (Edge e : n.getEdges()) {
                // CCurrent version is from always this node, but future version might combine edges and incoming edges
                if (mapping.containsKey(e.from)) {
                    e.from = mapping.get(e.from);
                }
                if (mapping.containsKey(e.to)) {
                    e.to = mapping.get(e.to);
                }
            }
        }
    }

    private static ArrayList<Node> selectSubGraphNodes(MyGraph g) {
        int graph_node_count = g.getNodes().size();

        if (graph_node_count <= 2 ) {
            return g.getNodes();
        }

        int sub_graph_size = r.nextInt(graph_node_count - 2) + 2;


        ArrayList<Node> nodes = new ArrayList<>();
        Set<Integer> indices_selected = new HashSet<>();

        int tries = 0;
        while (indices_selected.size() < sub_graph_size) {
            tries++;
            if (tries > graph_node_count*3) {
                break;
            }
            int random_node_index = r.nextInt(graph_node_count);
            if (indices_selected.contains(random_node_index)) {
                continue;
            }
            indices_selected.add(random_node_index);
            Node node_selected = g.getNodes().get(random_node_index);
            Node node_clone = (Node) SerializationUtils.clone(node_selected);

            nodes.add(node_clone);
        }
        return nodes;
    }

    private static GraphMutations.MutationMethod selectMutationMethod() {
        return GraphMutations.getRandomMutation(r);
    }

    public static boolean ByteMutationLimit(MyGraph currentGraph, int mm) {
        int rand_mutation_selection = mm;
        if (mm < 0) {
            rand_mutation_selection = r.nextInt(6);
        }
        if (rand_mutation_selection == 0) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            ArrayList<String> old_labels = n.labels;
            int random_label_index = r.nextInt(old_labels.size());
            String old_label = old_labels.get(random_label_index);
            String new_label = generateString(old_label.length() * 2);
            n.labels.set(random_label_index, new_label);
        } else if (rand_mutation_selection == 1) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            Edge e = getRandomEdge(n);
            if (e == null) {
                return false;
            }
            ArrayList<String> old_labels = e.labels;
            int random_label_index = r.nextInt(old_labels.size());
            String old_label = old_labels.get(random_label_index);
            String new_label = generateString(old_label.length() * 2);
            e.labels.set(random_label_index, new_label);
        } else if (rand_mutation_selection == 2) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            if (n.properties == null || n.properties.isEmpty()) {
                return false;
            }
            int random_prop = r.nextInt(n.properties.size());
            List<String> keys = new ArrayList<>(n.properties.keySet());
            String key = keys.get(random_prop);
            n.properties.put(key, generateString(n.properties.get(key).length() * 2));
        } else if (rand_mutation_selection == 3) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            Edge e = getRandomEdge(n);
            if (e == null) {
                return false;
            }
            if (e.properties == null || e.properties.isEmpty()) {
                return false;
            }
            int random_prop = r.nextInt(e.properties.size());
            List<String> keys = new ArrayList<>(e.properties.keySet());
            String key = keys.get(random_prop);
            e.properties.put(key, generateString(e.properties.get(key).length() * 2));
        } else if (rand_mutation_selection == 4) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            if (n.properties == null || n.properties.isEmpty()) {
                return false;
            }
            int random_prop = r.nextInt(n.properties.size());
            List<String> keys = new ArrayList<>(n.properties.keySet());
            String key = keys.get(random_prop);
            String value = n.properties.get(key);
            n.properties.remove(key);
            n.properties.put(generateString(key.length() * 2), value);
        } else if (rand_mutation_selection == 5) {
            Node n = getRandomNode(currentGraph);
            if (n == null) {
                return false;
            }
            Edge e = getRandomEdge(n);
            if (e == null) {
                return false;
            }
            if (e.properties == null || e.properties.isEmpty()) {
                return false;
            }
            int random_prop = r.nextInt(e.properties.size());
            List<String> keys = new ArrayList<>(e.properties.keySet());
            String key = keys.get(random_prop);
            String value = e.properties.get(key);
            e.properties.remove(key);
            e.properties.put(generateString(key.length() * 2), value);
        }
        return true;
    }


    public void setR(long seed) {
        SEED_FOR_RANDOM = seed;
        r = new Random(seed);
    }

    public static void printString(String s, System.Logger.Level isInfo) {
        boolean print_info = false;
        boolean print_debug = false;
        boolean print_warning = false;

        if (print_info && isInfo == System.Logger.Level.INFO) {
            System.out.println(s);
        }
        if (print_debug && isInfo == System.Logger.Level.DEBUG) {
            System.out.println(s);
        }
        if (print_warning && isInfo == System.Logger.Level.WARNING) {
            System.out.println(s);
        }
    }

    public static MyGraph GraphBitMutation(MyGraph g, int bitflipCount) {
        byte[] data = BitMutationToStream(g, bitflipCount);
        MyGraph newGraph = null;
        try {
            newGraph = (MyGraph) SerializationUtils.deserialize(data);
        } catch (Exception e) {
            // do nothing, structure is most likely corrupted
        }
        return newGraph;

    }

    public static byte[] BitMutationToStream(MyGraph g, int bitflipCount) {
        byte[] data = SerializationUtils.serialize(g);

        int MIN_BYTE_VALUE = -128;
        int MAX_BYTE_VALUE = 127;

        if (r == null) {
            r = new Random();
        }

        for (int i = 0; i < bitflipCount; i++) {
            int byteIndex = r.nextInt(data.length);

            int mutation = 1;
            if (r.nextBoolean()) {
                mutation = -1;
            }

            Integer mutatedByteValue = (int) data[byteIndex] + mutation;

            data[byteIndex] = mutatedByteValue.byteValue();
        }
        return data;

    }

    public static void ByteMutationToFile(MyGraph g, String output_file) throws IOException {
        byte[] data = ByteMutationToStream(g);

        FileOutputStream fileOut = new FileOutputStream(output_file);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(data);
        out.close();
        fileOut.close();
    }

    public static MyGraph ByteMutation(MyGraph g) throws IOException {
        byte[] data = ByteMutationToStream(g);
        MyGraph newGraph = null;
        try {
            newGraph = (MyGraph) SerializationUtils.deserialize(data);
        } catch (Exception e) {
            // do nothing, structure is most likely corrupted
            throw new IOException("Structure corrupted");
        }
        return newGraph;
    }

    public static byte[] ByteMutationToStream(MyGraph g) {
        byte[] data = SerializationUtils.serialize(g);

        int MIN_BYTE_VALUE = -128;

        byte[] mutated_data = new byte[0];

        //Either  mutate, delete or add byte
        int byte_mutation_method = r.nextInt(3);
        int byteIndex = r.nextInt(data.length);
        int byteValue = r.nextInt(256) + MIN_BYTE_VALUE;

        if (byte_mutation_method == 0) {
            mutated_data = data.clone();
            mutated_data[byteIndex] = (byte) byteValue;
        } else if (byte_mutation_method == 1) {
            mutated_data = new byte[data.length - 1];
            for (int i = 0; i < byteIndex; i++) {
                mutated_data[i] = data[i];
            }
            for (int i = byteIndex; i < data.length - 1; i++) {
                mutated_data[i] = data[i + 1];
            }

        } else if (byte_mutation_method == 2) {
            mutated_data = new byte[data.length + 1];
            for (int i = 0; i < byteIndex; i++) {
                mutated_data[i] = data[i];
            }
            mutated_data[byteIndex] = (byte) byteValue;
            for (int i = byteIndex; i < data.length; i++) {
                mutated_data[i + 1] = data[i];
            }
        }
        return mutated_data;
    }

}




