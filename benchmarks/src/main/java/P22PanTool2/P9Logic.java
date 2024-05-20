package P22PanTool2;


import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.*;

public class P9Logic {
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
                int bgc_counter = get_bgc_info(g, gene_node, info_per_mrna, function_count_map);
                if (bgc_counter > 0) {
                    mrnas_with_function[5]++;
                    function_found = true;
                }


                int go_counter = print_connected_function_node_info(g, mrna_node, "has_go", info_per_mrna, function_count_map);
                if (go_counter > 0) {
                    mrnas_with_function[0]++;
                    function_found = true;
                }
                int pfam_counter = print_connected_function_node_info(g, mrna_node, "has_pfam", info_per_mrna, function_count_map);
                if (pfam_counter > 0) {
                    mrnas_with_function[1]++;
                    function_found = true;
                }
                int interpro_counter = print_connected_function_node_info(g, mrna_node, "has_interpro", info_per_mrna, function_count_map);
                if (interpro_counter > 0) {
                    mrnas_with_function[2]++;
                    function_found = true;
                }

                int tigrfam_counter = print_connected_function_node_info(g, mrna_node, "has_tigrfam", info_per_mrna, function_count_map);
                if (tigrfam_counter > 0) {
                    mrnas_with_function[3]++;
                    function_found = true;
                }

                ArrayList<Node> phobiuss = g.getConnectedNodes(mrna_node, "has_phobius");

                if (!phobiuss.isEmpty()) {
                    function_found = true;
                    Node phobius = phobiuss.get(0);
                    if (phobius.properties.containsKey("phobius_signal_peptide")) {
                        String phobius_signalpep = (String) phobius.getProperty("phobius_signal_peptide");
                        if (phobius_signalpep.equals("yes")) {
                            function_count_map.merge("Phobius signal peptide", 1, Integer::sum);
                            info_per_mrna.append(" Phobius signal peptide").append("\n");
                            mrnas_with_function[6]++;
                        }
                    }

                    if (phobius.properties.containsKey("phobius_transmembrane")) {
                        int transmem_domains = Integer.parseInt(phobius.getProperty("phobius_transmembrane"));
                        if (transmem_domains > 0) {
                            function_count_map.merge("Phobius transmembrane domains", 1, Integer::sum);
                            info_per_mrna.append(transmem_domains).append(" Phobius transmembrane domains").append("\n");
                            mrnas_with_function[7]++;
                        }
                    }
                }

                ArrayList<Node> signalps = g.getConnectedNodes(mrna_node, "has_signalp");

                if (!signalps.isEmpty()) {
                    function_found = true;
                    Node signalp = signalps.get(0);
                    if (signalp.properties.containsKey("signalp_signal_peptide")) { // type can be 'yes', 'SP(Sec/SPI)', 'LIPO(Sec/SPII)' or 'TAT(Tat/SPI)'
                        String signalp_signalpep = (String) signalp.getProperty("signalp_signal_peptide");
                        if (signalp_signalpep.equals("yes")) {
                            signalp_signalpep = "(no type)";
                        }
                        function_count_map.merge("SignalP signal peptide", 1, Integer::sum);
                        info_per_mrna.append(" SignalP ").append(signalp_signalpep).append(" signal peptide").append("\n");
                        mrnas_with_function[8]++;
                    }
                }
                ArrayList<Node> cogs = g.getConnectedNodes(mrna_node, "has_cog");

