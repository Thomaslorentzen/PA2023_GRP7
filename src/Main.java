import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String rootPath  ="C:/Users/Razer/IdeaProjects/course-02242-examples";
        Map<String, Set<String>> classDependencies = new HashMap<>();

        EvaluateFolder(new File(rootPath), classDependencies);

        for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
            System.out.println("Class: " + entry.getKey() + " depends on : " + entry.getValue());
        }

        String dotFilePath = "class_dependency_graph.dot";

        plotGraph(classDependencies);

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
                Matcher packageMatcher = Pattern.compile("^package\\s+([a-zA-Z_][a-zA-Z0-9_.]*);").matcher(line);
                if (packageMatcher.find()) {
                    String dependency = packageMatcher.group(1);
                    dependencies.add(dependency);
                }

                Matcher importMatcher = Pattern.compile("^import\\s+([a-zA-Z_][a-zA-Z0-9_.]*);").matcher(line);
                if (importMatcher.find()) {
                    String dependency = importMatcher.group(1);
                    dependencies.add(dependency);
                }

                Matcher usageMatcher = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_.]*)\\s*[.=]\\s*[\\s\\w]*\\(").matcher(line);
                while (usageMatcher.find()) {
                    String dependency = usageMatcher.group(1);
                    if (!dependency.equals(className)) {
                        dependencies.add(dependency);
                    }
                }

                Matcher instantiationMatcher = Pattern.compile("\\b" + className + "\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*new\\s+" + className + "\\s*\\(").matcher(line);
                if (instantiationMatcher.find()) {
                    dependencies.add(className);
                }
            }
            reader.close();

            classDependencies.put(className, dependencies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void plotGraph(Map<String, Set<String>> dependencyMap) {
        createDotFile(dependencyMap);

        String dotFilePath = "output.dot";
        String outputFilePath = "outputGraph.png";

        try {
            String[] command = {"dot", "-Tpng", dotFilePath, "-o", outputFilePath};

            Process process = Runtime.getRuntime().exec(command);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Graph visualization generated successfully.");

                File pngFile = new File(outputFilePath);
                Desktop.getDesktop().open(pngFile);
            } else {
                System.err.println("Error generating graph visualization. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createDotFile(Map<String, Set<String>> map) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.dot"))) {
            writer.write("digraph G { ");
            writer.newLine();
            for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                Set<String> values = entry.getValue();

                for (String value : values) {
                    writer.write(key + " -> ");
                    writer.write(value.replace(".","_") + ";");
                    writer.newLine();
                }
            }
            writer.write("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}