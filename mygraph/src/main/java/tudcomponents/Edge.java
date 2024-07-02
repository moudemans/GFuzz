package tudcomponents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Edge implements Serializable {

    public ArrayList<String> labels = new ArrayList<>();
    public int from;
    public int to;
    //    boolean isDirected = false;
    public int weight = 0;

    public HashMap<String, String> properties = new HashMap<>();
    public HashMap<String, Type> propertyTypes = new HashMap<>();


    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public Edge(String label, int from, int to) {
        this.labels.add(label);
        this.from = from;
        this.to = to;
    }
    public Edge(ArrayList<String> labels, int from, int to) {
        this.labels = labels;
        this.from = from;
        this.to = to;
    }

    public Edge(Node from, Node to) {
        this.from = from.id;
        this.to = to.id;
    }

    public Edge(Relationship rel, Node c_from, Node c_to) {
        if (!c_from.labels.contains(rel.from)) {
            this.to = c_from.id;
            this.from = c_to.id;
        } else {
            this.to = c_to.id;
            this.from = c_from.id;
        }
        this.labels.add(rel.label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return from == edge.from && to == edge.to && Objects.equals(labels, edge.labels);
    }

    public Type getPropertyType(String label) {
        if (propertyTypes != null) {
            return propertyTypes.getOrDefault(label, null);
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, from, to);
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
