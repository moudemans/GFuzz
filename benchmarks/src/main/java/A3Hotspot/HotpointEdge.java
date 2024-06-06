package A3Hotspot;


import java.util.List;

public class HotpointEdge  {

    public List<CustomVertex> path;

    public HotpointEdge(List<CustomVertex> path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "HE: (" + this.path.size() + "   " +  super.toString() +  ")";
    }

}
