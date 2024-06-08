package P10PanToolE;


import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;
import tudcomponents.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class P10Logic {
    MyGraph my_g;

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        preparePhasedGenomeInformation(g);
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
            int highest;
            if (chromosomeNr != 0) {
                genomesWithPhasing.add(genomeNr);
                String underscore = "_";
                if (!phasing_array[1].equals("unphased")) {
                    underscore = "";
                }
                phasing_info[1] = phasing_array[1];
                phasing_info[2] = chromosomeNr + underscore + phasing_array[1];


                ArrayList<Node> connected_sequences = g.getConnectedNodes(sequenceNode, "mrnas");
                String settings = "undefined";
                if (connected_sequences.size() > 0) {
                    for (Node connected_sequence : connected_sequences) {
                        ArrayList<Edge> edges = g.getRelationshipsBetweenNodes(sequenceNode, connected_sequence);
                        Edge connection = edges.getFirst();
                        boolean is_enabled = Boolean.parseBoolean(connection.properties.get("is_enabled"));
                        if (is_enabled) {
                            ArrayList<Node> seq_settings = g.getConnectedNodes(connected_sequence, "settings");
                            if (seq_settings.size() > 0) {
                                if (!g.checkSingle(connected_sequence.id, "settings")) {
                                    settings = "Multi";
                                } else {
                                    Node ss = seq_settings.getFirst();
                                    Type property = g.getNodeProperty(ss, "value").type;
                                    if (property == Type.INT) {
                                        settings = "int";
                                    } else if (property == Type.BOOLEAN) {
                                        settings = "boolean";
                                    } else {
                                        settings = "string";
                                    }
                                }
                            }
                        } else {
                            settings = "None";
                        }
                    }
                } else {
                    settings = "Sequence not connected";
                }
                phasing_info[1] = settings;
            } else { // chromosome 0
                phasing_info[1] = "unphased";
                phasing_info[2] = "0_unphased";
            }
            phasingInfoMap.put(sequenceId, phasing_info); // [1, B, 1B, 1_B]
        }

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