package util;

import java.io.File;

public class Util {

    public static boolean fileExists(String path) {
        assert (path != null);
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }

    public static boolean dirExists(String path) {
        assert (path != null);

        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    public static String computeTimePassed(long flag) {
        long end = System.currentTimeMillis();
        float sec = (end - flag) / 1000F;
        return String.format("(%f2 s)", sec);

    }
}
