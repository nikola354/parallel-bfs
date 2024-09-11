package parallel.bfs;

import org.apache.commons.cli.*;
import parallel.bfs.workers.BfsWorker;
import parallel.bfs.workers.DirectedGraphGenerator;
import parallel.bfs.workers.UndirectedGraphGenerator;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static Properties properties;

    public static void main(String[] args) {
        properties = initProperties(args);

        //Create graph:
        Boolean[][] graph;
        if (properties.directed()) {
            graph = createDirectedGraph();
        } else {
            graph = createUndirectedGraph();
        }

        int[] parent = bfsLevelBarrier(graph);
    }

    public static int[] bfsLevelBarrier(Boolean[][] graph) {
        printMsg("Starting parallel BFS with level barrier using " + properties.threadsCount() + " threads");
        long startBFS = System.currentTimeMillis();

        int verticesCount = properties.verticesCount();
        int threadsCount = properties.threadsCount();
        int[] parent = new int[verticesCount];
        Arrays.fill(parent, -1);

        AtomicBoolean[] visited = new AtomicBoolean[verticesCount];
        for (int i = 0; i < verticesCount; i++) {
            visited[i] = new AtomicBoolean(false);
        }

        long[] threadsTimes = new long[threadsCount];

        for (int index = 0; index < verticesCount; index++) {
            if (!visited[index].get()) {
                bfsLevelBarrierFromVertex(graph, parent, visited, index, threadsTimes);
            }
        }

        long accumulatedTime = System.currentTimeMillis() - startBFS;

        for (int workerID = 0; workerID < threadsCount; workerID++) {
            printMsg("Parallel BFS worker-" + workerID + " took " + threadsTimes[workerID] + " ms");
        }

        printMsg("Parallel BFS with level barrier using " + threadsCount + " threads took " +
                accumulatedTime + " ms");
        return parent;
    }

    private static void bfsLevelBarrierFromVertex(Boolean[][] graph, int[] parent, AtomicBoolean[] visited, int start, long[] threadsTimes) {
        BlockingQueue<Integer> currentVertices = new LinkedBlockingQueue<>();
        currentVertices.add(start);
        visited[start].set(true);

        BlockingQueue<Integer> futureVertices = new LinkedBlockingQueue<>();

        Thread[] threads = new Thread[properties.threadsCount()];

        while (!currentVertices.isEmpty()) {
            for (int workerId = 0; workerId < properties.threadsCount(); workerId++) {
                threads[workerId] = new Thread(new BfsWorker(graph, parent, visited, currentVertices, futureVertices, properties, threadsTimes, workerId));
                threads[workerId].start();
            }

            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            currentVertices = new LinkedBlockingQueue<>(futureVertices);
            futureVertices.clear();
        }
    }

    private static Boolean[][] createDirectedGraph() {
        Boolean[][] graph = createEmptyGraph();
        Thread[] threads = new Thread[properties.threadsCount()];

        printMsg("Starting generation of directed graph...");

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < properties.threadsCount(); i++) {
            threads[i] = new Thread(new DirectedGraphGenerator(graph, properties, i));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        printMsg("Generation of directed graph took " + (endTime - startTime) + "ms");
        return graph;
    }

    private static Boolean[][] createUndirectedGraph() {
        Boolean[][] graph = createEmptyGraph();
        Thread[] threads = new Thread[properties.threadsCount()];


        final int n = properties.verticesCount();
        BlockingQueue<Integer> tasks = new LinkedBlockingQueue<>(n);
        for (int row = 0; row < n; row++) {
            tasks.add(row);
        }

        printMsg("Starting generation of undirected graph...");

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < properties.threadsCount(); i++) {
            threads[i] = new Thread(new UndirectedGraphGenerator(graph, properties, tasks, i));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        printMsg("Generation of undirected graph took " + (endTime - startTime) + "ms");
        return graph;
    }

    private static Boolean[][] createEmptyGraph() {
        int n = properties.verticesCount();
        Boolean[][] graph = new Boolean[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                graph[i][j] = false;
            }
        }

        return graph;
    }

    public static void printMsg(String msg) {
        if (!properties.quiet()) {
            System.out.println(msg);
        }
    }

    private static void printGraph(Boolean[][] graph) {
        for (int i = 0; i < properties.verticesCount(); i++) {
            for (int j = 0; j < properties.verticesCount(); j++) {
                System.out.print((graph[i][j] ? "1" : "0") + " ");
            }
            System.out.println();
        }
    }

    private static Properties initProperties(String[] args) {
        Options options = new Options();
        options.addOption(new Option("n", true, "Number of vertices"));
        options.addOption(new Option("d", true, "Density (in %)"));
        options.addOption(new Option("q", false, "Enable quiet mode"));
        options.addOption(new Option("t", true, "Threads count (serial execution if argument is not passed)"));
        options.addOption(new Option("directed", false, "Build a directed graph"));

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            boolean quiet = cmd.hasOption("q");
            boolean directed = cmd.hasOption("directed");

            double density = 0.2;
            if (cmd.hasOption("d")) {
                density = Double.parseDouble(cmd.getOptionValue("d"));

                if (density < 1 || density > 99) {
                    throw new RuntimeException("Density must be between 1 and 99");
                }
                density /= 100;
            }

            int threadsCount = 1;
            if (cmd.hasOption("t")) {
                threadsCount = Integer.parseInt(cmd.getOptionValue("t"));

                if (threadsCount < 1 || threadsCount > 32) {
                    throw new RuntimeException("Number of threads must be between 1 and 32");
                }
            }

            if (cmd.hasOption("n")) {
                int verticesCount = Integer.parseInt(cmd.getOptionValue("n"));

                return new Properties(verticesCount, density, quiet, directed, threadsCount);
            } else {
                throw new RuntimeException("You must specify a number of vertices");
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}