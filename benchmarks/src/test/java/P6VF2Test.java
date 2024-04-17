

import P6VF2.runner.App;


import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class P6VF2Test {

    @org.junit.Test
    public void test01() {

        String default_path = "C:\\Users\\moude\\IdeaProjects\\VF2\\data\\graphDB\\";
        Path graphPath = Paths.get(default_path, "mygraphdb.data");
        Path queryPath = Paths.get(default_path, "QManual.my");
//		Path queryPath = Paths.get(default_path, "Q24.my");
        Path outPath = Paths.get(default_path, "res_Q24.my");

        String[] args = new String[] {
                "-t", default_path + "mygraphdb.data",
                "-q", default_path + "QManual.my"
        };

        try {
            App.main(args);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        assertTrue("test", true);
    }

    @org.junit.Test
    public void test02() {

        String default_path = "C:\\Users\\moude\\IdeaProjects\\VF2\\data\\graphDB\\";
        Path graphPath = Paths.get(default_path, "mygraphdb.data");
//        Path queryPath = Paths.get(default_path, "QManual.my");
        Path queryPath = Paths.get(default_path, "Q24.my");
        Path outPath = Paths.get(default_path, "res_Q24.my");

        String[] args = new String[] {
                "-t", default_path + "mygraphdb.data",
                "-q", default_path + "Q24.my"
        };

        try {
            App.main(args);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        assertTrue("test", true);
    }

    @org.junit.Test
    public void test03() {

        String default_path = "C:\\Users\\moude\\IdeaProjects\\VF2\\data\\graphDB\\";


        String[] files = new String[] {
//                "Q4",
//                "Q8",
//                "Q12",
//                "Q16",
                "Q20",
//                "Q24"
        };

        for (String f :
                files) {
            String[] args = new String[] {
                    "-t", default_path + "mygraphdb.data",
                    "-q", default_path + f+".my"
            };

            try {
                App.main(args);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }



        assertTrue("test", true);
    }

    @org.junit.Test
    public void test04() {

        String default_path = "C:\\Users\\moude\\IdeaProjects\\VF2\\data\\graphDB\\";


        String[] files = new String[] {
                "Q4",
                "Q8",
                "Q12",
                "Q16",
                "Q20",
                "Q24"
        };

        for (String f :
                files) {
            String[] args = new String[] {
                    "-t", default_path + "mygraphdb.data",
                    "-q", default_path + f+".my"
            };

            try {
                App.main(args);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }



        assertTrue("test", true);
    }

    @org.junit.Test
    public void test06() {

        String default_path = "C:\\Users\\moude\\IdeaProjects\\VF2\\data\\graphDB\\";

        String[] args = new String[] {
                "-t", default_path + "mygraphdb.data",
                "-q", default_path + "testinput.txt"
        };

        try {
            App.main(args);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


        assertTrue("test", true);
    }
}
