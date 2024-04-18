package P8Cycle;

import java.util.ArrayList;
import java.util.HashSet;

public class Walker {
    private HashSet<Integer> visited = new HashSet<>();
    private ArrayList<Integer> path = new ArrayList<>();
    int steps = 0;
    int currentLocation;

    public Walker() {

    }

    public Walker(HashSet<Integer> visited, int steps, int currentLocation) {
        this.visited = visited;
        this.steps = steps;
        this.currentLocation = currentLocation;
    }

    public Walker(Walker w) {
        this.visited = new HashSet<>(w.getVisited());
        this.path = new ArrayList<>(w.getPath());
        this.steps = w.getSteps();
        this.currentLocation = w.getCurrentLocation();
    }

    public HashSet<Integer> getVisited() {
        return visited;
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public int getSteps() {
        return steps;
    }

    public int getCurrentLocation() {
        return currentLocation;
    }


    public Walker(int currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void makeStep(int toNodeID) {
        currentLocation = toNodeID;
        steps += 1;
    }

}
