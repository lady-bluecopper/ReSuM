package eu.unitn.disi.db.resum.clustering.quality;

/**
 *
 * @author bluecopper
 */
public abstract class QualityMeasure {
    
    final Double[][] adjacencyMatrix;
    
    public QualityMeasure(Double[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }
    
    public abstract double quality(int[] clustering, int clustersNum);
    
}
