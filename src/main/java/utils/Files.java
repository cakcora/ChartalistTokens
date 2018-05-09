package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxa123230 on 5/9/2018.
 */
public class Files {
    public static List<String> getTokenFiles(String dir) {
        List<String> results = new ArrayList<String>();
        File[] files = new File(dir).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }
}
