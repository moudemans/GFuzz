package P6PhenoOut;


import tudcomponents.*;

import java.util.ArrayList;
import java.util.List;

public class P6Logic {
    MyGraph my_g;


    public void run(MyGraph g) {
        getHeaderForCsvFile(g, g.getNodes().getFirst());
    }

    static String id_label = "id_label";
    static String start_label = "start_label";
    static String end_label = "end_label";

    public String[] getHeaderForCsvFile(MyGraph g, Node n) {
        List<String> fields = new ArrayList<>();
        String node_label = n.label;
        for (String key : n.properties.keySet()) {
            final String value;
            if (key.equals(id_label)) {
                String annotation = n.properties.get(key);
                value = "test1" + annotation;
            } else if (key.equals(start_label)) {
                String annotation = n.properties.get(key);
                value = "test2" + annotation;
            } else if (key.equals(end_label)) {
                String annotation = n.properties.get(key);
                value = "test3" + annotation;
            } else {
                value = addTypeToField(g, n, key);
            }
            fields.add(value);
        }

        return fields.toArray(new String[0]);
    }

    private String addTypeToField(MyGraph g, Node n, String property_label) {
        String value;
//        Property p = g.getSchema().getNodeProperties().get(node_label).stream().filter(property -> property.name.equals(property_label)).findFirst().orElse(null);
        Type t = g.getNodeProperty(n, property_label).type;

        if (t == Type.INT) {
            value = "int";
        } else if (t == Type.DOUBLE) {
            value = "double";
        } else if (t == Type.BOOLEAN) {

            value = "boolean";
        } else if (t == Type.STRING) {
            value = "string";
        } else {
            value = "ERROR";
        }
        return value;

    }
}