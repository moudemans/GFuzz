package P2Transportation;



import tudcomponents.Edge;
import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.ArrayList;

public class P2Logic {
    MyGraph my_g;

    public void run(MyGraph g) {
        getFirstStation(g);
        getTransport(g);
        getTimesFirstStation(g);
    }

    private int getTimesFirstStation(MyGraph g) {
        Node n = g.getNodes("Centroid").get(0);

        int time1 = 0;
        int time2 = 0;
        int time3 = 0;
        int time4 = 0;
        for (Edge e: n.getEdges()) {
            if (e.getLabel().equals("DRT")) {
                time1 += Integer.parseInt(e.properties.get("inter_times"));
                time2 += Integer.parseInt(e.properties.get("waiting_DRT"));
                time3 += Integer.parseInt(e.properties.get("DRT_time"));
            }
            if (e.getLabel().equals("WALK")) {
                time4 += Integer.parseInt(e.properties.get("walking_time"));
            }
        }
        return time1+time2+time3+time4;
    }

    private int getFirstStation(MyGraph g) {
        Node n =   g.getNodes("Centroid").get(0);
        for (Edge e : n.getEdges()) {
            if (e.getLabel().equals("DRT")) {
                return e.to;
            }
            if (e.getLabel().equals("WALK")) {
                return e.to;
            }
        }
        //TODO: throw error?
        return -1;
    }

    private String getTransport(MyGraph g) {
        int transport_count = 0;
        ArrayList<String> trsp = new ArrayList<>();

        Node n =  g.getNodes("Centroid").get(0);
        for (Edge e : n.getEdges()) {
            if (e.getLabel().equals("DRT") || e.getLabel().equals("WALK")  ) {
                trsp.add(e.getLabel());
                transport_count += 1;
            }
        }

        String transport;
        if (transport_count == 2) {
            transport= "DRT/WALK";
        } else {
            transport= trsp.get(0);
        }
        return transport;
    }

}