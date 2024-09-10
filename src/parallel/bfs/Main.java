package parallel.bfs;

import org.apache.commons.cli.*;
import parallel.bfs.workers.DirectedGraphGenerator;

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

    }

    private static Boolean[][] createDirectedGraph() {
        Boolean[][] graph = createEmptyGraph();
        Thread[] threads = new Thread[properties.threadsCount()];

        printMsg("Starting generation of directed graph...");

        long startTime = System.currentTimeMillis();
        for (int row = 0; row < properties.threadsCount(); row++) {
            threads[row] = new Thread(new DirectedGraphGenerator(graph, properties, row));
            threads[row].start();
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
//        ThreadLocalRandom random = ThreadLocalRandom.current();
//
//        for (int i = 0; i < verticesCount; i++) {
//            for (int j = i + 1; j < verticesCount; j++) {
//                graph[i][j] = random.nextDouble() < density;
//                graph[j][i] = graph[i][j];
//            }
//        }

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