import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        //String rootPath = "C:/Users/Razer/IdeaProjects/example-dependency-graphs";
        //String sourcePath = "C:/Users/Razer/IdeaProjects/PA-1.0-SNAPSHOT.jar";
        //String sourcePath = "C:/Users/Razer/IdeaProjects/example-dependency-graphs-1.0-SNAPSHOT.jar";
        String sourcePath = "C:/Users/Razer/IdeaProjects/course-02242-examples/decompiled/dtu/compute/exec";
        String extractionPath = "C:/Users/Razer/IdeaProjects/ExtractedFiles/";
        Map<String, Set<String>> classDependencies = new HashMap<>();

        var jsonFilesList = EvaluateFolder(new File(sourcePath), classDependencies);

        ArrayList<Object> cases1 = new ArrayList<>();
        for(int i=0; i<jsonFilesList.size(); i++){
            var cases  = analyzeJsonFile(jsonFilesList.get(i));

            cases1.add(cases);
        }





        // STEP 1: Get Json Files Path:

        // STEP 2: Get Bytecode:

        // STEP 1: UNZIP JAR FILE
        //var classFilePathList = unzipFolder(sourcePath, extractionPath);

        // STEP 2: CONVERT TO CLASS TO JSON
        //var jsonFilesList = convertClassFilesToJson(classFilePathList, classDependencies);

        // STEP 3: FIND DEPENDENCIES IN JSON FILES

        //EvaluateFolder(new File(sourcePath), classDependencies);

         //for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
         //    System.out.println("Class: " + entry.getKey() + " depends on : " + entry.getValue());
         //}

        //String dotFilePath = "class_dependency_graph.dot";
        //plotGraph(classDependencies);

    }

    private static ArrayList<String> unzipFolder(String sourcePath, String targetPath){
        var classFilesList = new ArrayList<String>();

        try (FileInputStream fis = new FileInputStream(sourcePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            byte[] buffer = new byte[1024];
            ZipEntry zipEntry;

            while ((zipEntry = zis.getNextEntry()) != null) {
                String entryName = zipEntry.getName();

                if (!zipEntry.isDirectory()) {

                    // Create directories if they don't exist
                    File outFile = new File(targetPath, entryName);
                    File parentDir = outFile.getParentFile();

                    // Create parent directories
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                        if(outFile.getName().endsWith(".class") && !outFile.getName().contains("$")){
                            classFilesList.add(outFile.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classFilesList;
    }

    private static ArrayList<String> convertClassFilesToJson(ArrayList<String> classFilesList, Map<String, Set<String>> depenedencies){
        var jsonFilesList = new ArrayList<String>();

        for (String file : classFilesList) {
            String jsonFilePath = file.replace(".class", ".json");
            String[] command = {"jvm2json","-s", file, "-t", jsonFilePath};

            try {
                Process process = Runtime.getRuntime().exec(command);
                int processExitCode = process.waitFor();

                if(processExitCode == 0) {
                    jsonFilesList.add(jsonFilePath);

                    var classDependencies = analyzeJsonFile(jsonFilePath);

                    //for(int i=0, i<classDependencies.size(); )

                    String className="";
                    Pattern pattern = Pattern.compile("([^/\\\\\\\\]+)$");
                    Matcher matcher = pattern.matcher(jsonFilePath);
                    if (matcher.find()) {
                        className = matcher.group(1);
                        className = className.replace(".json","");
                    }

                    /*int lastSlashIndex = jsonFilePath.lastIndexOf("\\");
                    int jsonExtensionIndex = jsonFilePath.lastIndexOf(".json");

                    if (lastSlashIndex >= 0 && jsonExtensionIndex > lastSlashIndex) {
                        className = jsonFilePath.substring(lastSlashIndex + 1, jsonExtensionIndex);
                    }*/

                    //Set<String> dependenciesList = new HashSet<>(classDependencies);
                    //depenedencies.put(className, dependenciesList);
                }

                if (processExitCode != 0)
                    System.out.println("ERROR: While Converting .class file to json :=> " + file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jsonFilesList;
    }

    private static ArrayList<Object> analyzeJsonFile(String filePath){
        var dependencies = new ArrayList<Object>();


        JSONParser jsonParser = new JSONParser();

        try{
            FileReader file = new FileReader(filePath);

            Object object = jsonParser.parse(file);

            JSONObject jsonObject = (JSONObject) object;

            dependencies = extractDependenciesFromJsonFile(jsonObject);

            System.out.println(filePath);
            /*for (String dependency : dependencies) {
                System.out.println("Dependency: " + dependency);
            }
            System.out.println();*/

        }catch(Exception e){
            e.printStackTrace();
        }

        return dependencies;
    }

    private static ArrayList<Object> extractDependenciesFromJsonFile(JSONObject jsonObject){
        ArrayList<Object> cases = new ArrayList<>();

        JSONArray methods = (JSONArray) jsonObject.get("methods");
        /*if (methods != null) {
            for (Object fieldObj : methods) {
                JSONObject method = (JSONObject) fieldObj;
                String name =  method.get("name").toString();
                JSONObject code = (JSONObject) method.get("code");
                if (code != null) {
                    JSONArray bytecode =  (JSONArray) code.get("bytecode");
                    if (bytecode != null) {
                        for(int i =0; i<bytecode.size();i++){
                            System.out.println(name);
                            System.out.println(bytecode.get(i));
                        }
                    }
                }
            }
        }
         */

        for(int i=0;i<methods.size();i++){
            JSONObject method = (JSONObject) methods.get(i);
            JSONArray annotations = (JSONArray) method.get("annotations");
            for(int j=0; j<annotations.size();j++){


                if(annotations.get(j) == "dtu/compute/exec/Case"){
                    cases.add(annotations);
                }
            }
        }






        return cases;
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

    private static ArrayList<String> EvaluateFolder(File folder, Map<String, Set<String>> classDependencies) {
        File[] files = folder.listFiles();
        ArrayList<String> jsonFilesList = new ArrayList<String>();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    EvaluateFolder(file, classDependencies);
                } else if (file.isFile() && file.getName().endsWith(".json")) {
                    //EvaluateFile(file, classDependencies);
                    //convertJavaToClassFormat(file, classDependencies);
                    //analyzeJsonFile(file.getPath());
                    jsonFilesList.add(file.getPath());
                }
            }
        }

        return jsonFilesList;
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