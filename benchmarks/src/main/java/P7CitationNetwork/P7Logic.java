package P7CitationNetwork;


import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class P7Logic {
    MyGraph my_g;


    public void run(MyGraph g) {


    }

        /**
     * Feature request: https://github.com/Citegraph/citegraph/issues/2
     * Given a paper, find its n-hop references, with the following requirements:
     * 1) The "bedrock" paper, defined as the one has most incoming links in this subgraph.
     * 2) 2-hop graph would already be very crazily large, and sometimes user might want even 3-hop. Assuming the
     * "bedrock" paper would have been cited multiple times in this hypothetical subgraph, we could randomly select,
     * say, 10 outgoing links from every node except the original starting point
     * <p>
     * Devnote: why do we not use a single gremlin query to achieve this? Three reasons:
     * 1) I believe it's technically possible to write a single query to achieve the above requirements, but I am not
     * an expert in writing complex gremlin queries
     * 2) Gremlin path representation contains duplicate data when we want a lot of paths that are more or less overlapping.
     * It's okay since our Gremlin server and web app server run on the same machine, but ideally we do want to reduce
     * unnecessary network cost
     * 3) We currently set up a global timeout on Gremlin server. For a task as complicated as this one, I do want to give
     * it more leniency to run longer. By splitting a single complicated giant query into a few smaller ones, we overcome
     * the timeout limitation.
     *
     * @param vid
     * @return
     */

    public int getCitationNetwork(MyGraph g, String vid) {
        PathDTO result = new PathDTO();
        // for each paper, how many times has it been cited in this subgraph?
        Map<String, Integer> citationMap = new HashMap<>();
        // for the starting vertex, let's assign a default value being one
        Node paper = g.getNodes("paper").get(0);
        citationMap.put(vid, 1);

        // step 0: get title of current vertex
        Map<String, String> props = paper.properties;
        result.getVertices().add(new VertexDTO(vid, null, (String) props.get("title"), 0));

        // step 1: find immediate neighbors (1-hop references)
        List<Node> oneHopVertices = paper.getOutgoingEdges().stream().filter(edge -> edge.label.equals("cites")).map(edge -> g.getNode(edge.to)).filter(node -> node.properties.containsKey("title") ).toList();
        for (Node oneHopVertex : oneHopVertices) {
            String id = (String) (oneHopVertex.id + "");
            citationMap.put(id, citationMap.getOrDefault(id, 0) + 1);
            result.getVertices().add(new VertexDTO(id, null, (String) oneHopVertex.properties.get("title"), 1));
            result.getEdges().add(new EdgeDTO(vid, id, "cites"));
        }

        // step 2: find 2-hop references.

        // two-hop vertex id -> one-hop vertex ids mapping
        Map<String, List<String>> twoHopVerticesAndEdges = new HashMap<>();

        for (Node oneHopVertex : oneHopVertices) {
            String id = (String) (oneHopVertex.id + "");
//            List<Object> neighbors = oneHopVertex.getOutgoingEdges()..out("cites").id().toList();
            List<Node> neighbors = oneHopVertex.getOutgoingEdges().stream().filter(edge -> edge.label.equals("cites")).map(edge -> g.getNode(edge.to)).toList();
            for (Object neighbor : neighbors) {
                twoHopVerticesAndEdges.computeIfAbsent(neighbor.toString(), k -> new ArrayList<>()).add(id);
            }
        }

        for (Map.Entry<String, List<String>> entry : twoHopVerticesAndEdges.entrySet()) {
            String twoHopVertexId = entry.getKey();
            // skip two-hop vertices that have only been cited by a single one-hop vertex
            if (entry.getValue().size() <= 1) continue;
            // add edges (from one-hop vertex to two-hop vertex)
            result.getEdges().addAll(entry.getValue().stream()
                    .map(oneHopVertexId -> new EdgeDTO(oneHopVertexId, twoHopVertexId, "cites"))
                    .collect(Collectors.toList()));
            // add two-hop vertex
            if (!citationMap.containsKey(twoHopVertexId)) {
//                Map<Object, Object> vProps = g.V(twoHopVertexId).elementMap("title").next();
                Map<String, String> vProps = g.getNode(Integer.parseInt(twoHopVertexId)).properties;
                result.getVertices().add(new VertexDTO(twoHopVertexId, null, (String) vProps.get("title"), 2));
            }
            citationMap.put(twoHopVertexId, citationMap.getOrDefault(twoHopVertexId, 0) + entry.getValue().size());
        }

        int maxCitations = citationMap.values().stream().max(Integer::compare).get();
        // Generate a "local" pagerank in this subgraph. Rather than use the standard pagerank algorithm to compute,
        // we do a simple scaling to make sure the number is between 0 and 1.
        result.getVertices().forEach(vertexDTO -> {
            if (vertexDTO.getId() != vid) {
                vertexDTO.setPagerank(((double) citationMap.get(vertexDTO.getId())) / maxCitations);
            } else {
                vertexDTO.setPagerank(1);
            }
        });

        return 1;
    }
}