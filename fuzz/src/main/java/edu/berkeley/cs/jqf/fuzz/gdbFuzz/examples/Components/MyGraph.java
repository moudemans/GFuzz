package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MyGraph implements Serializable {

    ArrayList<Node> nodes = new ArrayList<>();
    Random r = new Random();

    public MyGraph() {
    }

    public int nodeCount() {
        return nodes.size();
    }

    public void generateRandomSimpleGraph(int nodeCount, int edgeCount) {
        emptyGraph();

        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node(nodes.size()));
        }

        for (int i = 0; i < edgeCount; i++) {
            int fromId = r.nextInt(nodes.size());
            int toId = r.nextInt(nodes.size());

            nodes.get(fromId).addEdge(new Edge(nodes.get(fromId), nodes.get(toId)));

        }
    }


    public static MyGraph readGraphFromFile(String fileName) {
        MyGraph mg = null;
        try {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            mg = (MyGraph) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
            System.exit(-1);
            return null;
        }
        return mg;
    }

    public static void writeGraphToFile(String fileName, MyGraph g) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(g);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void generateRandomWeightedGraph(int nodeCount, int edgeCount, int maxWeight, boolean allowNegative) {
        emptyGraph();

        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new Node(nodes.size()));
        }

        for (int i = 0; i < edgeCount; i++) {
            int fromId = r.nextInt(nodes.size());
            int toId = r.nextInt(nodes.size());
            Edge e = new Edge(nodes.get(fromId), nodes.get(toId));
            e.weight = r.nextInt(maxWeight);
            e.weight = allowNegative && r.nextBoolean() ? e.weight * -1: e.weight ;
            nodes.get(fromId).addEdge(e);

        }
    }

    private void emptyGraph() {
        nodes = new ArrayList<>();
    }

    @Override
    public String toString() {
        String res = "";

        for (Node n :
                nodes) {
            res += "n" + n.id + ", (" + n.outgoingEdgesText() + ")\n" ;
        }
        return  res;
    }

    public void insertCycle(int size) {
        assert nodes.size() > size;

        int sourceNode = r.nextInt(nodes.size());
        Set<Integer> candidates = new HashSet<>();
        candidates.add(sourceNode);

        while (candidates.size() < size) {
            int nextNode = r.nextInt(nodes.size());
            if (!candidates.contains(nextNode)) {
                candidates.add(nextNode);
            }
        }

        int[] cycle = candidates.stream().mapToInt(value -> value).toArray();

        for (int i = 0; i < cycle.length; i++) {
            Node from = nodes.get(cycle[i]);
            int toIndex = i+1 >= cycle.length ? 0 : cycle[i+1];
            Node to = nodes.get(toIndex);
            from.addEdge(new Edge(from, to));
        }

    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void generateRandomLabeledGraph(int i, int i1) {
        emptyGraph();
        generateRandomSimpleGraph(i, i1);

        for (int j = 0; j < 20; j++) {
            Node n = nodes.get(0);
            String k = "cat" + r.nextInt(5);
            String v = "val" + r.nextInt(100);
            n.properties.put(k,v);

        }


    }
}
