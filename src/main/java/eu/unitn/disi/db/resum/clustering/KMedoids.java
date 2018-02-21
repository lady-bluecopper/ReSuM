package eu.unitn.disi.db.resum.clustering;

import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class KMedoids extends KClusterer {

    public KMedoids(ArrayList<ArrayList<Double>> userVectors, Distance distance) {
        super(userVectors, distance);
    }

    public KMedoids(ArrayList<ArrayList<Double>> userVectors, int patternsNum, Distance distance) {
        super(userVectors, patternsNum, distance);
    }

    protected ArrayList<ArrayList<Double>> findMedoidsUsingCentroids(int[] clustering, int clustersNum) {
        ArrayList<ArrayList<Double>> medoids = new ArrayList<ArrayList<Double>>(clustersNum);
        int[] currMedoids = new int[clustersNum];
        double[] minDists = new double[clustersNum];
        
        ArrayList<ArrayList<Double>> centroid = findCentroids(clustering, clustersNum);
        IntStream.range(0, clustersNum).parallel().forEach(i ->
                minDists[i] = Double.MAX_VALUE    
        );
        for (int i = 0; i < usersNum; i++) {
            int cId = clustering[i];
            double currDist = distance.distance(featureMap.get(i), centroid.get(cId));
            if (currDist < minDists[cId]) {
                minDists[cId] = currDist;
                currMedoids[cId] = i;
            }
        }
        IntStream.range(0, clustersNum).parallel().forEach(i -> 
                medoids.add(i, featureMap.get(currMedoids[i])));
        return medoids;
    }

    protected ArrayList<ArrayList<Double>> findMedoids(int[] clustering, int clustersNum) {
        ArrayList<ArrayList<Double>> medoids = new ArrayList<ArrayList<Double>>(clustersNum);
        int[] currMedoids = new int[clustersNum];
        double[] sumDistances = new double[usersNum];
        double[] minDists = new double[clustersNum];
        IntStream.range(0, clustersNum).parallel().forEach(i ->
                minDists[i] = Double.MAX_VALUE    
        );
        int i, j;
        for (i = 0; i < usersNum - 1; i++) {
            for (j = i; j < usersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    double currDist = distance.distance(featureMap.get(i), featureMap.get(j));
                    sumDistances[i] += currDist;
                    sumDistances[j] += currDist;
                }
            }
        }
        for (i = 0; i < usersNum; i ++) {
            int cId = clustering[i];
            if (sumDistances[i] < minDists[cId]) {
                currMedoids[cId] = i;
                minDists[cId] = sumDistances[i];
            }
        }
        for (i = 0; i < clustersNum; i ++) {
            medoids.add(featureMap.get(currMedoids[i]));
        }
        return medoids;
    }

    public int[] findClustering(int clustersNum) {
        return LloydMedoidClustering(clustersNum);
    }

    protected int[] LloydMedoidClustering(int clustersNum) {
        // Initializing structures
        int[] clustering = new int[usersNum];
        ArrayList<ArrayList<Double>> initialMedoids = (Settings.smart) ? smartSeeding(clustersNum) : randomSeeding(clustersNum);
        IntStream.range(0, usersNum).parallel().forEach(i -> 
                clustering[i] = findSimCluster(i, initialMedoids)
        );
        // Starting the iterations
        boolean converged = false;
        int iter = 0;
        while (!converged && iter < Settings.numberOfIterations) {
            converged = true;
            iter++;
            // Re-computing the centroids
            final ArrayList<ArrayList<Double>> medoids = findMedoids(clustering, clustersNum);
            converged = (IntStream.range(0, usersNum).parallel().mapToDouble(index -> {
                int old = clustering[index];
                clustering[index] = findSimCluster(index, medoids);
                return (old == clustering[index]) ? 0 : 1;
            }).sum() == 0);
            if (converged) {
                centroids = medoids;
            }
        }
        return clustering;
    }

}
