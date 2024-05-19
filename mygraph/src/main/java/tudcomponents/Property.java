package tudcomponents;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.text.DateFormat;

public class Property implements Serializable {
    public String name;
    public Type type;
    public boolean isUnique;
    public boolean isNotNull;
    public boolean valueIsConstraint;
    public int min;
    public int max;


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

    public Property(String name, Type type, boolean isUnique, boolean isNotNull, boolean valueIsConstraint, int min, int max) {
        this.name = name;
        this.type = type;
        this.isUnique = isUnique;
        this.isNotNull = isNotNull;
        this.valueIsConstraint = valueIsConstraint;
        this.min = min;
        this.max = max;
    }

    public Property(String name, Type type, boolean valueIsConstraint, int min, int max) {
        this.name = name;
        this.type = type;

        this.valueIsConstraint = valueIsConstraint;
        this.min = min;
        this.max = max;
    }



    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}

