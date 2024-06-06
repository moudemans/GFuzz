package S2Vertical;

import tudcomponents.MyGraph;
import tudcomponents.Node;

public class S2Logic {
    int hit = 0;

    String pattern1 = "-->";
    String pattern2 = "www.";
    String pattern3 = "@domain.nl";
    String pattern4 = "#a";
    String pattern5 = "err";

    int bound1 = 0;
    int bound2 = -12345;
    int bound3 = 42;
    int bound4 = 7;
    int bound5 = 85347346;


    public void run(MyGraph g) {
     // Cyccle
        traverse(g);
    }

    private void traverse(MyGraph g) {

        Node n = g.getNode(g.getNodes().get(0).getEdges().iterator().next().to);

        if (n.properties.containsKey("cat1")) {
            hit++;
            method1(n.properties.get("cat1"));
        } else if (n.properties.containsKey("cat2")) {
            hit++;
            method2(n.properties.get("cat2"));
        } else if (n.properties.containsKey("cat3")) {
            hit++;
            method3(n.properties.get("cat3"));
        } else if (n.properties.containsKey("cat4")) {
            hit++;
            method4(n.properties.get("cat4"));
        } else if (n.properties.containsKey("cat5")) {
            hit++;
            method5(n.properties.get("cat5"));
        } else if (n.properties.containsKey("cat6")) {
            hit++;
            method6(n.properties.get("cat6"));
        }
    }

    private void method1(String value) {
        if (value.contains(pattern1)) {
            hit++;
        }
    }

    private void method2(String value) {
        if (value.endsWith(pattern2)) {
            hit++;
        }
    }


    private void method3(String value) {
        if (value.startsWith(pattern3)) {
            hit++;
        }
    }


    private void method4(String value) {
        int number = 0;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return;
        }
        if (number <= bound1) {
            hit++;
        }
    }


    private void method5(String value) {
        int number = 0;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return;
        }
        if (number >= bound2) {
            hit++;
        }
    }


    private void method6(String value) {
        int number = 0;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return;
        }
        if (number == bound3) {
            hit++;
        }
    }


}