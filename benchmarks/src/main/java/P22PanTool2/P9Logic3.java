package P22PanTool2;


import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class P9Logic3 {
    MyGraph my_g;

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        HashMap<Integer, Node[]> mrnas_per_genome = get_mrna_nodes_by_hm_or_ids(g);
        create_all_functions_overview(g, mrnas_per_genome);
    }

    public static void create_all_functions_overview(MyGraph g, HashMap<Integer, Node[]> mrnas_per_genome) {
        ArrayList<Integer> genomes_without_anno = new ArrayList<>();
        String mrna_or_prot = "mRNA's", mrna_or_prot_file = "mrna";

        StringBuilder overview_builder = new StringBuilder();
        StringBuilder genome_builder = new StringBuilder();

        int total_genomes = Integer.parseInt(g.getNodes("pangenome").getFirst().getProperty("num_genomes"));

        for (int i = 1; i <= total_genomes; i++) {
            int mrna_with_anno = 0;
            HashMap<String, Integer> function_count_map = new HashMap<>();
            if (!mrnas_per_genome.containsKey(i)) {
                continue;
            }

            Node[] mrnaNodes = mrnas_per_genome.get(i);
            overview_builder.append("#Genome ").append(i).append("\n");
            genome_builder.append("#Genome ").append(i).append("\n");
            StringBuilder info_per_mrna = new StringBuilder();

            int[] mrnas_with_function = new int[9]; //go, pfam, interpro, tigrfam, cog, bgc, phobius signal peptide, phobius transmem domains, signalp signalpep
            for (Node mrna_node : mrnaNodes) {
                boolean function_found = false;
                if (!mrna_node.isSingleRelationship("has_homolog", true)) {
//                    throw new Exception("is not single");
                    return;
                }

                Node hmNode = g.getConnectedNode(mrna_node.id, "has_homolog", true);


//                Relationship hm_rel = mrna_node.getSingleRelationship(RelTypes.has_homolog, Direction.INCOMING);
                long hm_node_id = hmNode.id;
                long mrna_node_id = mrna_node.id;

                String mrna_id = (String) mrna_node.getProperty("id");

//                Relationship gene_rel = mrna_node.getSingleRelationship(RelTypes.codes_for, Direction.INCOMING);
                if (!mrna_node.isSingleRelationship("codes_for", true)) {
//                    throw new Exception("is not single");
                    return;
                }

                Node gene_node = g.getConnectedNode(mrna_node.id, "codes_for", true);
//                Node gene_node = gene_rel.getStartNode();


                long gene_node_id = gene_node.id;
                String gene_id = (String) gene_node.getProperty("id");
                String name = retrieveNamePropertyAsString(gene_node);
                if (name.equals("")) {
                    name = "-";
                }

                info_per_mrna.append("\nGene name: ").append(name)
                        .append("\nGene GFF id: ").append(gene_id)
                        .append("\nGene node id: ").append(gene_node_id)
                        .append("\nmRNA GFF id: ").append(mrna_id)
                        .append("\nmRNA node id: ").append(mrna_node_id)
                        .append("\nHomology group node id: ").append(hm_node_id)
                        .append("\nFunction:\n");
            }
        }
        for (int genome_nr : genomes_without_anno) {
            mrnas_per_genome.remove(genome_nr);
        }

    }


    public static HashMap<Integer, Node[]> get_mrna_nodes_by_hm_or_ids(MyGraph g) {
        HashMap<Integer, ArrayList<Node>> temp_genes_per_genome = new HashMap<>();
        HashMap<Integer, Node[]> genes_per_genome = new HashMap<>();

//        String NODE_ID = "2,2";
        ArrayList<Node> mrnas = g.getNodes("mRNA");
        String NODE_ID = "";
        for (int i = 0; i < mrnas.size(); i++) {
            if (i + 1 == mrnas.size()) {
                NODE_ID += mrnas.get(i).id;
            } else {
                NODE_ID += mrnas.get(i).id + ",";
            }
        }


        String[] gene_array;
        if (NODE_ID.contains(",")) { // string file provided on command line

            gene_array = NODE_ID.split(",");

            for (String gene_str : gene_array) {
                long id = Long.parseLong(gene_str);
                Node mrna_node = g.getNode((int) id);
                if (!mrna_node.label.equals("mRNA")) {
                   continue;
                }
//                test_if_correct_label(mrna_node, MRNA_LABEL, true);
                int genome_nr = Integer.parseInt(mrna_node.getProperty("genome"));
                temp_genes_per_genome.computeIfAbsent(genome_nr, i -> new ArrayList<>()).add(mrna_node);
            }
        }


        int counter = 0;
        for (Map.Entry<Integer, ArrayList<Node>> entry : temp_genes_per_genome.entrySet()) {
            int key = entry.getKey();
            ArrayList<Node> value_list = entry.getValue();
            counter++;
            Node[] node_array = value_list.toArray(new Node[value_list.size()]);
            genes_per_genome.put(key, node_array);
        }
        return genes_per_genome;
    }


    public static String retrieveNamePropertyAsString(Node node) {
        String name;
        try {
            name = (String) node.getProperty("name");
        } catch (ClassCastException cce) { // value is not a String
            // Framework does not support arrays
//            String[] nameArray = (String[]) node.getProperty("name");
//            name = Arrays.toString(nameArray).replace("[", "").replace("]", "").replace(",","/");
            name = "";
        }
        return name;
    }


}