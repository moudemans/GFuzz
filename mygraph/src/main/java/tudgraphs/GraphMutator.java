package tudgraphs;

import tudcomponents.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.SerializationUtils;
import util.Util;

public class GraphMutator {
    // Graphs.Mutation parameters


    // Random objects for random mutations. Seed is stored for reproducibility;
    static private final Random SEED_GENERATOR_FOR_RANDOM = new Random();
    static private long SEED_FOR_RANDOM = SEED_GENERATOR_FOR_RANDOM.nextLong();
    static private Random r = new Random(SEED_FOR_RANDOM);

    private static HashMap<Relationship, Boolean> cardinalityMutationsPerformed = new HashMap<>();
    private static HashSet<Property> propertyUniqueMutationPerformed = new HashSet<>();
    private static HashSet<Property> propertyNullMutationPerformed = new HashSet<>();


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
        System.out.println("Mutation method selected: " + mm);

        applyMutationMethod(g, mm, null);
        return mm;
    }

    public static String mutateGraphLimit(MyGraph g, Set<String> breaking_mutations) {
        assert g.getSchema() != null : "Graph schema not available for graph mutation";

        GraphMutations.MutationMethod mm = selectMutationMethod();
        System.out.println("Mutation method selected: " + mm);

        String message = applyMutationMethod(g, mm, breaking_mutations);
        return message;
    }


    private static String applyMutationMethod(MyGraph g, GraphMutations.MutationMethod mm, Set<String> breaking_mutations) {
        String mutation_message = "";
        switch (mm) {
            case CopySubset -> applyCopySubSetMutation(g);
            case AddNode -> addNodeMutation(g);
            case RemoveNode -> removeNodeMutation(g);
            case AddEdge -> addEdgeMutation(g);
            case RemoveEdge -> removeEdgeMutation(g);
            case ChangePropertyValue -> changePropertyValue(g);
            case RemoveProperty -> removePropertyKey(g);
            case AddProperty -> addPropertyKey(g);
            case BreakSchema -> mutation_message = breakSchemaMutation(g, breaking_mutations);

            case BreakCardinality -> mutation_message = breakCardinalityMutation(g, breaking_mutations);
            case BreakUnique -> mutation_message = breakUniqueMutation(g, breaking_mutations);
            case BreakNull -> mutation_message = breakNullMutation(g, breaking_mutations);
            default -> System.err.println("Mutation method not implemented: " + mm);
        }
        return mutation_message;
    }

    private static String breakSchemaMutation(MyGraph g, Set<String> breaking_mutations) {
        // There are 2 types of constraint that can be broken, relationship or property
        int random_schema_break_mutation = r.nextInt(3);
        GraphMutations.MutationMethod[] breaking_mutations_methods = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.BreakUnique, GraphMutations.MutationMethod.BreakCardinality, GraphMutations.MutationMethod.BreakNull
        };
        if (breaking_mutations == null) {
            breaking_mutations = new HashSet<>();
        }
        String constraint = applyMutationMethod(g, breaking_mutations_methods[random_schema_break_mutation], breaking_mutations);

        return breaking_mutations_methods[random_schema_break_mutation] + "$" + constraint;
    }

    private static String breakNullMutation(MyGraph g, Set<String> breaking_mutations) {
        HashMap<String, ArrayList<Property>> nullprops = new HashMap<>(g.getSchema().getNonNullNodeProperties());


        if (nullprops.isEmpty()) {
            System.err.println("Could not perform a break null property mutation. There are no unique properties");
            return "";
        }

        if (nullprops.isEmpty()) {
            System.err.println("Could not perform a break unique property mutation. There are no unique properties");
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

        String node_label = node_labels.get(0);
        assert !filtered_properties.get(node_label).isEmpty();

        ArrayList<Property> properties = new ArrayList<>(filtered_properties.get(node_label));
        Collections.shuffle(properties, r);


        Property property = properties.get(0);

        ArrayList<Node> nodes = new ArrayList<>(g.getNodes(node_label));
        if (nodes.isEmpty()) {
            System.err.println("Could not perform a break unique property mutations, not enough nodes for label: " + node_label);
            return "";
        }

        Collections.shuffle(nodes, r);
        Node node1 = nodes.get(0);


        assert node1.properties.containsKey(property.name);

        //TODO: check if this should be null or ""
        node1.properties.put(property.name, null);

        propertyNullMutationPerformed.add(property);
        return node_label + "-" + property.name;
    }

    private static String breakUniqueMutation(MyGraph g, Set<String> breaking_mutations) {

        //Collect all node+properties
        HashMap<String, ArrayList<Property>> uniqueprops = new HashMap<>(g.getSchema().getUniqueNodeProperties());


        if (uniqueprops.isEmpty()) {
            System.err.println("Could not perform a break unique property mutation. There are no unique properties");
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
            System.err.println("Could not perform a break unique property mutation. all the unique properties have already been mutated");
            return "";
        }
        ArrayList<String> node_labels = new ArrayList<>(filtered_properties.keySet());
        Collections.shuffle(node_labels, r);

        String node_label = node_labels.get(0);
        assert !filtered_properties.get(node_label).isEmpty();

        ArrayList<Property> properties = new ArrayList<>(filtered_properties.get(node_label));
        Collections.shuffle(properties, r);

        Property property = properties.get(0);

        ArrayList<Node> nodes = new ArrayList<>(g.getNodes(node_label));
        if (nodes.size() <= 1) {
            System.err.println("Could not perform a break unique property mutations, not enough nodes for label: " + node_label);
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
            System.err.println("There are no relationships with cardinality which can be broken, no cardinality mutation performed");
            return "";
        }

        Set<String> cardinality_mutations_applied = breaking_mutations.stream().filter(s -> s.contains(GraphMutations.MutationMethod.BreakCardinality.toString())).collect(Collectors.toSet()); // Filter on cardinality mutation messages
        ArrayList<Relationship> filtered_relationships = new ArrayList<>(relationships_with_cardinality.stream().filter(relationship -> !cardinality_mutations_applied.contains(relToConstraintString(relationship))).toList());

        if (filtered_relationships.isEmpty()) {
            System.err.println("All relationships with cardinality have already been broken, no mutation performed");
            return "";
        }

        int random_relationship_index = r.nextInt(relationships_with_cardinality.size());
        Relationship rel = relationships_with_cardinality.get(random_relationship_index);

        breakCardinality(g, rel);

        cardinalityMutationsPerformed.put(rel, true);
        String constraint_string = relToConstraintString(rel);
        return constraint_string;
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
            System.err.printf("Not enough from nodes found to break many2One cardinality for relationship [%s]", rel);
            return;
        }

        if (to_nodes.isEmpty()) {
            System.err.printf("No to nodes found to break many2One cardinality for relationship [%s]", rel);
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
            System.err.printf("No from nodes found to break many2One cardinality for relationship [%s]", rel);
            return;
        }

        if (to_nodes.size() < 2) {
            System.err.printf("Not enough to nodes found to break many2One cardinality for relationship [%s]", rel);
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
            System.err.printf("No from nodes found to break simple cardinality for relationship [%s]", rel);
            return;
        }

        if (to_nodes.isEmpty()) {
            System.err.printf("No to nodes found to break simple cardinality for relationship [%s]", rel);
            return;
        }

        int random_from_node_index = r.nextInt(from_nodes.size());
        int random_to_node_index = r.nextInt(to_nodes.size());

        Node from_node = from_nodes.get(random_from_node_index);
        Node to_node = to_nodes.get(random_to_node_index);

        Edge e1 = new Edge(rel.getLabel(), from_node.id, to_node.id);
        Edge e2 = new Edge(rel.getLabel(), from_node.id, to_node.id);

        System.out.printf("Breaking SIMPLE cardinality between node [%s] and [%s] by adding two edges with label [%s]", from_node.id, to_node.id, rel.getLabel());

        g.addEdge(e1);
        g.addEdge(e2);
    }


    // TODO
    private static void addPropertyKey(MyGraph g) {
        String property_label;
        GraphSchema gs = g.getSchema();
        ArrayList<String> node_labels = new ArrayList<>(gs.getNodeProperties().keySet());
        ArrayList<String> edge_labels = new ArrayList<>(gs.getEdgeProperties().keySet());

        int pool_size = node_labels.size() + edge_labels.size();
        int random_index = r.nextInt(pool_size);
        String random_label = (random_index < node_labels.size()) ? node_labels.get(random_index) : edge_labels.get(random_index);
//        Property random_property = (random_index < node_labels.size()) ? node_labels.get(random_index) : edge_labels.get(random_index);
    }

    private static void removePropertyKey(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        int random_node_index = r.nextInt(nodes.size());
        Node n = g.getNodeOnIndex(random_node_index);


        if (n.properties.isEmpty()) {
            System.err.println("No properties on node which can be removed");
            return;
        }

        ArrayList<String> properties = new ArrayList<>(n.properties.keySet());
        int random_property_index = r.nextInt(properties.size());
        String prop_key = properties.get(random_property_index);

        // TODO edge properties

        String value_to_be_removed = n.properties.get(prop_key);
        n.properties.remove(prop_key);

        System.out.printf("Removed Property [%s] with value [%s], from node [%s] \n", prop_key, value_to_be_removed, n.id);
    }

    private static void removeEdgeMutation(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        int random_node_index = r.nextInt(nodes.size());
        Node n = g.getNodeOnIndex(random_node_index);

        ArrayList<Edge> edges = new ArrayList<>(n.getEdges());
        int random_edge_index = r.nextInt(edges.size());
        Edge e = edges.get(random_edge_index);

        System.out.printf("Removed Edge [%s], from [%s] --> to [%s] \n", e.label, e.from, e.to);
        g.removeEdge(e);
    }

    private static void addEdgeMutation(MyGraph g) {
        GraphSchema gs = g.getSchema();

        // Select random edge label from schema
        ArrayList<String> labels = new ArrayList<>(gs.getEdgeLabels());
        String random_edge_label = labels.get(r.nextInt(labels.size()));

        // Determine which nodes are needed to create relationship
        List<Relationship> rels = gs.getRelationships().stream().filter(relationship -> relationship.getLabel().equals(random_edge_label)).toList();

        // If no relationship is defined, select 2 random nodes to add the edge between? <-- TODO
        if (rels.isEmpty()) {
            System.err.printf("No relationships available for edge label: %s \n", random_edge_label);
        }

        Relationship rel = rels.get(0);
        String from_label = rel.getFrom();
        String to_label = rel.getTo();

        ArrayList<Node> from_candidates = g.getNodes(from_label);
        ArrayList<Node> to_candidates = g.getNodes(to_label);

        if (from_candidates.isEmpty() || to_candidates.isEmpty()) {
            System.out.printf("Could not add a new edge, to few available nodes - from=[%s]:[%s], to=[%s]:[%s] \n", from_label, from_candidates.size(), to_label, to_candidates.size());
            return;
        }

        int random_from_index = r.nextInt(from_candidates.size());
        int random_to_index = r.nextInt(to_candidates.size());

        Node from = from_candidates.get(random_from_index);
        Node to = to_candidates.get(random_to_index);

        // TODO: follow cardinality?

        Edge new_edge = new Edge(random_edge_label, from.id, to.id);
        g.addEdge(new_edge);
        System.out.printf("Add Edge [%s], from [%s] --> to [%s] \n", new_edge.label, new_edge.from, new_edge.to);
    }

    private static void changePropertyValue(MyGraph g) {
        if (g.getSchema().getNodeLabels().isEmpty()) {
            System.err.println("No node labels defined in schema, can't mutate properties");
        }
        // select random Node label from schema + property
        ArrayList<String> node_labels = new ArrayList<>(g.getSchema().getNodeLabels());
        int random_label_index = r.nextInt(node_labels.size());
        String random_label = new ArrayList<>(g.getSchema().getNodeLabels()).get(random_label_index);
        ArrayList<Property> properties = g.getSchema().getNodeProperties().get(random_label);

        if (properties == null || properties.isEmpty()) {
            System.err.printf("No properties defined for node [%s], can't mutate properties \n", random_label);
            return;
        }

        int random_property_index = r.nextInt(properties.size());
        Property p = properties.get(random_property_index);

        // select random node from graph with selected label +schema
        ArrayList<Node> candidates = g.getNodes(random_label);
        int random_candidate_index = r.nextInt(candidates.size());
        Node candidate = candidates.get(random_candidate_index);

        // generate new value
        String old_value = candidate.properties.get(p.name);
        String new_value = GraphGenerator.generatePropertyValue(p);
        candidate.properties.put(p.name, new_value);

        System.out.printf("Changed Property [%s] for Node [%s]. [%s] --> [%s] \n", p.name, candidate.id, old_value, new_value);
    }

    private static void removeNodeMutation(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();
        int random_node_index = r.nextInt(nodes.size());
        Node n = g.getNodeOnIndex(random_node_index);
        System.out.printf("Removed Node [%s] \n", n.id);
        g.removeNode(n.id);
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
        if (newNode.label.equals(from_node_label)) {
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

        if (!from.label.equals(rel.getFrom())) {
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
                return !(from.getEdges().stream().filter(edge -> edge.to == finalTo.id).map(edge -> edge.label).collect(Collectors.toSet()).contains(rel.getLabel()));
            }
            case MANY2ONE -> {
                Set<Edge> current_edges = from.getOutgoingEdges();
                Set<String> edge_labels = current_edges.stream().map(edge -> edge.label).collect(Collectors.toSet());
                return !edge_labels.contains(rel.getLabel());

            }
            case ONE2MANY -> {
                Set<Edge> current_edges = to.getIncomingEdges();
                Set<String> edge_labels = current_edges.stream().map(edge -> edge.label).collect(Collectors.toSet());
                return (!edge_labels.contains(rel.getLabel()));
            }
            case ONE2ONE -> {
                Set<Edge> current_edges_from_out = from.getOutgoingEdges();
                Set<String> edge_labels_from_out = current_edges_from_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

                Set<Edge> current_edges_from_in = from.getIncomingEdges();
                Set<String> edge_labels_from_in = current_edges_from_in.stream().map(edge -> edge.label).collect(Collectors.toSet());

                Set<Edge> current_edges_to_in = to.getIncomingEdges();
                Set<String> edge_labels_to_in = current_edges_to_in.stream().map(edge -> edge.label).collect(Collectors.toSet());
                Set<Edge> current_edges_to_out = to.getOutgoingEdges();
                Set<String> edge_labels_to_out = current_edges_to_out.stream().map(edge -> edge.label).collect(Collectors.toSet());

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
        System.out.printf("Selected [%s] nodes for subgraph%n", subset_nodes.size());
        HashMap<Integer, Integer> mapping = updateNodeIDs(subset_nodes, g);
        System.out.println("subset mapping: ");
        for (Integer k :
                mapping.keySet()) {
            System.out.printf("\t Node [%s] --> Node [%s]", k, mapping.get(k));

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

        int sub_graph_size = r.nextInt(graph_node_count - 2) + 2;


        ArrayList<Node> nodes = new ArrayList<>();
        Set<Integer> indices_selected = new HashSet<>();

        while (indices_selected.size() < sub_graph_size) {
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

    private static GraphMutations.MutationMethod selectMutationMethod(Set<GraphMutations.MutationMethod> excludeMutations) {
        ArrayList<GraphMutations.MutationMethod> mutations = GraphMutations.getActiveMutationMethodList();
        List<GraphMutations.MutationMethod> filtered = new ArrayList<>(mutations.stream().filter(mutationMethod -> !excludeMutations.contains(mutationMethod)).toList());
        Collections.shuffle(filtered);
        if (filtered.isEmpty()) {
            return GraphMutations.MutationMethod.NoMutation;
        }
        return filtered.get(0);
    }


    public void setR(long seed) {
        SEED_FOR_RANDOM = seed;
        r = new Random(seed);
    }

}




