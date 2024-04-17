package tudcomponents;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class Edge implements Serializable {

    public String label;
    public int from;
    public int to;
//    boolean isDirected = false;
    public int weight = 0;

    public HashMap<String, String> properties = new HashMap<>();

    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public Edge(String label, int from, int to) {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public Edge(Node from, Node to) {
        this.from = from.id ;
        this.to = to.id;
    }

    public Edge(Relationship rel, Node c_from, Node c_to) {
        if (!rel.from.equals(c_from.label)) {
            this.to = c_from.id;
            this.from = c_to.id;
        } else {
            this.to = c_to.id;
            this.from = c_from.id;
        }
        this.label = rel.label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return from == edge.from && to == edge.to && Objects.equals(label, edge.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, from, to);
    }
}
