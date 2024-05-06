package P7CitationNetwork;

import java.util.ArrayList;
import java.util.List;

public class PathDTO {
    private List<VertexDTO> vertices = new ArrayList<>();
    private List<EdgeDTO> edges = new ArrayList<>();

    public PathDTO() {

    }


    public List<VertexDTO> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexDTO> vertices) {
        this.vertices = vertices;
    }

    public List<EdgeDTO> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeDTO> edges) {
        this.edges = edges;
    }
}