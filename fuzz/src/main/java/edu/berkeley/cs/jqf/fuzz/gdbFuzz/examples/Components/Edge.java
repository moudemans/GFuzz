package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components;

import java.io.Serializable;
import java.util.HashMap;

public class Edge implements Serializable {

    public Node from;
    public Node to;
    boolean isDirected = false;
    public int weight = 0;
    String label;

    public HashMap<String, String> properties;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
    }
}
