import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        String rootPath = "C:\\emacs\\git\\course-02242-examples";
        Map<String, Set<String>> classDependencies = new HashMap<>();

        evaluateFolder(new File(rootPath), classDependencies);

        for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
            System.out.println("Class: " + entry.getKey());
            System.out.println("Dependencies: " + entry.getValue());
            System.out.println();
        }
    }

    private static void evaluateFolder(File folder, Map<String, Set<String>> classDependencies) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    evaluateFolder(file, classDependencies);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    evaluateFile(file, classDependencies);
                }
            }
        }
    }

    private static void evaluateFile(File file, Map<String, Set<String>> classDependencies) {
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

                Pattern importPattern = Pattern.compile("import\\s+([^;]+);");
                Matcher importMatcher = importPattern.matcher(line);
                while (importMatcher.find()) {
                    String dependency = importMatcher.group(1).trim();
                    dependencies.add(dependency);
                }

                Pattern staticImportPattern = Pattern.compile("import\\s+static\\s+([^;]+);");
                Matcher staticImportMatcher = staticImportPattern.matcher(line);
                while (staticImportMatcher.find()) {
                    String staticDependency = staticImportMatcher.group(1).trim();
                    dependencies.add(staticDependency);
                }

                Pattern classReferencePattern = Pattern.compile("\\b([A-Z]\\w*)\\b");
                Matcher classReferenceMatcher = classReferencePattern.matcher(line);
                while (classReferenceMatcher.find()) {
                    String classReference = classReferenceMatcher.group(1);
                    if (!classReference.equals(className)) {
                        dependencies.add(classReference);
                    }
                }
            }
            reader.close();

            classDependencies.put(className, dependencies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}