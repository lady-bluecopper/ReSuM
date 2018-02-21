package eu.unitn.disi.db.resum.clustering;

import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Implementation of the k-means clustering algorithm to group the weighting
 * functions in K groups.
 *
 * @author bluecopper
 */
public class KMeans extends KClusterer {

    public KMeans(ArrayList<ArrayList<Double>> featureMap, Distance distance) {
        super(featureMap, distance);
    }

    public int[] findClustering(int numClusters) {
        return LloydClustering(numClusters);
    }

    protected int[] LloydClustering(int numClusters) {
        // Initializing structures
        int[] clustering = new int[usersNum];
        final ArrayList<ArrayList<Double>> initialClusterCentroids = (Settings.smart) ? smartSeeding(numClusters) : randomSeeding(numClusters);
        IntStream.range(0, usersNum).parallel().forEach(index -> {
                clustering[index] = findSimCluster(index, initialClusterCentroids);
        });
        // Starting the iterations
        boolean converged = false;
        int iter = 0;
        while (!converged && iter < Settings.numberOfIterations) {
            converged = true;
            iter++;
            // Re-computing the centroids
            final ArrayList<ArrayList<Double>> clusterCentroids = findCentroids(clustering, numClusters);
            converged = (IntStream.range(0, usersNum).parallel().mapToDouble(index -> {
                int old = clustering[index];
                clustering[index] = findSimCluster(index, clusterCentroids);
                return (old == clustering[index]) ? 0 : 1;
            }).sum() == 0);
            if (converged) {
                centroids = clusterCentroids;
            }
        }
        return clustering;
    }

}
