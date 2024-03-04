package Components;

import java.util.Random;

public class Sandbox {

    public static void main(String[] args) {
        Random r = new Random();
        MyGraph g = MyGraph.readGraphFromFile("fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/gdbFuzz/seed.txt");

        int trials = 1000;
        int byteMutationErrors = 0;

        System.out.println("Starting byte mutations ... ");
        for (int i = 0; i < trials; i++) {
            System.out.print("\r At " + i);
            try {
                GraphUtil.byteMutation(g, 1, r);
            } catch (Exception e) {
                byteMutationErrors++;
            }
        }

        int bitMutationErrors = 0;
        System.out.println();
        System.out.println("Starting bit mutations ... ");
        for (int i = 0; i < trials; i++) {
            System.out.print("\r At " + i);
            try {
                GraphUtil.bitMutation(g, 1, r);
            } catch (Exception e) {
                bitMutationErrors++;
            }
        }

        double perc1 = (double) byteMutationErrors / trials * 100;
        double perc2 = (double) bitMutationErrors / trials * 100;
        System.out.println();
        System.out.println("Invalid byte mutations: " + perc1 + " - (" + byteMutationErrors + " / " + trials + ")");
        System.out.println("Invalid bit mutations: " + perc2 + " - (" + bitMutationErrors + " / " + trials + ")");

    }
}
