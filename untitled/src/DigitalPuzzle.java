import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The entry point for the Digital Puzzle application.
 * This class coordinates the process of loading digital fragments from a file,
 * solving the connection puzzle, and displaying the results.
 *
 * @author Palaida Dmytro
 * @version 1.0
 */
public class DigitalPuzzle {

    /**
     * Path to the source file containing numeric fragments.
     */
    private static final String FILE_PATH = "untitled/text/lines_with_numbers.txt";

    /**
     * The number of digits that must match for two fragments to connect.
     */
    private static final int OVERLAP_SIZE = 2;

    /**
     * The width of the decorative border in the console output.
     */
    private static final int BORDER_SIZE = 40;

    /**
     * Main method that executes the puzzle-solving workflow.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println(">>> START PUZZLE CONNECTION <<<");

        try {
            List<String> fragments = FileLoader.loadFragments(FILE_PATH);
            System.out.println("Fragments loaded: " + fragments.size());

            PuzzleSolver solver = new PuzzleSolver(fragments, OVERLAP_SIZE);

            System.out.println("Finding the longest chain...");
            long startTime = System.currentTimeMillis();

            String result = solver.solve();

            long endTime = System.currentTimeMillis();
            printReport(result, endTime - startTime);

        } catch (FileNotFoundException e) {
            System.err.println("FATAL ERROR: File not found at path: " + FILE_PATH);
        }
    }

    /**
     * Prints a formatted report of the execution results to the console.
     *
     * @param result The final concatenated string of digits.
     * @param timeMs The duration of the execution in milliseconds.
     */
    private static void printReport(String result, long timeMs) {
        String border = "=".repeat(BORDER_SIZE);
        System.out.println("\n" + border);
        System.out.println(" FINAL REPORT");
        System.out.println(border);

        if (result.isEmpty()) {
            System.out.println("No solution found.");
        } else {
            System.out.println("Execution time: " + timeMs + " ms");
            System.out.println("Result length: " + result.length() + " digits");
            System.out.println("Result:\n" + result);
        }
        System.out.println(border);
    }
}

/**
 * Handles the core logic of the puzzle, including graph construction
 * and searching for the longest path.
 */
class PuzzleSolver {
    private final List<String> fragments;
    private final int overlap;
    private List<Integer> bestPath;
    private List<List<Integer>> adjacencyList;

    /**
     * Initializes the solver with fragments and overlap requirements.
     *
     * @param fragments A list of numeric strings to be connected.
     * @param overlap   The number of overlapping characters required for a connection.
     */
    public PuzzleSolver(List<String> fragments, int overlap) {
        this.fragments = fragments;
        this.overlap = overlap;
        this.bestPath = new ArrayList<>();
    }

    /**
     * Starts the solving process by building a graph and performing a DFS search.
     *
     * @return The longest possible sequence formed by the fragments.
     */
    public String solve() {
        buildGraph();

        boolean[] visited = new boolean[fragments.size()];
        for (int i = 0; i < fragments.size(); i++) {
            List<Integer> currentPath = new ArrayList<>();
            currentPath.add(i);
            visited[i] = true;

            dfs(i, visited, currentPath);

            visited[i] = false; // Reset for the next starting node
        }

        return constructStringFromPath(bestPath);
    }

    /**
     * Constructs an adjacency list representing connections between fragments.
     * A connection exists if the suffix of fragment A matches the prefix of fragment B.
     */
    private void buildGraph() {
        adjacencyList = new ArrayList<>();
        for (int i = 0; i < fragments.size(); i++) {
            adjacencyList.add(new ArrayList<>());
            String suffix = getSuffix(fragments.get(i));

            for (int j = 0; j < fragments.size(); j++) {
                if (i == j) continue;
                String prefix = getPrefix(fragments.get(j));

                if (suffix.equals(prefix)) {
                    adjacencyList.get(i).add(j);
                }
            }
        }
    }

    /**
     * Recursive Depth-First Search with backtracking to find the longest path in the graph.
     *
     * @param u           The index of the current fragment.
     * @param visited     Tracking which fragments are used in the current path.
     * @param currentPath The sequence of indices representing the current path.
     */
    private void dfs(int u, boolean[] visited, List<Integer> currentPath) {
        if (currentPath.size() > bestPath.size()) {
            bestPath = new ArrayList<>(currentPath);
        }
        for (int v : adjacencyList.get(u)) {
            if (!visited[v]) {
                visited[v] = true;
                currentPath.add(v);
                dfs(v, visited, currentPath);
                currentPath.removeLast(); // Backtrack
                visited[v] = false;
            }
        }
    }

    /**
     * Concatenates fragments from the given path into a single string,
     * accounting for the overlapping digits.
     *
     * @param path The list of fragment indices in order.
     * @return The combined digital sequence.
     */
    private String constructStringFromPath(List<Integer> path) {
        if (path.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(fragments.get(path.getFirst()));

        for (int i = 1; i < path.size(); i++) {
            String fragment = fragments.get(path.get(i));
            sb.append(fragment.substring(overlap));
        }
        return sb.toString();
    }

    private String getSuffix(String s) {
        return s.substring(s.length() - overlap);
    }

    private String getPrefix(String s) {
        return s.substring(0, overlap);
    }
}

/**
 * Utility class for reading numeric data from external files.
 */
class FileLoader {
    /**
     * Reads numeric fragments from a specified file path.
     *
     * @param path The file system path to the text file.
     * @return A list of valid numeric strings found in the file.
     * @throws FileNotFoundException If the file at the given path does not exist.
     */
    public static List<String> loadFragments(String path) throws FileNotFoundException {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String line = scanner.next().trim();
                if (!line.isEmpty() && line.matches("\\d+")) {
                    list.add(line);
                }
            }
        }
        return list;
    }
}