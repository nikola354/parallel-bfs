package parallel.bfs.workers;

import parallel.bfs.Properties;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BfsWorker implements Runnable {
    private final Boolean[][] graph;
    private final int[] parent;
    private final AtomicBoolean[] visited;
    private final BlockingQueue<Integer> currentVertices;
    private final BlockingQueue<Integer> futureVertices;
    private final Properties properties;
    private final long[] threadTimes;
    private final int workerId;

    public BfsWorker(Boolean[][] graph,
                     int[] parent,
                     AtomicBoolean[] visited,
                     BlockingQueue<Integer> currentVertices,
                     BlockingQueue<Integer> futureVertices,
                     Properties properties,
                     long[] threadTimes,
                     int workerId) {
        this.graph = graph;
        this.parent = parent;
        this.visited = visited;
        this.currentVertices = currentVertices;
        this.futureVertices = futureVertices;
        this.properties = properties;
        this.threadTimes = threadTimes;
        this.workerId = workerId;
    }

    @Override
    public void run() {
        long startWorkerTime = System.currentTimeMillis();

        Integer vertex;
        while ((vertex = currentVertices.poll()) != null) {
            for (int neighbor = 0; neighbor < properties.verticesCount(); neighbor++) {
                if (graph[vertex][neighbor] && !visited[neighbor].getAndSet(true)) {
                    futureVertices.add(neighbor);
                    parent[neighbor] = vertex;
                }
            }
        }

        threadTimes[workerId] += System.currentTimeMillis() - startWorkerTime;
    }
}
