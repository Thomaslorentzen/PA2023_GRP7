import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String rootPath  ="C:/Users/Razer/IdeaProjects/course-02242-examples";
        Map<String, Set<String>> classDependencies = new HashMap<>();

        EvaluateFolder(new File(rootPath), classDependencies);

        for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
            System.out.println("Class: " + entry.getKey());
            System.out.println("Dependencies: " + entry.getValue());
            System.out.println();
        }
    }

    private static void EvaluateFolder(File folder, Map<String, Set<String>> classDependencies) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    EvaluateFolder(file, classDependencies);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    EvaluateFile(file, classDependencies);
                }
            }
        }
    }

    private static void EvaluateFile(File file, Map<String, Set<String>> classDependencies) {
        try {
            String className = file.getName().replace(".java", "");
            Set<String> dependencies = new HashSet<>();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean inBlockComment = false;

            while ((line = reader.readLine()) != null) {
                if (inBlockComment) {
                    if (line.contains("*/")) {
                        inBlockComment = false;
                    }
                    continue;
                } else {
                    if (line.contains("/*")) {
                        inBlockComment = !line.contains("*/");
                        continue;
                    }
                }
                if (line.trim().startsWith("//")) {
                    continue;
                }

                // CODE FOR MATCHERS
                // ....
            }
            reader.close();

            classDependencies.put(className, dependencies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}