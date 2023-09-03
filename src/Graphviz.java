import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.Set;

public class Graphviz {
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

