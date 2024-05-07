package P3Citation;


import tudcomponents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class P3Logic {
    MyGraph my_g;


    public void run(MyGraph g) {


    }

    public int getPaper(MyGraph g, String id, int limit) {
        if (g.getNodes(id) == null || g.getNodes(id).isEmpty()) {
            return -1;
        }

        Node paper = g.getNodes(id).getFirst();
        HashMap<String, String> paperProps = paper.properties;
        if (!paperProps.containsKey("title")) {
            return -1;
        }
        PaperResponse paperResponse = new PaperResponse(
                id,
                (String) paperProps.get("title"),
                Integer.parseInt(paperProps.getOrDefault("year", "0")));

        List<Node> authorIds = g.getConnectedNodes(paper, "writes");

        List<AuthorResponse> authors = IntStream.range(0, authorIds.size())
                .mapToObj(index -> {
                    Node vid = authorIds.get(index);
                    Map<String, String> props = vid.properties;
                    return new AuthorResponse(
                            (String) props.getOrDefault("name", "unknown"),
                            (String) (vid.id + ""),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferers", "0")),
                            Double.parseDouble(props.getOrDefault("pagerank", "0.0")),
                            index + 1);
                })
                .collect(Collectors.toList());
        paperResponse.setAuthors(authors);


        // collect pagerank
        double pagerank = Double.parseDouble(paperProps.getOrDefault("pagerank", "0.0"));
        // collect paper citations
        int numOfReferees = Integer.parseInt(paperProps.getOrDefault("numOfPaperReferees", "0"));
        int numOfReferers = Integer.parseInt(paperProps.getOrDefault("numOfPaperReferers", "0"));
        paperResponse.setNumOfReferees(numOfReferees);
        paperResponse.setNumOfReferers(numOfReferers);
        paperResponse.setPagerank(pagerank);
        paperResponse.setVenue((String) paperProps.get("venue"));
        paperResponse.setKeywords((String) paperProps.get("keywords"));
        paperResponse.setField((String) paperProps.get("field"));
        paperResponse.setDocType((String) paperProps.get("docType"));
        paperResponse.setVolume((String) paperProps.get("volume"));
        paperResponse.setIssue((String) paperProps.get("issue"));
        paperResponse.setIssn((String) paperProps.get("issn"));
        paperResponse.setIsbn((String) paperProps.get("isbn"));
        paperResponse.setDoi((String) paperProps.get("doi"));
        paperResponse.setPaperAbstract((String) paperProps.get("abstract"));

        List<PaperResponse> referees = g.getConnectedNodes(paper, "cites", false)
                .stream()
                .map(v -> {
                    Map<String, String> props = v.properties;

                    return new PaperResponse(
                            (String) (v.id + ""),
                            (String) props.getOrDefault("title", "N/A"),
                            Integer.parseInt(props.getOrDefault("year", "N/A")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferees", "0")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferers", "0")),
                            Double.parseDouble(props.getOrDefault("pagerank", "0.0")));
                })
                .collect(Collectors.toList());
        paperResponse.setReferees(referees);

        List<PaperResponse> referers = g.getConnectedNodes(paper, "cites", true)
                .stream()
                .map(v -> {
                    Map<String, String> props = v.properties;
                    return new PaperResponse(
                            (String) (v.id + ""),
                            (String) props.getOrDefault("title", "N/A"),
                            Integer.parseInt(props.getOrDefault("year", "N/A")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferees", "0")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferers", "0")),
                            Double.parseDouble(props.getOrDefault("pagerank", "0.0")));
                })
                .collect(Collectors.toList());
        paperResponse.setReferers(referers);


        return 1;
    }

    public int getAuthor(MyGraph g, String id, int limit, boolean getEdges) {
        if (g.getNodes(id) == null || g.getNodes(id).isEmpty()) {
            return -1;
        }

        Node paper = g.getNodes(id).getFirst();
        HashMap<String, String> paperProps = paper.properties;
        if (!paperProps.containsKey("title")) {
            return -1;
        }

        Node author = g.getNodes(id).getFirst();
//        Vertex author = g.V().hasId(id).next();
        Map<String, String> authorProps = author.properties;
        if (!authorProps.containsKey("name")) {
            return -1;
        }
        String name = (String) authorProps.get("name");


        // collect pagerank
        double pagerank = Double.parseDouble(authorProps.getOrDefault("pagerank", "0.0"));
        // collect papers
        int numOfPapers = Integer.parseInt(authorProps.getOrDefault("numOfPapers", "0"));

        // collect author references
        int numOfReferees = Integer.parseInt(authorProps.getOrDefault("numOfAuthorReferees", "0"));
        int numOfReferers = Integer.parseInt(authorProps.getOrDefault("numOfAuthorReferers", "0"));
        // collect paper references
        int numOfPaperReferees = Integer.parseInt(authorProps.getOrDefault("numOfPaperReferees", "0"));
        int numOfPaperReferers = Integer.parseInt(authorProps.getOrDefault("numOfPaperReferers", "0"));

        int numOfCoauthors = Integer.parseInt(authorProps.getOrDefault("numOfCoworkers", "0"));

        String org = (String) authorProps.getOrDefault("org", "");

        AuthorResponse res = new AuthorResponse(name, id, org, numOfPapers, numOfReferees, numOfReferers,
                numOfPaperReferees, numOfPaperReferers, numOfCoauthors, pagerank);

        List<PaperResponse> papers = g.getConnectedNodes(author, "writes")
                .stream()
                .map(v -> {
                    Map<String, String> props = v.properties;
                    return new PaperResponse((String) (v.id + ""),
                            (String) props.getOrDefault("title", "N/A"),
                            Integer.parseInt(props.getOrDefault("year", "0")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferees", "0")),
                            Integer.parseInt(props.getOrDefault("numOfPaperReferers", "0")),
                            Integer.parseInt(props.getOrDefault("pagerank", "0.0"))
                    );
                })
                .collect(Collectors.toList());
        res.setPapers(papers);



        List<Edge> referees = author.getOutgoingEdges().stream().filter(edge -> edge.label.equals("refers")).toList();
        List<CitationResponse> refereeResponse = new ArrayList<>(referees.size());
        for (Edge referee : referees) {
            int refCount = Integer.parseInt(referee.properties.get("refCount"));
            String refereeId = (String) (referee.from + "");
            String refereeName = (String) referee.properties.get("name");
            double refereePagerank = Double.parseDouble(referee.properties.get("pagerank"));
            CitationResponse citationResponse = new CitationResponse(new AuthorResponse(refereeName, refereeId, refereePagerank), refCount);
            refereeResponse.add(citationResponse);
        }

        List<Edge> referers = author.getIncomingEdges().stream().filter(edge -> edge.label.equals("refers")).toList();
//        List<Edge> referers = g.V(author).inE("refers").limit(limit).toList();
        List<CitationResponse> refererResponse = new ArrayList<>(referers.size());
        for (Edge referer : referers) {
            int refCount =  Integer.parseInt(referer.properties.getOrDefault("refCount", "1"));
            String refererId =  (String) (referer.from + "");
            String refererName =  (String) referer.properties.get("name");
            double refererPagerank =  Double.parseDouble(referer.properties.get("pagerank"));
            CitationResponse citationResponse = new CitationResponse(new AuthorResponse(refererName, refererId, refererPagerank), refCount);
            refererResponse.add(citationResponse);
        }

        List<Edge> coauthors = author.getEdges().stream().filter(edge -> edge.label.equals("collaborates")).toList();
//        List<Edge> coauthors = g.V(author).bothE("collaborates").limit(limit).toList();
        List<CollaborationResponse> coauthorResponse = new ArrayList<>(coauthors.size());
        for (Edge edge : coauthors) {
            int collaborationCount = Integer.parseInt(edge.properties.getOrDefault("collaborateCount", "1"));
            Node otherVertex = edge.to == author.id ? g.getNode(edge.from) : g.getNode(edge.to);
            String coauthorId = (String) (otherVertex.id + "");
            String coauthorName = (String) edge.properties.get("name");
            double coauthorPagerank = Double.parseDouble(edge.properties.get("pagerank"));
            CollaborationResponse collaborationResponse = new CollaborationResponse(new AuthorResponse(coauthorName, coauthorId, coauthorPagerank), collaborationCount);
            coauthorResponse.add(collaborationResponse);
        }

        res.setReferees(refereeResponse);
        res.setReferers(refererResponse);
        res.setCoauthors(coauthorResponse);


        return 1;
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
//    @GetMapping("/graph/citations/{vid}")
//    public PathDTO getCitationNetwork(@PathVariable String vid) {
//        PathDTO result = new PathDTO();
//        // for each paper, how many times has it been cited in this subgraph?
//        Map<String, Integer> citationMap = new HashMap<>();
//        // for the starting vertex, let's assign a default value being one
//        citationMap.put(vid, 1);
//
//        // step 0: get title of current vertex
//        Map<Object, Object> props = g.V(vid).elementMap("title").next();
//        result.getVertices().add(new VertexDTO(vid, null, (String) props.get("title"), 0));
//
//        // step 1: find immediate neighbors (1-hop references)
//        List<Map<Object, Object>> oneHopVertices = g.V(vid).out("cites").elementMap("title").toList();
//        for (Map<Object, Object> oneHopVertex : oneHopVertices) {
//            String id = (String) oneHopVertex.get(T.id);
//            citationMap.put(id, citationMap.getOrDefault(id, 0) + 1);
//            result.getVertices().add(new VertexDTO(id, null, (String) oneHopVertex.get("title"), 1));
//            result.getEdges().add(new EdgeDTO(vid, id, "cites"));
//        }
//
//        // step 2: find 2-hop references.
//
//        // two-hop vertex id -> one-hop vertex ids mapping
//        Map<String, List<String>> twoHopVerticesAndEdges = new HashMap<>();
//
//        for (Map<Object, Object> oneHopVertex : oneHopVertices) {
//            String id = (String) oneHopVertex.get(T.id);
//            List<Object> neighbors = g.V(id).out("cites").id().toList();
//            for (Object neighbor : neighbors) {
//                twoHopVerticesAndEdges.computeIfAbsent(neighbor.toString(), k -> new ArrayList<>()).add(id);
//            }
//        }
//
//        for (Map.Entry<String, List<String>> entry : twoHopVerticesAndEdges.entrySet()) {
//            String twoHopVertexId = entry.getKey();
//            // skip two-hop vertices that have only been cited by a single one-hop vertex
//            if (entry.getValue().size() <= 1) continue;
//            // add edges (from one-hop vertex to two-hop vertex)
//            result.getEdges().addAll(entry.getValue().stream()
//                    .map(oneHopVertexId -> new EdgeDTO(oneHopVertexId, twoHopVertexId, "cites"))
//                    .collect(Collectors.toList()));
//            // add two-hop vertex
//            if (!citationMap.containsKey(twoHopVertexId)) {
//                Map<Object, Object> vProps = g.V(twoHopVertexId).elementMap("title").next();
//                result.getVertices().add(new VertexDTO(twoHopVertexId, null, (String) vProps.get("title"), 2));
//            }
//            citationMap.put(twoHopVertexId, citationMap.getOrDefault(twoHopVertexId, 0) + entry.getValue().size());
//        }
//
//        int maxCitations = citationMap.values().stream().max(Integer::compare).get();
//        // Generate a "local" pagerank in this subgraph. Rather than use the standard pagerank algorithm to compute,
//        // we do a simple scaling to make sure the number is between 0 and 1.
//        result.getVertices().forEach(vertexDTO -> {
//            if (vertexDTO.getId() != vid) {
//                vertexDTO.setPagerank(((double) citationMap.get(vertexDTO.getId())) / maxCitations);
//            } else {
//                vertexDTO.setPagerank(1);
//            }
//        });
//
//        return result;
//    }
//
//    @GetMapping("/graph/vertex/{vid}")
//    public Map<String, Object> getVertexById(@PathVariable String vid, @RequestParam int limit, @RequestParam boolean getEdges) {
//        try {
//
//            if (getEdges) {
//                return g.V(vid)
//                        .project("self", "neighbors")
//                        .by(__.elementMap())
//                        .by(
//                                __.union(
//                                                __.bothE("collaborates").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap()),
//                                                __.outE("cites").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap()),
//                                                __.inE("cites").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap()),
//                                                __.outE("refers").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap()),
//                                                __.inE("refers").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap()),
//                                                __.bothE("writes").limit(limit).as("edge")
//                                                        .otherV().as("vertex")
//                                                        .select("edge", "vertex")
//                                                        .by(__.elementMap())
//                                        )
//                                        .fold()
//                        ).next();
//            } else {
//                return g.V(vid)
//                        .project("self")
//                        .by(__.elementMap())
//                        .next();
//            }
//        } catch (NoSuchElementException ex) {
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND, String.format("Vertex (ID: %s) not found", vid));
//        }
//    }




}