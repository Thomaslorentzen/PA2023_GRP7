import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        // Specify the root directory of your Java codebase
        String rootDirectory = "/Users/ThomasG.Lodberg1/Documents/Repositories/DTU/ProgramAnalysis/course-02242-examples/src/dependencies/java/dtu/deps/";

        // Create a map to store dependencies
        Map<String, Set<String>> dependencyMap = new HashMap<>();

        String patt = "^(?!\\s*//).import\\s+([\\w.]+);";
        String test = "(?:import|new|package)\\s+([\\w.]+)";
        // Use a regular expression to match import statements
        Pattern importPattern = Pattern.compile(test);

        // Recursively search for Java files and analyze their dependencies
        analyzeFiles(rootDirectory, dependencyMap, importPattern);

        // Print the dependency graph
        for (Map.Entry<String, Set<String>> entry : dependencyMap.entrySet()) {
            String className = entry.getKey();
            Set<String> dependencies = entry.getValue();
            System.out.println(className + " depends on: " + dependencies);
        }
    }

    private static void analyzeFiles(String directory, Map<String, Set<String>> dependencyMap, Pattern importPattern) {
        File folder = new File(directory);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively analyze files in subdirectories
                    analyzeFiles(file.getAbsolutePath(), dependencyMap, importPattern);
                } else if (file.getName().endsWith(".java")) {
                    // Analyze Java files
                    analyzeJavaFile(file, dependencyMap, importPattern);
                }
            }
        }
    }

    private static void analyzeJavaFile(File file, Map<String, Set<String>> dependencyMap, Pattern importPattern) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String className = "";
            boolean inMultiLineComment = false;

            var list = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                if(line.contains("//"))
                    continue;
                if (inMultiLineComment) {
                    // Check if the current line ends the multi-line comment
                    if (line.contains("*/")) {
                        inMultiLineComment = false;
                    }
                    continue;
                }

                // Check for the start of a multi-line comment
                if (line.contains("/*")) {
                    inMultiLineComment = true;
                    continue;
                }

                if (line.contains("class ") && (line.contains("public") || line.contains("private") ||
                        line.contains("protected")) && inMultiLineComment == false){

                    // Extract the class name
                    className = line.split("class ")[1].split(" ")[0];
                    // Initialize the class entry in the dependency map if it doesn't exist
                    dependencyMap.putIfAbsent(className, new HashSet<>());
                }
                Matcher matcher = importPattern.matcher(line);
                while (matcher.find()) {
                    // Extract the imported class/package and add it as a dependency
                    String importedClass = matcher.group(1);
                    if(className != ""){
                    dependencyMap.get(className).add(importedClass);}
                    else
                    {
                        list.add(importedClass);
                    }
                }

            }

            for(var element : list){

                dependencyMap.get(className).add(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
