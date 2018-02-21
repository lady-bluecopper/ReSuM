package eu.unitn.disi.db.resum.distance;

import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public abstract class ClusterDistance implements Distance<Collection<Integer>> {
    
    Double[][] adjacencyMatrix;
    
    public ClusterDistance(Double[][] adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }
    
    public abstract double[][] computeInterClusterDistanceMatrix(int[] clustering, int clustersNum);
    
    public abstract double[] computeIntraClusterDistanceVector(int[] clustering, int clustersNum);
    
}
