package Components;

import java.io.Serializable;
import java.util.*;

public class Node implements Serializable {

    public int id;
    public String label;

    public HashMap<String, String> properties = new HashMap<>();

    Set<Edge> edges = new HashSet<>();

    public Node(int id) {
        this.id = id;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public String outgoingEdgesText() {
        ArrayList<Integer> connectedTo = new ArrayList<>();

        for(Edge e : edges) {
            connectedTo.add(e.to.id);
        }
        Arrays.sort(connectedTo.toArray());

        String res = "";
        for (int i :
                connectedTo) {
            res += i + ", ";

        }
        return res;
    }

    public int getID() {
        return id;
    }

    public Set<Edge> getEdges() {
        return edges;
    }
}
