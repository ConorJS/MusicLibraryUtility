package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileHandlerUtil {

    /**
     * Get all files
     *
     * @param path  Root to begin searching from
     * @param recurse Flag to check all subdirectories (and their subdirectories, i.e recursive checking)
     *
     * @return All the files located beneath the root path (this includes contents of subdirectories)
     */
    public static List<File> getAllFiles(String path, boolean recurse){
        File[] files = new File(path).listFiles();

        if (files != null) {
            return Arrays.stream(files)
                    .flatMap(file -> {
                        List<File> subDirectoryFiles = new ArrayList<>(); // also will store tree leaves (files) in 1-size array

                        if (file.isDirectory() && recurse) {
                            subDirectoryFiles.addAll(getAllFiles(file.getPath(), true));

                        } else {
                            subDirectoryFiles.add(file);
                        }

                        return subDirectoryFiles.stream();
                    }).collect(Collectors.toList());

        } else {
            return new ArrayList<>();
        }
    }

}
