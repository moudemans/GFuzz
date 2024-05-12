package A1Cycle;



import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.Edge;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.mo.mygraph.tudcomponents.Node;

import java.util.*;

public class A1Logic {

    public void run(MyGraph g) {
     // Cyccle
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

                int newStartID = nodesNotReached.stream().toList().get(newStart);
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

                if (toNodeIds.contains(e.to)) {
                    continue;
                }

                if (nWalker.getVisited().contains(e.to)) {
                    int beginCycleIndex = nWalker.getPath().indexOf(e.to);
                    List<Integer> cyclePath = nWalker.getPath().subList(beginCycleIndex, nWalker.getPath().size());
                    if (cyclePath.size() > 10) {
                        System.out.println("Found cycle of size " + cyclePath.size() + ". Path: " + cyclePath.toString() + " --> " + e.to + " ******* full path: " + nWalker.getPath());
                        return true;
                    }
                    continue;

                }
                nWalker.makeStep(e.to);
                toNodeIds.add(e.to);
                walkers.push(nWalker);
            }


        }
        return false;

    }


}