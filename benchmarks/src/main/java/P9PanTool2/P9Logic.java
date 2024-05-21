package P9PanTool2;


import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class P9Logic {
    MyGraph my_g;

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        getOrderedMrnaNodesPerSequence(g, true);
        getOrderedMrnaNodesPerSequence(g, false);

    }


    public HashMap<String, Node[]> getOrderedMrnaNodesPerSequence(MyGraph g,boolean allow_same_start) {

        HashMap<String, Node[]> mrnaNodesPerSequence = new HashMap<>();
        int total_genomes = Integer.parseInt(g.getNodes("pangenome").getFirst().getProperty("num_genomes"));

        for (int i = 1; i <= total_genomes; i++) { // i is a genome number
            int total_mrna_counter = 0;
            ArrayList<Node> genome_nodes = g.getNodes("genome", "number", i + "");

            if(genome_nodes == null || genome_nodes.size() != 1) {
                // This is handled in the neo4j library throwing an error, seeGraphDatabaseFacade.findNode(label, key, value)
                return null;
            }
            Node genome_node = genome_nodes.getFirst();

            int num_sequences = Integer.parseInt(genome_node.getProperty("num_sequences"));
            for (int j = 1; j <= num_sequences; j++) {


                TreeSet<Integer> gene_start_positions = new TreeSet<>(); // automatically ordered, contains starting locations of genes
                HashMap<Integer, ArrayList<Node>> mrna_nodes_per_start_position = new HashMap<>();
//                ResourceIterator<Node> mrna_nodes = GRAPH_DB.findNodes(MRNA_LABEL, "genome", i, "sequence", j);
                Iterator<Node> mrna_nodes = g.findNodes("mRNA", "genome", i+"", "sequence", j+"").iterator();
                while (mrna_nodes.hasNext()) {
                    Node mrna_node = mrna_nodes.next();
                    if (!mrna_node.properties.containsKey("longest_transcript")) {
                        continue;
                    }
                    if ( !mrna_node.properties.containsKey("protein_ID")) {
                        continue;
                    }
                    total_mrna_counter++;

                    // TODO: int array
                    String address_array =  mrna_node.getProperty("address");
                    String[] address = address_array.split(",");


                    gene_start_positions.add(Integer.valueOf(address[2]));
                    mrna_nodes_per_start_position.computeIfAbsent(Integer.valueOf(address[2]), k -> new ArrayList<>()).add(mrna_node); // used to find genes with the same start position
                }
                if (gene_start_positions.isEmpty()) {
                    continue;
                }
                Node[] mrnas_ordered = put_ordered_genes_in_array(g, gene_start_positions, mrna_nodes_per_start_position, allow_same_start); // order the genes by start position
                mrnaNodesPerSequence.put(i + "_" + j, mrnas_ordered);
            }
        }
        return mrnaNodesPerSequence;
    }

    private Node[] put_ordered_genes_in_array(MyGraph g, TreeSet<Integer> gene_start_positions,
                                              HashMap<Integer, ArrayList<Node>> mrna_nodes_per_start_position, boolean allow_same_start) {

        ArrayList<Node> ordered_mrnas = new ArrayList<>();
        for (int gene_start : gene_start_positions) {
            ArrayList<Node> mrna_nodes_list = mrna_nodes_per_start_position.get(gene_start);
            if (mrna_nodes_list.size() > 1) {
                if (allow_same_start) { // add all mrna's but order them from shortest to largest
                    TreeSet<Integer> lengths = new TreeSet<>();
                    HashMap<Integer, ArrayList<Node>> nodes_per_length = new HashMap<>();
                    for (Node mrna_node : mrna_nodes_list) {
                        int length = Integer.parseInt( mrna_node.getProperty("length"));
                        lengths.add(length);
                        nodes_per_length.computeIfAbsent(length, k -> new ArrayList<>()).add(mrna_node);
                    }
                    for (int length : lengths) {
                        ArrayList<Node> nodes = nodes_per_length.get(length);
                        ordered_mrnas.addAll(nodes);
                    }
                } else { // only add the largest
                    int highest = 0;
                    Node mrna_of_highest = g.getNode(0);
                    for (Node mrna_node : mrna_nodes_list) {
                        int length = Integer.parseInt(mrna_node.getProperty("length"));
                        if (highest < length) {
                            mrna_of_highest = mrna_node;
                        }
                    }
                    ordered_mrnas.add(mrna_of_highest);
                }
            } else {
                ordered_mrnas.add(mrna_nodes_list.get(0));
            }
        }
        return ordered_mrnas.toArray(new Node[ordered_mrnas.size()]); // list_to_array list to array ;
    }







}