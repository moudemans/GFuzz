package tudcomponents;

import java.io.Serializable;

public class Relationship implements Serializable {
    String label, from, to;
    Cardinality cardinality = Cardinality.MULTI;

    public Relationship(String label, String from, String to, Cardinality c) {
        this.label = label;
        this.from = from;
        this.to = to;
        this.cardinality = c;
    }

    public Relationship() {
    }

    public String getLabel() {
        return label;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
