package edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P1;



import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.Edge;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.MyGraph;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.Node;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class P1Cycle {
    public void run(String inputFile) throws IOException {
        MyGraph g = MyGraph.readGraphFromFile(inputFile);
        findCycle(g);
    }


    public boolean findCycle(MyGraph g) {
        ArrayList<Node> nodes = g.getNodes();

        Stack<Walker> walkers = new Stack<>();
        Random r = new Random();

        HashSet<Integer> nodesNotReached = new HashSet<>();
        for (Node n :
                nodes) {
            nodesNotReached.add(n.getID());
        }

        while (!nodesNotReached.isEmpty()) {
            if (walkers.empty()) {
                int newStart = r.nextInt(nodesNotReached.size());

                int newStartID = nodesNotReached.stream().collect(Collectors.toList()).get(newStart);
                walkers.push(new Walker(newStartID));
            }

            Walker currentWalker = walkers.pop();
            Node currentNode = nodes.get(currentWalker.currentLocation);
            Set<Edge> outgoingEdges = currentNode.getEdges();
            nodesNotReached.remove(currentWalker.currentLocation);

            Set<Integer> toNodeIds = new HashSet<>();
            for (Edge e : outgoingEdges) {
                Walker nWalker = new Walker(currentWalker);
                nWalker.getVisited().add(nWalker.currentLocation);
                nWalker.getPath().add(nWalker.currentLocation);

                if (toNodeIds.contains(e.to.id)) {
                    continue;
                }

                if (nWalker.getVisited().contains(e.to.id)) {
                    int beginCycleIndex = nWalker.getPath().indexOf(e.to.id);
                    List<Integer> cyclePath = nWalker.getPath().subList(beginCycleIndex, nWalker.getPath().size());
                    if (cyclePath.size() > 15) {
                        System.out.println("Found cycle of size " + cyclePath.size() + ". Path: " + cyclePath.toString() + " --> " + e.to.id + " ******* full path: " + nWalker.getPath());
                        return true;
                    }
                    continue;

                }
                nWalker.makeStep(e.to.id);
                toNodeIds.add(e.to.id);
                walkers.push(nWalker);
            }


        }
        return false;

    }
}




