package P10PanTool3;


import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.*;
import java.util.stream.Collectors;

public class P10Logic {
    MyGraph my_g;

    public static HashMap<Integer, String> geno_pheno_map; // genome number is coupled to its phenotype
    public static HashMap<String, int[]> phenotype_map; // key is phenptype, array contains genome numbers

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        // TODO
        retrieve_gene_seq_check_presence_hmgroup(g, "", new ArrayList<>(), new StringBuilder());
    }


    /**
     * Collects a lot of statistics about genes that were found per gene
     * @param geneName the gene name that was searched for
     * @param gene_list the gene names that were found
     * @param log_builder
     *
     * The functions assumes there is only one mRNA for each gene. Will not work for eukaryotes
     */
    public void retrieve_gene_seq_check_presence_hmgroup(MyGraph g, String geneName, ArrayList<Node> gene_list, StringBuilder log_builder) {
        HashMap<Node, StringBuilder> prot_seqs_per_hmgroup = new HashMap<>(); // key is homology node.
        HashMap<Node, StringBuilder> nuc_seqs_per_hmgroup = new HashMap<>();
        HashMap<Node, ArrayList<Integer>> dna_lengths_per_hmgroup = new HashMap<>();
        HashMap<Node, ArrayList<Integer>> prot_lengths_per_hmgroup = new HashMap<>();
        StringBuilder prot_seq_builder = new StringBuilder();
        StringBuilder nuc_seq_builder = new StringBuilder();
        ArrayList<Integer> gene_length_list = new ArrayList<>();
        ArrayList<Integer> protein_length_list = new ArrayList<>();
        HashMap<Integer, Integer> gene_genome_map = new HashMap<>();

        int total_genomes = Integer.parseInt(g.getNodes("pangenome").getFirst().getProperty("num_genomes"));

        int [] presence_array = new int[total_genomes];
        for(int i = 1 ; i <= total_genomes; i++) {
            gene_genome_map.put(i, 0);
        }
        HashMap<String, String> prot_seqs_per_genome = new HashMap<>();
        // key is genome_nr "_" + copy_number. Value is protein sequence used for when multiple copies are found
        HashMap<String, String> nuc_seqs_per_genome = new HashMap<>();
        Set<Node> all_hms = new HashSet<>(); // homology_group nodes

        String address_str = "";
        for (Node gene_node : gene_list) {
            int gene_length = 0, protein_length = 0;
            int genome_nr = Integer.parseInt(gene_node.getProperty("genome"));

            String gene_name = retrieveNamePropertyAsString(gene_node);

//            Iterable<Edge> rels = gene_node.getRelationships(RelTypes.codes_for);
            List<Edge> rels = gene_node.getEdges().stream().filter(edge -> edge.label.equals("codes_for")).toList(); // relation to bgc node


            String prot_sequence = "", nuc_sequence = "", seq_header = "", mrna_node_str = "", hm_node_str = "no homology group";
            for (Edge rel : rels) { // for MLSA, the functions assumes there is only one mRNA for each gene. Will not work for eukaryotes
                Node mrna_node = g.getNode(rel.to);
                mrna_node_str = mrna_node.id +"";
                if (!mrna_node.properties.containsKey("protein_ID")) {
                    continue;
                }

                String protein_id = (String) mrna_node.getProperty("protein_ID");
                gene_length = Integer.parseInt(mrna_node.getProperty("length"));
                protein_length = Integer.parseInt(mrna_node.getProperty("protein_length"));

//                int[] address = (int[]) mrna_node.getProperty("address");
                int[] address = new int[]{
                        1,2,3,4
                };

                address_str = address[0] + " " + address[1] + " " + address[2] + " " + address[3];
                gene_length_list.add(gene_length);
                protein_length_list.add(protein_length);

//                Relationship hm_rel = mrna_node.getSingleRelationship(RelTypes.has_homolog, Direction.INCOMING);
                if (!mrna_node.isSingleRelationship("has_homolog", true)) {
//                    throw new Exception("is not single");
                    return;
                }

                Node hmNode = g.getConnectedNode(mrna_node.id, "has_homolog", true);


                prot_sequence = get_protein_sequence(mrna_node);
                nuc_sequence = get_nucleotide_sequence(mrna_node);
                prot_sequence = splitSeqOnLength(prot_sequence, 80);
                nuc_sequence = splitSeqOnLength(nuc_sequence, 80);
                String phenotype = get_phenotype_for_genome(genome_nr, true);
                if (hmNode != null) { // mrna is connected to an homology_group node
                    Node hm_node = hmNode;
                    hm_node_str =  hm_node.id +"";

                        seq_header = ">" + protein_id + " " + gene_name + "_gn_" + genome_nr + "_" + gene_node.id + "_hmgroup_" + hm_node.id + phenotype + "\n";

                    all_hms.add(hm_node);
                    dna_lengths_per_hmgroup.computeIfAbsent(hm_node, n -> new ArrayList<>()).add(gene_length);
                    prot_lengths_per_hmgroup.computeIfAbsent(hm_node, n -> new ArrayList<>()).add(protein_length);
                    prot_seqs_per_hmgroup.computeIfAbsent(hm_node, n -> new StringBuilder())
                            .append(seq_header).append(prot_sequence).append("\n");
                    nuc_seqs_per_hmgroup.computeIfAbsent(hm_node, n -> new StringBuilder())
                            .append(seq_header).append(nuc_sequence).append("\n");
                } else { // pr
                        seq_header = ">" + protein_id + " " + gene_name + "_gn_" + genome_nr + "_" + gene_node.id + phenotype + "\n";
                }
            }
            if (gene_length == 0) {
                log_builder.append(gene_name).append(", ").append(genome_nr).append(", ").append(gene_node.id).append(", ").append(mrna_node_str)
                        .append(", Protein sequence is missing! Something wrong with GFF?\n");
                continue;
            }
            presence_array[genome_nr-1]++;
            log_builder.append(gene_name).append("; ").append(genome_nr).append("; ").append(gene_node.id).append("; ").append(mrna_node_str).append("; ")
                    .append(hm_node_str).append("; ").append(gene_length).append("; ").append(protein_length).append("; ").append(address_str).append("\n");
            int gene_count = gene_genome_map.get(genome_nr);
            gene_count++;
            gene_genome_map.put(genome_nr, gene_count);
            prot_seqs_per_genome.put(genome_nr + "_" + gene_count + "_header", seq_header);
            prot_seqs_per_genome.put(genome_nr + "_" + gene_count, prot_sequence);
            nuc_seqs_per_genome.put(genome_nr + "_" + gene_count, nuc_sequence);
        }

        boolean multi_groups = false; // when true, the genes are found in multiple homology groups
        if (prot_seqs_per_hmgroup.size() > 1) {
            multi_groups = true;

            for (Node hm_node : prot_seqs_per_hmgroup.keySet()) {
                StringBuilder prot_seq = prot_seqs_per_hmgroup.get(hm_node);
                StringBuilder nuc_seq = nuc_seqs_per_hmgroup.get(hm_node);

            }
        }

        ArrayList<Integer> distinct_gene_lengths = remove_duplicates_from_AL_int(gene_length_list);
        String gene_lengths = determine_frequency_list_int(gene_length_list);
        double gene_average = average(gene_length_list);
        String gene_avg_str = String.format("%.2f", gene_average);
        double protein_average = average(protein_length_list);
        String prot_avg_str = String.format("%.2f", protein_average);
        String protein_lengths = determine_frequency_list_int(protein_length_list);

        log_builder.append("\nAverage gene length: ").append(gene_avg_str)
                .append("\nAll gene lengths: ").append(gene_lengths)
                .append("\nAverage protein length: ").append(prot_avg_str)
                .append("\nAll protein lengths: ").append(protein_lengths)
                .append("\n\nNumber of copies per genome ").append(Arrays.toString(presence_array)).append("\n");
        String absent_genomes = "Warning! Absent in genomes: ", present_genomes = "\nFound in genomes: ", multiple_copies = "Genomes with multiple copies: ";
        for (int genome_nr : gene_genome_map.keySet()) {

            int value = gene_genome_map.get(genome_nr);
            if (value == 0) {
                absent_genomes += genome_nr + ",";
            } else {
                present_genomes += genome_nr + ",";
                if (value > 1) {
                    HashSet<Integer> duplicate_set = check_if_duplicate_sequences(value, genome_nr, prot_seqs_per_genome);
                    if (value - duplicate_set.size() == 1) {
                        if (duplicate_set.size() == 1) {
                            log_builder.append("Removed 1 gene from genome ").append(genome_nr).append(" because it is identical\n");
                        } else {
                            log_builder.append("Removed ").append(duplicate_set.size()).append(" genes from genome ")
                                    .append(genome_nr).append(" because they are identical\n");
                        }
                        for (int copy_number: duplicate_set) {
                            prot_seqs_per_genome.remove(genome_nr + "_" + copy_number);
                            nuc_seqs_per_genome.remove(genome_nr + "_" + copy_number);
                        }
                    } else { // extra copy
                        multiple_copies += genome_nr + ",";
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : nuc_seqs_per_genome.entrySet()) {
            String genome_str = entry.getKey();
            String nuc_seq = entry.getValue();
            String prot_seq = prot_seqs_per_genome.get(genome_str);
            String header = prot_seqs_per_genome.get(genome_str + "_header");
            prot_seq_builder.append(header).append(prot_seq).append("\n");
            nuc_seq_builder.append(header).append(nuc_seq).append("\n");
        }

        absent_genomes = absent_genomes.replaceFirst(".$","");
        present_genomes = present_genomes.replaceFirst(".$","");
        multiple_copies = multiple_copies.replaceFirst(".$","");
        if (present_genomes.length() > 18) {
            // do nothing
        } else {
            log_builder.append("\nWarning! Gene was not found!\n\n");
        }
        String[] present_array = present_genomes.split(",");
        String size_str = get_size_str(distinct_gene_lengths);
        boolean absent = true;
        if (absent_genomes.length() > 27) {
            log_builder.append(present_genomes).append("\n")
                    .append(absent_genomes).append("\n");
        } else {
            log_builder.append("Gene was found in every genome!\n");
            absent = false;
        }

        if (multiple_copies.length() > 29) {
            log_builder.append("Warning! ").append(multiple_copies).append("\n");
        }

        if (all_hms.size() == 1) {
            log_builder.append("All genes are part of the same homology group\n");
            if (multiple_copies.length() > 29 && !absent) { // 1 group but multiple copies, extra warning!
                log_builder.append("Warning! You must remove duplicate copies from the input files\n ");
            }
        } else if (all_hms.size() > 1) {
            log_builder.append("Warning! Genes were found in ").append(all_hms.size()).append(" different homology groups! Sequences for are written to:\n");
        } else {
            log_builder.append("Warning! No (active) homology grouping is present!\n");
        }

        StringBuilder hm_builder = new StringBuilder();
        int perfect_group_counter = 0;
        for (Node hm_node : all_hms) {
            ArrayList<Integer> dna_lengths_lists = dna_lengths_per_hmgroup.get(hm_node);
            ArrayList<Integer> prot_lengths_lists = prot_lengths_per_hmgroup.get(hm_node);
            String dna_freqs = determine_frequency_list_int(dna_lengths_lists);
            String prot_freqs = determine_frequency_list_int(prot_lengths_lists);
            int num_members = Integer.parseInt(hm_node.getProperty("num_members"));
//            TODO: No int arrays in framework
//            int[] temp_copy_number = (int[]) hm_node.getProperty("copy_number");
//            int[] copy_number = remove_first_position_array(temp_copy_number);
            int[] copy_number = new int[]{1,2,3,4,5};
            hm_builder.append("Homology group ").append(hm_node.id).append(", ").append(num_members).append(" members ").append(Arrays.toString(copy_number))
                    .append("\nGene lengths: ").append(dna_freqs)
                    .append("\nProtein lengths: ").append(prot_freqs).append("\n");

            if (multi_groups && num_members == total_genomes && perfect_group_counter == 0) {
                log_builder.append("Warning! Multiple homology groups are found! Group ").append(hm_node.id).append(" is selected for ").append(geneName)
                        .append(".fasta as the group size matches the number of genomes.\n");
                prot_seq_builder = prot_seqs_per_hmgroup.get(hm_node);
                nuc_seq_builder = nuc_seqs_per_hmgroup.get(hm_node);
                perfect_group_counter++;
            } else if (multi_groups && num_members == total_genomes) {
                log_builder.append("Warning! Group ").append(hm_node.id).append(" is also suitable for the analysis for ").append(geneName)
                        .append(". It was not selected because a suitable candidate was already found. \n");
            }
        }

        log_builder.append("\n").append(hm_builder.toString()).append("\n");

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
    public static String get_protein_sequence(Node mrna_node) {
        String sequence = "";
        if (mrna_node.properties.containsKey("protein")) {
            sequence = (String) mrna_node.properties.getOrDefault("protein", "");
        } else {
            sequence = (String) mrna_node.properties.getOrDefault("protein_sequence", "");
        }
        return sequence;
    }

    public static String get_nucleotide_sequence(Node mrna_node) {
        if (mrna_node.properties.containsKey("nucleotide_sequence")) {
            return (String) mrna_node.getProperty("nucleotide_sequence");
        } else if (mrna_node.properties.containsKey("dna_sequence")) {
            return (String) mrna_node.getProperty("dna_sequence");
        } else if (mrna_node.properties.containsKey("sequence")) {
            if (mrna_node.getProperty("sequence") instanceof String) {
                return (String) mrna_node.getProperty("sequence");
            }
        } else {
            throw new RuntimeException("Could not find sequence");
        }
        return "";
    }

    public static String splitSeqOnLength(String sequence, int length) {
        StringBuilder sequenceSB = new StringBuilder();
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            sequenceSB.append(c);
            if ((i+1) % length == 0) {
                sequenceSB.append("\n");
            }
        }
        String sequenceString = sequenceSB.toString();
        if (sequenceString.endsWith("*")) {
            sequenceString = sequenceString.replaceAll("\\*$", "");
        }
        return sequenceString;
    }

    public static String get_phenotype_for_genome(int genome, boolean space_in_front) {
        String add = "";
            String pheno = "Unknown";
            if (geno_pheno_map.containsKey(genome)) {
                pheno = geno_pheno_map.get(genome);
                if (pheno.equals("?")) {
                    pheno = "Unknown";
                }
            }
            if (space_in_front) {
                add += " " + pheno;
            } else {
                add = pheno;
            }
        return add;
    }

    public static void retrieve_phenotypes(MyGraph g) {
        geno_pheno_map = new HashMap<>();
        phenotype_map = new HashMap<>();

        int pheno_node_count = (int) g.getNodes("phenotype").size();
        if (pheno_node_count == 0) {
            System.exit(1);
        }
//        ResourceIterator<Node> pheno_nodes = GRAPH_DB.findNodes(PHENOTYPE_LABEL);
        Iterator<Node> pheno_nodes = g.getNodes("phenotype").iterator();
        HashMap<String, ArrayList<Integer>> temp_phenotype_map = new HashMap<>();
        Object value;
        while (pheno_nodes.hasNext()) {
            Node pheno_node = pheno_nodes.next();
            int current_genome = Integer.parseInt(pheno_node.getProperty("genome"));
            if (pheno_node.properties.containsKey("species")) {
                value = pheno_node.getProperty("species");
            } else {
                System.exit(1);
                continue;
            }


            if (value instanceof String) { // It's a String
                String value_str = value.toString();
                if (value_str.equals("?")) {
                    value_str = "Unknown";
                }
                geno_pheno_map.put(current_genome, value_str);
                temp_phenotype_map.computeIfAbsent(value_str, s -> new ArrayList<>()).add(current_genome);
            } else if (value instanceof Integer) { // It's an Integer
                String value_str = value.toString();
                geno_pheno_map.put(current_genome, value_str);
                temp_phenotype_map.computeIfAbsent(value_str, s -> new ArrayList<>()).add(current_genome);
            } else if (value instanceof Boolean) { // It's a Boolean
                String value_str = value.toString();
                geno_pheno_map.put(current_genome, value_str);
                temp_phenotype_map.computeIfAbsent(value_str, s -> new ArrayList<>()).add(current_genome);
            } else {
                System.exit(1);
            }
        }

        for (String phenotype : temp_phenotype_map.keySet()) {
            ArrayList<Integer> genome_list = temp_phenotype_map.get(phenotype);
            int[] genome_array = genome_list.stream().mapToInt(i -> i).toArray();
            Arrays.sort(genome_array);
            phenotype_map.put(phenotype, genome_array);
        }
    }

    public static ArrayList<Integer> remove_duplicates_from_AL_int(ArrayList<Integer> list) {
        List<Integer> newList = list.stream()
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(newList);
        ArrayList tester = new ArrayList<>(newList);
        return tester;
    }
    public static String determine_frequency_list_int(ArrayList<Integer> size_list) {
        ArrayList<Integer> distinct_size_list = remove_duplicates_from_AL_int(size_list);
        StringBuilder output = new StringBuilder();
        for (int size : distinct_size_list) {
            int occurrences = Collections.frequency(size_list, size);
            output.append(size).append("(").append(occurrences).append("x), ");
        }
        return output.toString().replaceFirst(".$", "").replaceFirst(".$", ""); // remove last character (2x)
    }

    public static <T extends Number & Comparable<T>> double average(List<T> numbersList) {
        if (numbersList.isEmpty()) {
            throw new IllegalArgumentException("Cannot compute average on an empty list of numbers");
        }
        double sum = 0;
        for (Number number : numbersList) {
            sum += number.doubleValue();
        }
        return sum / numbersList.size();
    }

    public HashSet<Integer> check_if_duplicate_sequences(int value, int genome_nr, HashMap<String, String> prot_seqs_per_genome) {
        HashSet<Integer> duplicate_set = new HashSet<>();
        for (int i = 1; i <= value; i++) {
            String prot_sequence1 = prot_seqs_per_genome.get(genome_nr + "_" + i);
            for (int j = i+1; j <= value; j++) {
                if (i == j) {
                    continue;
                }
                String prot_sequence2 = prot_seqs_per_genome.get(genome_nr + "_" + j);
                if (prot_sequence1.equals(prot_sequence2)) {
                    duplicate_set.add(j);
                }
            }
        }
        return duplicate_set;
    }

    public String get_size_str(ArrayList<Integer> distinct_gene_lengths) {
        int size1 = distinct_gene_lengths.get(0);
        String size_str = size1 +"";
        if (distinct_gene_lengths.size() == 1) {
            size_str += "bp";
            return size_str;
        }
        int size2 = distinct_gene_lengths.get(distinct_gene_lengths.size() - 1);
        size_str += "-" + size2 + "bp";
        return size_str;
    }
}