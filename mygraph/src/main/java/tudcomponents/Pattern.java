package tudcomponents;

import java.io.Serializable;
import java.util.Set;

public class Pattern implements Serializable {

    Set<Relationship> relationships;
    int maxSize;

    public Pattern(Set<Relationship> relationships) {
        this.relationships = relationships;
    }

    public Pattern(Set<Relationship> relationships, int maxSize) {
        this.relationships = relationships;
        this.maxSize = maxSize;
    }

    public Set<Relationship> getRelationships() {
        return relationships;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
