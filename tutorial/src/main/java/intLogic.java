public class intLogic {

    public static int testBranch(String s) {
//        System.out.println("INPUT: " + s);
        int a = Integer.parseInt(s);

        if (a <= 10) {
            return 1;
        }
        else if (a <= 20) {
            return 2;
        }
        else if (a <= 30) {
            return 3;
        }
        else if (a <= 40) {
            return 4;
        }
        else if (a <= 50) {
            return 5;
        }
        else if (a <= 100) {
            return 6;
        } else if (a == 255) {
            return 8;
        } else if (a >= 500) {
            return 9;
        }
        return 7;



    }
}
