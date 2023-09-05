import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        String rootPath = "C:/Users/Razer/IdeaProjects/example-dependency-graphs";
        Map<String, Set<String>> classDependencies = new HashMap<>();

        EvaluateFolder(new File(rootPath), classDependencies);

         for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
             System.out.println("Class: " + entry.getKey() + " depends on : " + entry.getValue());
         }

        //String dotFilePath = "class_dependency_graph.dot";
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
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(file);
            JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, fileObjects);

            for (CompilationUnitTree compilationUnit : javacTask.parse()) {
                String className = "";
                for (Tree tree : compilationUnit.getTypeDecls()) {
                    if (tree instanceof ClassTree) {
                        className = ((ClassTree) tree).getSimpleName().toString();
                        break;
                    }
                }
                className = className.toLowerCase();
                Set<String> dependencies = extractDependencies(compilationUnit);
                classDependencies.put(className, dependencies);
            }

            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> extractDependencies(CompilationUnitTree compilationUnit) {
        Set<String> dependencies = new HashSet<>();
        TreeScanner<Void, Void> scanner = new TreeScanner<Void, Void>() {
            @Override
            public Void visitImport(ImportTree importTree, Void aVoid) {
                String importText = importTree.toString();

                if (importText.endsWith(".*")) {
                    importText = importText.substring(0, importText.length() - 2);
                }

                String className = importText.replace('.', '/');
                dependencies.add(className);

                return super.visitImport(importTree, aVoid);
            }
        };
        compilationUnit.accept(scanner, null);

        return dependencies;
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
                    writer.write(value.replace("/",".").replace(".*",""));
                    writer.newLine();
                }
            }
            writer.write("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}