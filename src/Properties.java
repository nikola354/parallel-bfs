public class Properties {
    private int verticesCount;
    private final double density;
    private final boolean quiet;
    private final boolean directed;
    private final int threadsCount;
    private final String outputFile;
    private final String inputFile;

    public Properties(int verticesCount, double density, boolean quiet, boolean directed, int threadsCount, String outputFile, String inputFile) {
        this.verticesCount = verticesCount;
        this.density = density;
        this.quiet = quiet;
        this.directed = directed;
        this.threadsCount = threadsCount;
        this.outputFile = outputFile;
        this.inputFile = inputFile;
    }

    public String outputFile() {
        return outputFile;
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

    public String inputFile() {
        return inputFile;
    }

    public void setVerticesCount(int verticesCount) {
        this.verticesCount = verticesCount;
    }
}
