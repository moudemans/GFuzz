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
//        int paper_id = g.getNodes("paper").get(0).id;
//        int author_id = g.getNodes("author").get(0).id;
//
//        getPaper(g, paper_id + "");
//        getAuthor(g, author_id + "");
        getPaper(g, "5");
        getAuthor(g, "70");

    }


    public int getPaper(MyGraph g, String id) {
        if (g.getNode(Integer.parseInt(id)) == null ) {
            return -1;
        }

        Node paper = g.getNode(Integer.parseInt(id));
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

    public int getAuthor(MyGraph g, String id) {
        if (g.getNode(Integer.parseInt(id)) == null ) {
            return -1;
        }


        Node author = g.getNode(Integer.parseInt(id));
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
            Node referee_node = g.getNode(Integer.parseInt(refereeId));
            String refereeName = (String) referee_node.properties.get("name");
            double refereePagerank = Double.parseDouble(referee_node.properties.get("pagerank"));
            CitationResponse citationResponse = new CitationResponse(new AuthorResponse(refereeName, refereeId, refereePagerank), refCount);
            refereeResponse.add(citationResponse);
        }

        List<Edge> referers = author.getIncomingEdges().stream().filter(edge -> edge.label.equals("refers")).toList();
//        List<Edge> referers = g.V(author).inE("refers").limit(limit).toList();
        List<CitationResponse> refererResponse = new ArrayList<>(referers.size());
        for (Edge referer : referers) {
            int refCount =  Integer.parseInt(referer.properties.getOrDefault("refCount", "1"));

            String refererId =  (String) (referer.from + "");
            Node referer_node = g.getNode(Integer.parseInt(refererId));

            String refererName =  (String) referer_node.properties.get("name");
            double refererPagerank =  Double.parseDouble(referer_node.properties.get("pagerank"));
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
            Node co_author = g.getNode(Integer.parseInt(coauthorId));

            String coauthorName = (String) co_author.properties.get("name");
            double coauthorPagerank = Double.parseDouble(co_author.properties.get("pagerank"));
            CollaborationResponse collaborationResponse = new CollaborationResponse(new AuthorResponse(coauthorName, coauthorId, coauthorPagerank), collaborationCount);
            coauthorResponse.add(collaborationResponse);
        }

        res.setReferees(refereeResponse);
        res.setReferers(refererResponse);
        res.setCoauthors(coauthorResponse);


        return 1;
    }

}