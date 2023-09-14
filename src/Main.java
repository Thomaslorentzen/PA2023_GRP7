import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import org.json.simple.JSONObject;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        //String rootPath = "C:/Users/Razer/IdeaProjects/example-dependency-graphs";
        String path1 = "C:/Users/Razer/IdeaProjects/PA-1.0-SNAPSHOT.jar";

        try (FileInputStream fis = new FileInputStream(path1);
             ZipInputStream zis = new ZipInputStream(fis)) {

            byte[] buffer = new byte[1024];
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    File outFile = new File(entryName);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        System.out.println("Extracted: " + outFile.getAbsolutePath());
                    }
                }
            }

            System.out.println("Extraction completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }



        Map<String, Set<String>> classDependencies = new HashMap<>();

        JSONObject jsonObject=new JSONObject();

        EvaluateFolder(new File(path1), classDependencies);

         //for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
         //    System.out.println("Class: " + entry.getKey() + " depends on : " + entry.getValue());
         //}

        //String dotFilePath = "class_dependency_graph.dot";
        //plotGraph(classDependencies);

    }

    private static void EvaluateFolder(File folder, Map<String, Set<String>> classDependencies) {
         File[] files = folder.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    EvaluateFolder(file, classDependencies);
                } else if (file.isFile() && file.getName().endsWith(".class")) {
                    //EvaluateFile(file, classDependencies);
                    convertJavaToClassFormat(file, classDependencies);
                }
            }
        }
    }

    private static void convertJavaToClassFormat(File file, Map<String, Set<String>> classDependencies){
        try{;

            String filePath = file.getPath();
            //filePath = filePath.replace("java", "");
            String[] convertJavaToClassFileCommand = {"javac ", filePath};

            Process process1 = Runtime.getRuntime().exec(convertJavaToClassFileCommand);
            
            int process1ExitCode = process1.waitFor();

            // jvm2json -s Test.class -t test.json
            
            if(process1ExitCode == 0){
                String filePathWithClassExtension = file.getPath().replace(".java", ".class");
                //String jsonPath = file.getPath().replace(".java", ".json");

                String jsonFilePath = file.getPath().replace(".java", ".json");
                String[] convertClassToJsonFileCommand = {"jvm2json >", filePathWithClassExtension, " <" , jsonFilePath};
                Process process2 = Runtime.getRuntime().exec(convertClassToJsonFileCommand);
                int process2ExitCode = process2.waitFor();

                if(process2ExitCode == 0){
                    System.out.println("Here we go");
                    // read json file:::
                }
                else{
                    System.err.println("Converting .class file to json. Exit Code: "+process2ExitCode);
                }

            }
            else{
                System.err.println("Converting java to class format. Exit code: " + process1ExitCode);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    //private static Set<String> extractDependenciesFromJsonFile(File file, Map<String, Set<String>> classDependencies){
    //}

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
                    String[] dividedText = importText.split("\\.");
                    importText = dividedText[dividedText.length-1].replace(";", "").toLowerCase();
                }

                String[] dividedText = importText.split("\\.");
                String className = dividedText[dividedText.length-1].replace(";", "").toLowerCase();

                dependencies.add(className);

                return super.visitImport(importTree, aVoid);
            }

            @Override
            public Void visitVariable(VariableTree variableTree, Void aVoid) {
                String variableType = variableTree.getType().toString();

                dependencies.add(variableType.replace("[]", "").toLowerCase());
                return super.visitVariable(variableTree, aVoid);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree methodInvocationTree, Void aVoid) {
                String methodName = methodInvocationTree.getMethodSelect().toString();
                if (methodName.contains(".")) {
                    String dependency = methodName.substring(0, methodName.lastIndexOf('.'));
                    int firstIndex = methodName.indexOf('.');
                    int lastIndex = methodName.lastIndexOf('.');
                    if(firstIndex != lastIndex){
                        String[] dividedText = dependency.split("\\.");
                        String extractedDependency;
                        if(dividedText.length == 2){
                            extractedDependency = dividedText[0].replace(";", "").toLowerCase();
                        }
                        else{
                        extractedDependency = dividedText[dividedText.length-1].replace(";", "").toLowerCase();
                        }
                        dependencies.add(extractedDependency);
                    }
                }
                return super.visitMethodInvocation(methodInvocationTree, aVoid);
            }
        };
        compilationUnit.accept(scanner, null);

        return dependencies;
    }

    public static void plotGraph(Map<String, Set<String>> dependencyMap) {
        //createDotFile(dependencyMap);

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
                    //if(value.equals("out")) value = "system."+value;
                    writer.write(value.replace(".","_"));
                    writer.newLine();
                }
            }
            writer.write("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}