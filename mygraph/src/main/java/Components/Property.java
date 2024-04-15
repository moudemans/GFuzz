package Components;

import java.io.Serializable;

public class Property implements Serializable {
    public String name;
    public Type type;
    public boolean isUnique;
    public boolean isNotNull;


    public Property(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Property(String name, Type type, boolean isUnique) {
        this.name = name;
        this.type = type;
        this.isUnique = isUnique;
    }

    public Property(String name, Type type, boolean isUnique, boolean isNotNull) {
        this.name = name;
        this.type = type;
        this.isUnique = isUnique;
        this.isNotNull = isNotNull;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

