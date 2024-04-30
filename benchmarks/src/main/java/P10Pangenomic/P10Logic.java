package P10Pangenomic;



import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class P10Logic {
    MyGraph my_g;

    public void run(MyGraph g) {
        write_families(g);
    }

    public void write_families(MyGraph g) {
        ArrayList<Node> families = g.getNodes("Family");

        for (Node family :
                families) {
            HashMap<String, String> fam_dic_prop = new HashMap<>();
            String name = family.properties.get("name");
            String partition1 = family.properties.get("named_partition");
            String partition2 = family.properties.get("partition");
            String annotation = family.properties.get("annotation");


            String gene = get_genes(g, family);
            String get_family = get_neighbor(g, family);

            fam_dic_prop.put("name",name);
            fam_dic_prop.put("partition1",partition1);
            fam_dic_prop.put("partition2",partition2);
            fam_dic_prop.put("annotation",annotation);
            fam_dic_prop.put("Gene",gene);
            fam_dic_prop.put("Family",get_family);
        }
    }

    public String get_genes(MyGraph g, Node family ) {
        ArrayList<HashMap<String,String>> genes_list = new ArrayList<>();
//
//        Node family = g.getNodes("Family").get(0);
        ArrayList<Node> genes = new ArrayList<>();
        for (Edge e :
                family.getEdges()) {
            if (g.getNode(e.from).label.equals("Gene")) {
                genes.add(g.getNode(e.to));
            }
        }

        for (Node gene :
                genes) {
            String name = gene.properties.get("name");
            String type = gene.properties.get("type");
            boolean is_fragment = Boolean.parseBoolean(gene.properties.get("is_fragment"));
            String start = gene.properties.get("start");
            String stop = gene.properties.get("stop");
            String strand = gene.properties.get("strand");
            String product = gene.properties.get("product");
            String local_id = gene.properties.get("local_identified");
            String tmp_id = gene.properties.get("tmp_id");

            HashMap<String, String> map = new HashMap<>();
            map.put("name", name);
            map.put("type", type);
            map.put("is_fragment", String.valueOf(is_fragment));
            map.put("start", start);
            map.put("stop", stop);
            map.put("strand", strand);
            map.put("product", product);
            map.put("local_id", local_id);
            map.put("tmp_id", tmp_id);
            genes_list.add(map);
        }
        return genes_list.toString();
    }

    public String get_neighbor(MyGraph g, Node family) {
        ArrayList<HashMap<String, String>> neighbors = new ArrayList<>();
        HashSet<String> fam_visit = new HashSet<>();

        for (Edge e :
                family.getEdges()) {
            Node source = g.getNode(e.from);
            Node target = g.getNode(e.to);
            if (source.properties.get("name").equals(family.properties.get("name"))) {
                if (!fam_visit.contains(target.properties.get("name"))) {
                    String weight = e.properties.get("organisms");
                    String name = target.properties.get("name");
                    String partition1 = target.properties.get("named_partition");
                    String partition2 = target.properties.get("partition");

                    String annot = target.properties.get("annotation");


                    HashMap<String, String> neighbor = new HashMap<>();

                    neighbor.put("weight",weight);
                    neighbor.put("name",name);
                    neighbor.put("partition1",partition1);
                    neighbor.put("partition2",partition2);
                    neighbor.put("annotation",annot);
                    neighbors.add(neighbor);
                }
            } else if (target.properties.get("name").equals(family.properties.get("name"))) {
                if (!fam_visit.contains(source.properties.get("name"))) {
                    String weight = e.properties.get("weight");
                    String name = source.properties.get("name");
                    String partition1 = source.properties.get("named_partition");
                    String partition2 = source.properties.get("partition");

                    String annot = source.properties.get("annotation");


                    HashMap<String, String> neighbor = new HashMap<>();

                    neighbor.put("weight",weight);
                    neighbor.put("name", name);
                    neighbor.put("partition1", partition1);
                    neighbor.put("partition2", partition2);
                    neighbor.put("annotation", annot);
                    neighbors.add(neighbor);
                }
            } else {
                System.err.println("Source and target name are different from edge's family. \n Please check you import graph data and if the problem persist, post an issue.");
                return "";
            }
        }
        fam_visit.add(family.properties.get("name"));
        return neighbors.toString();
    }
}