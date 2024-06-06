package A3Hotspot;



import tudcomponents.Edge;
import tudcomponents.MyGraph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import tudcomponents.Node;

import java.util.*;

public class A3Logic {

    private Graph<CustomVertex, CustomEdge> graph;
    private Graph<CustomVertex, CustomEdge> graphRev;
    private Graph<CustomVertex, HotpointEdge> graphHotpoint;

    private long actualTimeStamp;
    private final int graphHotpointThreshold = 10;
    private final int pathLength = 4;

    public void run(MyGraph g) {
     // Cyccle
        this.graph = mygraphToHotSpotGraph(g);
        this.graphRev = new DefaultDirectedGraph<CustomVertex, CustomEdge>(CustomEdge.class);
        this.graphHotpoint = new DefaultDirectedGraph<CustomVertex, HotpointEdge>(HotpointEdge.class);
        recalculateHotpoints(graph.vertexSet());
    }

    private Graph<CustomVertex, CustomEdge> crateEmptyGraph(){
        Graph<CustomVertex, CustomEdge> graph;
        graph = new DefaultDirectedGraph<CustomVertex, CustomEdge>(CustomEdge.class);
        return graph;
    }
    private List<CustomVertex> createGraphNodes(MyGraph g){
        List<CustomVertex> nodes = new ArrayList<CustomVertex>();
        for(int i = 0; i < g.getNodes().size(); ++i){
            nodes.add(new CustomVertex(String.valueOf(g.getNodes().get(i).id)));
        }
        return nodes;
    }

    private Graph<CustomVertex, CustomEdge>  mygraphToHotSpotGraph(MyGraph g) {
        Graph<CustomVertex, CustomEdge> dynamicGraph = crateEmptyGraph();
        List<CustomVertex> dynamicNodes = createGraphNodes(g);
        Map<CustomVertex,ArrayList<CustomVertex>> temp = new HashMap<>();

        for(CustomVertex node : dynamicNodes){
            dynamicGraph.addVertex(node);
        }

        for(CustomVertex node : dynamicNodes) {
            ArrayList<CustomVertex> t = new ArrayList<>();
            for (CustomVertex v : dynamicNodes) {
                if (v != node) {
                    t.add(v);
                }
            }
            temp.put(node,t);
        }
        Random rand = new Random(1);

        int basicEdges = 2*dynamicNodes.size();

        for(Node n : g.getNodes()) {
            for (Edge e : n.getEdges()) {
                dynamicGraph.addEdge(dynamicNodes.get(e.from),dynamicNodes.get(e.to),new CustomEdge(true, 5));

            }
        }

        int hotPointCount = rand.nextInt(dynamicNodes.size()/5)+1;

        for (int i=0;i<hotPointCount;i++) {
            CustomVertex start = dynamicNodes.get(rand.nextInt(dynamicNodes.size()));
            int max = Math.min(graphHotpointThreshold, temp.get(start).size());
            for (int j = 0; j < max;j++) {
                dynamicGraph.addEdge(start,temp.get(start).remove(rand.nextInt(temp.get(start).size())),new CustomEdge(true, 5));
            }
        }
        return dynamicGraph;
    }

    private void recalculateHotpoints(Set<CustomVertex> verts) {

        List<CustomVertex> changedHotPoints = new LinkedList<CustomVertex>();

        for (CustomVertex v : verts) {
            if (graph.edgesOf(v).size() >= graphHotpointThreshold && !v.getHotPoint()) {
                graphHotpoint.addVertex(v);
                v.setHotPoint(true);
            } else if(graph.edgesOf(v).size() < graphHotpointThreshold && v.getHotPoint()) {
                graphHotpoint.removeVertex(v);
                v.setHotPoint(false);
            }
            if (v.getHotPoint()) {
                changedHotPoints.add(v);
            }
        }

        for (CustomVertex v : changedHotPoints) {
            for (CustomVertex hp : graphHotpoint.vertexSet()) {
                if (!v.equals(hp)) {
                    Set<CustomVertex> visited = new HashSet<CustomVertex>();
                    Stack<CustomVertex> stack = new Stack<CustomVertex>();
                    List<List<CustomVertex>> pathList = new LinkedList<List<CustomVertex>>();
                    findAllSimplePathBetweenHP(v,hp,visited,stack,pathList);

                    graphHotpoint.removeAllEdges(v,hp);

                    for (List<CustomVertex> path : pathList) {
                        graphHotpoint.addEdge(v,hp,new HotpointEdge(path));
                    }

                    visited.clear();
                    stack.clear();
                    pathList.clear();
                    findAllSimplePathBetweenHP(hp,v,visited,stack,pathList);

                    graphHotpoint.removeAllEdges(v,hp);

                    for (List<CustomVertex> path : pathList) {
                        graphHotpoint.addEdge(hp,v,new HotpointEdge(path));
                    }
                }
            }
        }


    }


    private void findAllSimplePath(CustomVertex start, CustomVertex end, Graph<CustomVertex,CustomEdge> graph, Set<CustomVertex> visited, Stack<CustomVertex> stack ,List<List<CustomVertex>> pathList) {

        visited.add(start);
        stack.push(start);

        if (start.equals(end)) {

            List<CustomVertex> path = new LinkedList();
            for (CustomVertex v : stack) {
                path.add(v);
            }
            pathList.add(path);
        } else {
            if (stack.size() < pathLength) {
                for (CustomEdge e : graph.outgoingEdgesOf(start)) {
                    if (e.getTarget().getHotPoint() && e.getTarget() != end) {
                        continue;
                    }
                    if (!visited.contains(e.getTarget())) {
                        findAllSimplePath(e.getTarget(),end, graph, visited,stack,pathList);
                    }
                }
            }
        }
        stack.pop();
        visited.remove(start);
    }

    private void findAllSimplePathBetweenHP(CustomVertex start, CustomVertex end, Set<CustomVertex> visited, Stack<CustomVertex> stack ,List<List<CustomVertex>> pathList) {

        visited.add(start);
        stack.push(start);

        if (start.equals(end)) {

            List<CustomVertex> path = new LinkedList();
            for (CustomVertex v : stack) {
                path.add(v);
            }
            pathList.add(path);
        } else {
            if (stack.size() < pathLength) {
                for (CustomEdge e : graph.outgoingEdgesOf(start)) {
                    if (e.getTarget().getHotPoint() && e.getTarget() != end) {
                        continue;
                    }
                    if (!visited.contains(e.getTarget())) {
                        findAllSimplePath(e.getTarget(),end, graph, visited,stack,pathList);
                    }
                }
            }
        }
        stack.pop();
        visited.remove(start);
    }



}