                if (!cogs.isEmpty()) {
                    Node cog = cogs.get(0);
                    Map<String, String> props = cog.properties;
                    function_found = true;
                    mrnas_with_function[4]++;
                    String cog_cat = (String) cog.getProperty("COG_category");
                    String cogId = (String) cog.getProperty("COG_id");
                    String cogDescription = "";
                    if (cog.properties.containsKey("COG_description")) {
                        cogDescription = "," + (String) cog.getProperty("COG_description");
                    }
                    info_per_mrna.append(" COG category ").append(cog_cat).append(",").append(cogId).append(cogDescription).append("\n");
                    function_count_map.merge("COG category " + cog_cat + "," + cogId, 1, Integer::sum);
                }
                info_per_mrna.append("\n");
                if (function_found) {
                    mrna_with_anno++;
                }
            }

            overview_builder.append(mrna_with_anno).append("/").append(mrnaNodes.length).append(" ").append(mrna_or_prot).append(" have a functional annotation\n")
                    .append(info_per_mrna);
            genome_builder.append(mrna_with_anno).append("/").append(mrnaNodes.length).append(" ").append(mrna_or_prot).append(" have a functional annotation\n\n");
            create_function_per_genome_overview(function_count_map, genome_builder, mrnas_with_function);
            if (mrna_with_anno == 0) {
                genomes_without_anno.add(i);
            }
        }
        for (int genome_nr : genomes_without_anno) {
            mrnas_per_genome.remove(genome_nr);
        }

    }

    public static int get_bgc_info(MyGraph g, Node gene_node, StringBuilder overview_builder, HashMap<String, Integer> function_count_map) {
        List<Edge> rels = gene_node.getEdges().stream().filter(edge -> edge.label.equals("part_of")).toList(); // relation to bgc node
        int found_counter = 0;
        for (Edge rel : rels) {
            int position = Integer.parseInt(rel.properties.get("position"));
            Node bgc_node = g.getNode(rel.to);
            String type = (String) bgc_node.getProperty("type");
            int length = Integer.parseInt(bgc_node.properties.get("length"));
            overview_builder.append(" gene ").append(position).append("/").append(length).append(" of '").append(type).append(" bgc");
            function_count_map.merge("gene of " + type + ", length " + length + "bgc", 1, Integer::sum);
            found_counter++;
        }
        return found_counter;
    }

    /**
     * @param function_count_map
     * @param genome_builder
     * @param mrnas_with_function
     */
    public static void create_function_per_genome_overview(HashMap<String, Integer> function_count_map,
                                                           StringBuilder genome_builder, int[] mrnas_with_function) {
        String mrna_or_prot = "mRNA's";

        int[] function_count_array = new int[8]; //go, pfam, interpro, tigrfam, cog, bgc, effector, receptor, transmembrane
        StringBuilder pfam = new StringBuilder();
        StringBuilder signalp = new StringBuilder("with SignalP signal peptides: ");
        StringBuilder phobius_signalpep = new StringBuilder("with Phobius signal peptides: ");
        StringBuilder p_transmembrane = new StringBuilder("with Phobius transmembrane: ");
        StringBuilder go_bp = new StringBuilder("GO sub categories\n\nBiological process\n");
        StringBuilder go_mf = new StringBuilder("Molecular function\n");
        StringBuilder go_cl = new StringBuilder("Cellular component\n");
        StringBuilder go = new StringBuilder();
        StringBuilder interpro = new StringBuilder();
        StringBuilder tigrfam = new StringBuilder();
        StringBuilder cog = new StringBuilder();
        StringBuilder bgc = new StringBuilder();
        for (String key : function_count_map.keySet()) {
            int value = function_count_map.get(key);
            if (key.startsWith("PF")) {
                pfam.append(key).append(": ").append(value).append("\n");
                function_count_array[1]++;
            } else if (key.startsWith("SignalP")) {
                signalp.append(value);
            } else if (key.startsWith("Phobius signal")) {
                phobius_signalpep.append(value);
            } else if (key.startsWith("Phobius transmembrane")) { // Phobius transmembrane domains
                p_transmembrane.append(value);
            } else if (key.startsWith("GO")) {
                function_count_array[0]++;
                go.append(key).append(": ").append(value).append("\n");
            } else if (key.endsWith("molecular_function")) {
                key = key.replace("molecular_function", "");
                if (key.equals("")) {
                    continue;
                }
                go_mf.append(" ").append(key).append(": ").append(value).append("\n");
            } else if (key.endsWith("biological_process")) {
                key = key.replace("biological_process", "");
                if (key.equals("")) {
                    continue;
                }
                go_bp.append(" ").append(key).append(": ").append(value).append("\n");
            } else if (key.endsWith("cellular_component")) {
                key = key.replace("cellular_component", "");
                if (key.equals("")) {
                    continue;
                }
                go_cl.append(" ").append(key).append(": ").append(value).append("\n");
            } else if (key.startsWith("IPR")) {
                interpro.append(key).append(": ").append(value).append("\n");
                function_count_array[2]++;
            } else if (key.endsWith("bgc")) {
                function_count_array[5]++;
                key = key.replace("bgc", "");
                bgc.append(key).append(": ").append(value).append("\n");
            } else if (key.startsWith("TIGR")) { // tigr
                function_count_array[3]++;
                tigrfam.append(key).append(": ").append(value).append("\n");
            } else if (key.startsWith("COG")) {
                cog.append(key).append(": ").append(value).append("\n");
                function_count_array[4]++;
            } else {
                System.exit(1);
            }
        }

        genome_builder.append("Number of ").append(mrna_or_prot).append(" with at least one of the following functions, number of distinct functions found\n");
        if (mrnas_with_function[0] != 0) {
            genome_builder.append("GO: ").append(mrnas_with_function[0]).append(",").append(function_count_array[0]).append("\n");
        }
        if (mrnas_with_function[1] != 0) {
            genome_builder.append("PFAM: ").append(mrnas_with_function[1]).append(",").append(function_count_array[1]).append("\n");
        }
        if (mrnas_with_function[2] != 0) {
            genome_builder.append("InterPro: ").append(mrnas_with_function[2]).append(",").append(function_count_array[2]).append("\n");
        }
        if (mrnas_with_function[3] != 0) {
            genome_builder.append("TIGRFAM: ").append(mrnas_with_function[3]).append(",").append(function_count_array[3]).append("\n");
        }
        if (mrnas_with_function[4] != 0) {
            genome_builder.append("COG: ").append(mrnas_with_function[4]).append(",").append(function_count_array[4]).append("\n");
        }
        if (mrnas_with_function[5] != 0) {
            genome_builder.append("BGC: ").append(mrnas_with_function[5]).append(",").append(function_count_array[5]).append("\n");
        }
        if (mrnas_with_function[6] != 0) {
            genome_builder.append("Secreted proteins: ").append(mrnas_with_function[6]).append("\n");
        }
        if (mrnas_with_function[7] != 0) {
            genome_builder.append("Receptor: ").append(mrnas_with_function[7]).append("\n");
        }
        if (mrnas_with_function[8] != 0) {
            genome_builder.append("Transmembrane: ").append(mrnas_with_function[8]).append("\n");
        }
        genome_builder.append("\nFound functions\n")
                .append("GO: ").append("\n")
                .append(go).append("\n")
                .append(go_bp).append("\n")
                .append(go_mf).append("\n")
                .append(go_cl).append("\n")
                .append("PFAM domains: ").append("\n").append(pfam).append("\n")
                .append("InterPro domains: ").append("\n").append(interpro).append("\n")
                .append("TIGRFAMs: ").append("\n").append(tigrfam).append("\n")
                .append("Biosynthetic gene clusters: ").append("\n").append(bgc).append("\n")
                .append("COG proteins: ").append("\n").append(cog).append("\n");

        String signalp_str = signalp.toString();
        if (signalp_str.endsWith(": ")) {
            signalp_str += 0;
        }
        genome_builder.append(signalp_str).append("\n");

        String phobius_signalpep_str = phobius_signalpep.toString();
        if (phobius_signalpep_str.endsWith(": ")) {
            phobius_signalpep_str += 0;
        }
        genome_builder.append(phobius_signalpep_str).append("\n");

        String transmembrane_str = p_transmembrane.toString();
        if (transmembrane_str.endsWith(": ")) {
            transmembrane_str += 0;
        }
        genome_builder.append(transmembrane_str).append("\n\n");
    }

    public static int print_connected_function_node_info(MyGraph g, Node target_node, String reltype, StringBuilder overview_builder,
                                                         HashMap<String, Integer> function_count_map) {

        List<Edge> relationships = target_node.getOutgoingEdges().stream().filter(edge -> edge.label.equals(reltype)).toList();
        int found_counter = 0;
        for (Edge rel : relationships) {
            found_counter++;
            Node function_node = g.getNode(rel.to);
            String function_id = (String) function_node.getProperty("id");
            String function_name = (String) function_node.getProperty("name");
            overview_builder.append(" ").append(function_id).append(", ").append(function_name).append("\n");
            function_count_map.merge(function_id + ", " + function_name, 1, Integer::sum);
            if (function_node.label.equals("GO")) {
                String sub_cats = (String) function_node.getProperty("sub category");
                String[] sub_cat_array = sub_cats.split(" & ");
                for (String sub_cat : sub_cat_array) {
                    String cat = (String) function_node.getProperty("category");
                    function_count_map.merge(sub_cat + cat, 1, Integer::sum);
                }
            }
        }
        return found_counter;
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