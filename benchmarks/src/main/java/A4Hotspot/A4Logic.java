package A4Hotspot;


import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.*;

public class A4Logic {

    private final int graphHotpointThreshold = 10;
    private final int pathLength = 4;

    HashMap<Node, Boolean> hotpoints = new HashMap<>();

    public void run(MyGraph g) {
        recalculateHotpoints(g, g.getNodes());
    }




    private void recalculateHotpoints(MyGraph g, ArrayList<Node> verts) {

        List<Node> changedHotPoints = new LinkedList<>();

        MyGraph graphHotpoints = new MyGraph();

        for (Node v : verts) {
            if (v.getEdges().size() >= graphHotpointThreshold && !hotpoints.getOrDefault(v, false)) {
//                graphHotpoint.addVertex(v);
                graphHotpoints.addNode(new Node(v));
//                v.setHotPoint(true);
                hotpoints.put(v, true);
            } else if(v.getEdges().size() < graphHotpointThreshold && hotpoints.getOrDefault(v, false)) {
//                graphHotpoint.removeVertex(v);
                graphHotpoints.removeNode(v.getID());
//                v.setHotPoint(false);
                hotpoints.put(v, false);
            }
            if ( hotpoints.getOrDefault(v, false)) {
                changedHotPoints.add(v);
            }
        }

        for (Node v : changedHotPoints) {
            for (Node hp : graphHotpoints.getNodes()) {
                if (!v.equals(hp)) {
                    Set<Node> visited = new HashSet<>();
                    Stack<Node> stack = new Stack<>();
                    List<List<Node>> pathList = new LinkedList<List<Node>>();
                    findAllSimplePathBetweenHP(g,v,hp,visited,stack,pathList);

                    ArrayList<Edge> edgesRemove = new ArrayList<>();
                    for (Edge e : v.getEdges()) {
                        if (hp.getEdges().contains(e) ) {
                            edgesRemove.add(e);
                        }
                    }
                    for (Edge e : edgesRemove) {
                        v.removeEdge(e);
                    }

                    for (List<Node> path : pathList) {
                        graphHotpoints.addEdge(new Edge(v.id,hp.id));
                    }

                    visited.clear();
                    stack.clear();
                    pathList.clear();
                    findAllSimplePathBetweenHP(g, hp,v,visited,stack,pathList);


                    ArrayList<Edge> edgesRemove2 = new ArrayList<>();
                    for (Edge e : hp.getEdges()) {
                        if (v.getEdges().contains(e) ) {
                            edgesRemove2.add(e);
                        }
                    }
                    for (Edge e : edgesRemove2) {
                        hp.removeEdge(e);
                    }


                    for (List<Node> path : pathList) {
                        graphHotpoints.addEdge(new Edge(hp,v));
                    }
                }
            }
        }


    }


    private void findAllSimplePath(Node start, Node end, MyGraph g, Set<Node> visited, Stack<Node> stack ,List<List<Node>> pathList) {

        visited.add(start);
        stack.push(start);

        if (start.equals(end)) {

            List<Node> path = new LinkedList();
            for (Node v : stack) {
                path.add(v);
            }
            pathList.add(path);
        } else {
            if (stack.size() < pathLength) {
                for (Edge e : start.getOutgoingEdges()) {
                    if (hotpoints.getOrDefault(g.getNode(e.to), false) && g.getNode(e.to) != end) {
                        continue;
                    }
                    if (!visited.contains(g.getNode(e.to))) {
                        findAllSimplePath(g.getNode(e.to),end, g, visited,stack,pathList);
                    }
                }
            }
        }
        stack.pop();
        visited.remove(start);
    }

    private void findAllSimplePathBetweenHP(MyGraph g, Node start, Node end, Set<Node> visited, Stack<Node> stack ,List<List<Node>> pathList) {

        visited.add(start);
        stack.push(start);

        if (start.equals(end)) {

            List<Node> path = new LinkedList();
            for (Node v : stack) {
                path.add(v);
            }
            pathList.add(path);
        } else {
            if (stack.size() < pathLength) {
                for (Edge e : start.getOutgoingEdges()) {
                    if (hotpoints.getOrDefault(g.getNode(e.from), false) && g.getNode(e.to) != end) {
                        continue;
                    }
                    if (!visited.contains(g.getNode(e.to))) {
                        findAllSimplePath(g.getNode(e.to),end, g, visited,stack,pathList);
                    }
                }
            }
        }
        stack.pop();
        visited.remove(start);
    }



}