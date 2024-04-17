package tudcomponents;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class GraphSchema implements Serializable {
    private Set<String> nodeLabels = new HashSet<>();
    private Set<String> edgeLabels = new HashSet<>();
    private Set<Relationship> relationships = new HashSet<>();
    private Map<String, ArrayList<Property>> nodeProperties = new HashMap<>(); // Map containing the allowed properties for nodes where the key corresponds with the node label
    private Map<String, ArrayList<Property>> edgeProperties = new HashMap<>();

    private Set<Pattern> patterns = new HashSet<>();


    public GraphSchema() {
    }

    public GraphSchema(Set<String> nodeLabels, Set<String> edgeLabels, Set<Relationship> relationships, Map<String, ArrayList<Property>> nodeProperties, Map<String, ArrayList<Property>> edgeProperties) {
        this.nodeLabels = nodeLabels;
        this.edgeLabels = edgeLabels;
        this.relationships = relationships;
        this.nodeProperties = nodeProperties;
        this.edgeProperties = edgeProperties;
    }

    public GraphSchema(Set<String> nodeLabels, Set<String> edgeLabels, Set<Relationship> relationships, Map<String, ArrayList<Property>> nodeProperties, Map<String, ArrayList<Property>> edgeProperties, Set<Pattern> patterns) {
        this.nodeLabels = nodeLabels;
        this.edgeLabels = edgeLabels;
        this.relationships = relationships;
        this.nodeProperties = nodeProperties;
        this.edgeProperties = edgeProperties;
        this.patterns = patterns;
    }

    public static GraphSchema readFromFile(String path) {
        Gson gson = createGSon();

        try {
            return gson.fromJson(new FileReader(path), GraphSchema.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Gson createGSon() {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();
    }

    public static GraphSchema dummySchema() {
        HashSet<String> nodes = new HashSet<>();
        HashSet<String> edges = new HashSet<>();
        HashSet<Relationship> relationships = new HashSet<>();
        HashMap<String, ArrayList<Property>> nProperties = new HashMap<>();
        HashMap<String, ArrayList<Property>> eProperties = new HashMap<>();

        nodes.add("User");
        nodes.add("Product");
        edges.add("OWNS");

        relationships.add(new Relationship("OWNS", "User", "Product", Cardinality.MULTI));

        ArrayList<Property> userProperties = new ArrayList<>();
        userProperties.add(new Property("name", Type.STRING));
        userProperties.add(new Property("adult", Type.BOOLEAN));

        nProperties.put("User", userProperties);


        ArrayList<Property> productProperties = new ArrayList<>();
        productProperties.add(new Property("name", Type.STRING));
        productProperties.add(new Property("price", Type.INT));

        nProperties.put("Product", productProperties);


        ArrayList<Property> ownsProperties = new ArrayList<>();
        ownsProperties.add(new Property("active", Type.BOOLEAN));
        ownsProperties.add(new Property("test", null));

        eProperties.put("OWNS", ownsProperties);

        Pattern p = new Pattern(relationships, 2);

        Set<Pattern> patterns1 = new HashSet<>();
        patterns1.add(p);

        return new GraphSchema(nodes, edges, relationships, nProperties, eProperties, patterns1);
    }

    public void toJSONFile(String path) {
        Gson gson = createGSon();

        try {
            FileWriter fw = new FileWriter(path);
            gson.toJson(this, fw);

            // Flush is needed as there are occurrences where not everything was written to the file
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        GraphSchema gs = dummySchema();
        gs.toJSONFile("mygraph/src/main/resources/graphSchemas/graphschema.json");
        Gson gson = createGSon();


        System.out.println(gson.toJson(gs));

        GraphSchema gs2 = GraphSchema.readFromFile("mygraph/src/main/resources/graphSchemas/graphschema.json");


        String json = gson.toJson(gs2);


        System.out.println(json);

    }

    public HashMap<String, ArrayList<Property>> getUniqueNodeProperties() {
        ArrayList<String> propertyKeys = new ArrayList<>(nodeProperties.keySet());

        HashMap<String, ArrayList<Property>> res = new HashMap<>();
        for (String s : propertyKeys) {
            ArrayList<Property> properties = nodeProperties.get(s);
            ArrayList<Property> uniqueProps = new ArrayList<>();
            for (Property p : properties) {
                if (p.isUnique) {
                    uniqueProps.add(p);
                }
            }
            if (!uniqueProps.isEmpty()) {
                res.put(s, uniqueProps);
            }
        }
        return res;
    }

    public HashMap<String, ArrayList<Property>> getNonNullNodeProperties() {
        ArrayList<String> propertyKeys = new ArrayList<>(nodeProperties.keySet());

        HashMap<String, ArrayList<Property>> res = new HashMap<>();
        for (String s : propertyKeys) {
            ArrayList<Property> properties = nodeProperties.get(s);
            ArrayList<Property> uniqueProps = new ArrayList<>();
            for (Property p : properties) {
                if (p.isNotNull) {
                    uniqueProps.add(p);
                }
            }
            if (!uniqueProps.isEmpty()) {
                res.put(s, uniqueProps);
            }
        }
        return res;
    }

    public Set<String> getNodeLabels() {
        return nodeLabels;
    }

    public Set<String> getEdgeLabels() {
        return edgeLabels;
    }

    public Map<String, ArrayList<Property>> getNodeProperties() {
        return nodeProperties;
    }

    public Map<String, ArrayList<Property>> getEdgeProperties() {
        return edgeProperties;
    }

    public Set<Relationship> getRelationships() {
        return relationships;
    }

    public Set<Pattern> getPatterns() {
        return patterns;
    }
}
