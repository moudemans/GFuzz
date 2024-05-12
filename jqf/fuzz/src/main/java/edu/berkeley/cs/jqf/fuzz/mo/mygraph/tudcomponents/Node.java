package edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Node implements Serializable {

    public int id;
    public String label;

    public HashMap<String, String> properties = new HashMap<>();

    Set<Edge> edges = new HashSet<>();

    public Node(int id) {
        this.id = id;
    }

    public Node(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public Node(Node currNode) {
        this.id = currNode.id;
        this.label = currNode.label;
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
            if (e.label.equals(label)) {
                if(found) {
                    return false;
                }
                found = true;
            }
        }
        return res;
    }


}
