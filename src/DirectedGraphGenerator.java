import java.util.concurrent.ThreadLocalRandom;

public class DirectedGraphGenerator implements Runnable {
    private final Boolean[][] graph;
    private final Properties properties;
    private final int row;

    public DirectedGraphGenerator(Boolean[][] graph, Properties properties, int row) {
        this.graph = graph;
        this.properties = properties;
        this.row = row;
    }

    @Override
    public void run() {
        Main.printMsg("Starting directed graph generator:  worker-" + row);

        long start = System.currentTimeMillis();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = row; i < properties.verticesCount(); i += properties.threadsCount()) {
            for (int j = 0; j < properties.verticesCount(); j++) {
                graph[i][j] = random.nextDouble() < properties.density();
            }
        }

        long timeElapsed = System.currentTimeMillis() - start;
        Main.printMsg("Directed graph generator: worker-" + row + " took " + timeElapsed + "ms");
    }
}
