package tudcomponents;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements Serializable {

    public int id;
    public ArrayList<String> labels = new ArrayList<>();

    public HashMap<String, String> properties = new HashMap<>();
    public HashMap<String, Type> propertyTypes = new HashMap<>();

    Set<Edge> edges = new HashSet<>();

    public Node(int id) {
        this.id = id;
    }

    public Node(int id, String label) {
        this.id = id;
        this.labels.add(label);
    }


    public Node(int id, ArrayList<String> labels) {
        this.id = id;
        this.labels = labels;
    }

    public Node(Node currNode) {
        this.id = currNode.id;
        this.labels = currNode.labels;
        this.properties = new HashMap<>(currNode.properties);
        this.edges = new HashSet<>(currNode.edges);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public String outgoingEdgesText() {
        ArrayList<Integer> connectedTo = new ArrayList<>();

        for (Edge e : edges) {
            connectedTo.add(e.to);
        }
        Arrays.sort(connectedTo.toArray());

        StringBuilder res = new StringBuilder();
        for (int i :
                connectedTo) {
            res.append(i).append(", ");

        }
        return res.toString();
    }

    public int getID() {
        return id;
    }

    public Set<Edge> getOutgoingEdges() {
        return edges.stream().filter(edge -> edge.from == id).collect(Collectors.toSet());
    }

    public Set<Edge> getIncomingEdges() {
        return edges.stream().filter(edge -> edge.to == id).collect(Collectors.toSet());
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    public String getProperty(String address) {
        return properties.get(address);
    }

    public boolean isSingleRelationship(String label, boolean isIncomming) {
        boolean res = true;
        boolean found = false;
        Set<Edge> l_edges = isIncomming ? getIncomingEdges() : getOutgoingEdges();
        for (Edge e : l_edges) {
            if (e.labels.equals(label)) {
                if(found) {
                    return false;
                }
                found = true;
            }
        }
        return res;
    }

    public Type getPropertyType(String label) {
        if (propertyTypes != null) {
            return propertyTypes.getOrDefault(label, null);
        }
        return null;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }
    public void addLabel(String label) {
        this.labels.add(label);
    }

    public String getLabel(int index) {
        return labels.get(index);
    }


    public String getLabel() {
        return labels.getFirst();
    }
}
