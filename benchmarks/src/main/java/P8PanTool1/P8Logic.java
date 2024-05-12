package P8PanTool1;


import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.Node;

import java.util.*;

public class P8Logic {
    MyGraph my_g;

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        HashMap<String, Node[]>  mrnaNodesPerSequence = getOrderedMrnaNodesPerSequence(g, true);
        preparePhasedGenomeInformation(g);

        HashMap<String, ArrayList<String>> sequencesPerChromosomeMap = createSequencesPerChromosomeMapString();
        checkIfGeneCopiesWithinGenome(g, mrnaNodesPerSequence, sequencesPerChromosomeMap);
    }

    private HashMap<String, ArrayList<String>> createSequencesPerChromosomeMapString() {
        HashMap<String, ArrayList<String>> sequencesPerChromosomeMap = new HashMap<>();
        for (String sequenceId : phasingInfoMap.keySet()) {
            if (!sequenceId.contains("_")) { // its a genome number
                continue;
            }
            String[] seqIdArray = sequenceId.split("_");
            String[] phasingInfo = phasingInfoMap.get(sequenceId);
            sequencesPerChromosomeMap.computeIfAbsent(seqIdArray[0] + "_" + phasingInfo[0], k -> new ArrayList<>()).add(sequenceId);
        }
        return sequencesPerChromosomeMap;
    }

    public HashMap<String, Node[]> getOrderedMrnaNodesPerSequence(MyGraph g,boolean allow_same_start) {

        HashMap<String, Node[]> mrnaNodesPerSequence = new HashMap<>();
        int total_genomes = Integer.parseInt(g.getNodes("pangenome").getFirst().getProperty("num_genomes"));
        for (int i = 1; i <= total_genomes; i++) { // i is a genome number
            int total_mrna_counter = 0;
            ArrayList<Node> genome_nodes = g.getNodes("genome", "number", i + "");

            if(genome_nodes.size() > 1) {
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
//                    int[] address = (int[]) mrna_node.getProperty("address");
                    int[] address = new int[]{
                            1,2,3
                    };
                    gene_start_positions.add(address[2]);
                    mrna_nodes_per_start_position.computeIfAbsent(address[2], k -> new ArrayList<>()).add(mrna_node); // used to find genes with the same start position
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


    private void checkIfGeneCopiesWithinGenome(MyGraph g, HashMap<String, Node[]> mrnaNodesPerSequence,
                                               HashMap<String, ArrayList<String>> sequencesPerChromosomeMap) {

        if (phasingInfoMap == null || sequencesPerChromosomeMap.isEmpty()) { // no phasing
//            System.out.println("\r No phasing information found (for the sequence selection)");
            return;
        }

        int seqCounter = 0;
        for (String sequenceId : mrnaNodesPerSequence.keySet()) {
            seqCounter++;
//            System.out.print("\r Checking chromosome presence: " + seqCounter + "/" + mrnaNodesPerSequence.size() + "  ");
            String[] phasingInfo = phasingInfoMap.get(sequenceId); // example [1, D, 1D, 1_D]
            if (phasingInfo == null) {
                continue;
            }

            Node[] mrnaNodes = mrnaNodesPerSequence.get(sequenceId);
            String[] seqIdArray = sequenceId.split("_");
            int genomeNr = Integer.parseInt(seqIdArray[0]);
            StringBuilder singleCopyBuilder = new StringBuilder("x,y,block_nr,type\n");
            int blockNr = 0;
            for (Node mrnaNode : mrnaNodes) {
                // TODO INT ARRAY
//                int[] address = (int[]) mrnaNode.getProperty("address");
                int[] address = new int[]{
                        1,2,3,4
                };
                if (!mrnaNode.isSingleRelationship("has_homolog", true)) {
//                    throw new Exception("is not single");
                    return;
                }

                Node hmNode = g.getConnectedNode(mrnaNode.id, "has_homolog", true);

                if (hmNode == null) {
                    return;
                }

                // TODO INT ARRAY
//                int[] copyNumberArray = (int[]) hmNode.getProperty("copy_number");
                int[] copyNumberArray = new int[]{1, 1};
                String inOtherChromosome = "Other chromosome";
                if (copyNumberArray[genomeNr] == 1) { // only found once in genome
                    inOtherChromosome = "Single copy";
                } else { // found multiple times, check if it occurs in same chromosome
                    if (!hmNode.properties.containsKey("copy_number_genome_" + genomeNr)) {
                        System.out.println("This plot requires you to run gene_classification2 with the --sequence argument ");
                        return;
                    }
                    // TODO INT ARRAY
//                    int[] sequenceCopyNumberArray = (int[]) hmNode.getProperty("copy_number_genome_" + genomeNr);
                    int[] sequenceCopyNumberArray = new int[]{1};

                    inOtherChromosome = "Single copy"; // this is new
                    ArrayList<String> chromosomeSeqIdentifiers = sequencesPerChromosomeMap.get(genomeNr + "_" + phasingInfo[0]);
                    for (int i = 0; i < sequenceCopyNumberArray.length; i++) {
                        //TODO:
//                        if (!selectedSequences.contains(genomeNr + "_" + (i + 1))) {
//                            continue;
//                        }
                        if (sequenceCopyNumberArray[i] > 0 && !chromosomeSeqIdentifiers.contains(genomeNr + "_" + (i + 1))) {
                            inOtherChromosome = "Other chromosome";
                            break;
                        }
                    }
                }

                singleCopyBuilder.append(address[2]).append(",0,").append(blockNr).append(",").append(inOtherChromosome).append("\n");
                singleCopyBuilder.append(address[2]).append(",5,").append(blockNr).append(",").append(inOtherChromosome).append("\n");
                singleCopyBuilder.append(address[3]).append(",5,").append(blockNr).append(",").append(inOtherChromosome).append("\n");
                singleCopyBuilder.append(address[3]).append(",0,").append(blockNr).append(",").append(inOtherChromosome).append("\n");
                blockNr++;
            }
        }
//        System.out.println("");
    }

    public int preparePhasedGenomeInformation(MyGraph g) {
        phasingInfoMap = new HashMap<>(); // genome number and sequence identifiers as key
        int counter = 0;
        HashSet<Integer> genomesWithPhasing = new HashSet<>();
        Iterator<Node> sequenceNodes = g.getNodes("sequence").iterator();
//            ResourceIterator<Node> sequenceNodes = GRAPH_DB.findNodes(SEQUENCE_LABEL);
        long sequenceNodeCount = g.getNodes("sequence").size();
        while (sequenceNodes.hasNext()) {
            counter++;
            Node sequenceNode = sequenceNodes.next();

            if (!sequenceNode.properties.containsKey("phasing_assigned")) {
                continue;
            }
            String sequenceId = (String) sequenceNode.getProperty("identifier");
            int genomeNr = Integer.parseInt(sequenceNode.getProperty("genome"));
            int sequenceNr = Integer.parseInt(sequenceNode.getProperty("number"));


            if (!sequenceNode.properties.containsKey("phasing_ID")) {
                continue;
            }

            int chromosomeNr = Integer.parseInt(sequenceNode.getProperty("phasing_chromosome"));
            String phasingId = (String) sequenceNode.getProperty("phasing_ID"); // consists of chromosome number + "_" + phase
            String[] phasing_array = phasingId.split("_"); // 1_A becomes [1,A], 2_unphased [2,unphased]
            String[] phasing_info = new String[4]; //[chromosome number, phasing letter, combination of the two, combination of the two with '_' ]
            phasing_info[0] = chromosomeNr + "";
            phasing_info[3] = phasingId;
            if (chromosomeNr != 0) {
                genomesWithPhasing.add(genomeNr);
                String underscore = "_";
                if (!phasing_array[1].equals("unphased")) {
                    underscore = "";
                }
                phasing_info[1] = phasing_array[1];
                phasing_info[2] = chromosomeNr + underscore + phasing_array[1];
            } else { // chromosome 0
                phasing_info[1] = "unphased";
                phasing_info[2] = "0_unphased";
            }
            phasingInfoMap.put(sequenceId, phasing_info); // [1, B, 1B, 1_B]
        }
//        System.out.println(""); // to print nicely on next line. TODO remove with progress bar

        if (genomesWithPhasing.size() == 1) {
            phasingInfoMap.isEmpty();
//            System.out.println("One genome with chromosome/phasing information");
        } else if (genomesWithPhasing.size() > 1) {
            phasingInfoMap.isEmpty();
            //            System.out.println(genomesWithPhasing.size() + " genomes with chromosome/phasing information");
        } else {
            phasingInfoMap.isEmpty();
            //            System.out.println("No phased genomes are present");
        }
        return genomesWithPhasing.size();
    }


}