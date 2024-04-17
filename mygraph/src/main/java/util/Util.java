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
}
