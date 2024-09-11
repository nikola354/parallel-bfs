package parallel.bfs.workers;

import parallel.bfs.Main;
import parallel.bfs.Properties;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class UndirectedGraphGenerator implements Runnable {
    private final Boolean[][] graph;
    private final Properties properties;
    private final BlockingQueue<Integer> tasks;
    private final int id;

    public UndirectedGraphGenerator(Boolean[][] graph, Properties properties, BlockingQueue<Integer> tasks, int id) {
        this.graph = graph;
        this.properties = properties;
        this.tasks = tasks;
        this.id = id;
    }

    @Override
    public void run() {
        Main.printMsg("Starting undirected graph generator:  worker-" + id);

        long start = System.currentTimeMillis();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        Integer row;
        while ((row = tasks.poll()) != null) {
            graph[row][row] = true;
            for (int i = row + 1; i < properties.verticesCount(); i++) {
                graph[row][i] = random.nextDouble() < properties.density();
                graph[i][row] = graph[row][i];
            }
        }

        long timeElapsed = System.currentTimeMillis() - start;
        Main.printMsg("Undirected graph generator: worker-" + id + " took " + timeElapsed + "ms");
    }
}
