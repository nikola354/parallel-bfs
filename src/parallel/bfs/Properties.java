package parallel.bfs;

public class Properties {
    private final int verticesCount;
    private final double density;
    private final boolean quiet;
    private final boolean directed;
    private final int threadsCount;

    public Properties(int verticesCount, double density, boolean quiet, boolean directed, int threadsCount) {
        this.verticesCount = verticesCount;
        this.density = density;
        this.quiet = quiet;
        this.directed = directed;
        this.threadsCount = threadsCount;
    }

    public int verticesCount() {
        return verticesCount;
    }

    public double density() {
        return density;
    }

    public boolean quiet() {
        return quiet;
    }

    public boolean directed() {
        return directed;
    }

    public int threadsCount() {
        return threadsCount;
    }
}
