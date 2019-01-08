package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.stream.IntStream;

/**
 * Implementation of the k-means clustering algorithm to group the weighting
 * functions in K groups.
 *
 * @author bluecopper
 */
public class KMeans extends KClusterer {

    public KMeans(HashIntObjMap<double[]> featureMap, Distance distance) {
        super(featureMap, distance);
    }

    public int[] findClustering(int numClusters) {
        return LloydClustering(numClusters);
    }

    protected int[] LloydClustering(int numClusters) {
        // Initializing structures
        int[] clustering = new int[usersNum];
        final HashIntObjMap<double[]> initialClusterCentroids = (Settings.smart) ? smartSeeding(numClusters) : randomSeeding(numClusters);
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
            final HashIntObjMap<double[]> clusterCentroids = findCentroids(clustering, numClusters);
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
    
    protected int[] recomputeLloydClustering(int numClusters) {
        // Initializing structures
        int[] clustering = new int[usersNum];
        IntStream.range(0, usersNum).parallel().forEach(index -> {
                clustering[index] = findSimCluster(index, centroids);
        });
        // Starting the iterations
        boolean converged = false;
        int iter = 0;
        while (!converged && iter < Settings.numberOfIterations) {
            converged = true;
            iter++;
            // Re-computing the centroids
            final HashIntObjMap<double[]> clusterCentroids = findCentroids(clustering, numClusters);
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

    public int[] recomputeClustering(int clustersNum) {
        return recomputeLloydClustering(clustersNum);
    }

}
