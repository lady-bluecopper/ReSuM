package eu.unitn.disi.db.resum.clustering;

import eu.unitn.disi.db.resum.distance.Distance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public abstract class KClusterer extends Clusterer {

    protected ArrayList<ArrayList<Double>> centroids;

    protected KClusterer(ArrayList<ArrayList<Double>> relevanceMap, Distance distance) {
        super(relevanceMap, distance);
    }

    protected KClusterer(ArrayList<ArrayList<Double>> relevanceMap, int featuresNum, Distance distance) {
        super(relevanceMap, featuresNum, distance);
    }

    @Override
    public int[] findRandomClustering(int numClusters) {
        int[] clusters = new int[usersNum];
        ArrayList<ArrayList<Double>> clusterCentroids = randomSeeding(numClusters);
        IntStream.range(0, usersNum).parallel().forEach(index
                -> clusters[index] = findSimCluster(index, clusterCentroids));
        centroids = clusterCentroids;
        return clusters;
    }

    protected ArrayList<ArrayList<Double>> randomSeeding(int clustersNum) {
        ArrayList<ArrayList<Double>> clusterCentroids = new ArrayList<ArrayList<Double>>(clustersNum);
        HashSet<Integer> seeds = new HashSet<Integer>();
        Random rand = new Random(42);

        int firstSeed = rand.nextInt(usersNum);
        seeds.add(firstSeed);
        clusterCentroids.add(0, featureMap.get(firstSeed));

        while (seeds.size() < clustersNum) {
            int newCentroid = rand.nextInt(usersNum);
            if (seeds.add(newCentroid)) {
                clusterCentroids.add(seeds.size() - 1, featureMap.get(newCentroid));
            }
        }
        return clusterCentroids;
    }

    protected ArrayList<ArrayList<Double>> smartSeeding(int clustersNum) {
        ArrayList<ArrayList<Double>> clusterCentroids = new ArrayList<ArrayList<Double>>(clustersNum);
        HashSet<Integer> seeds = new HashSet<Integer>();

        int firstSeed = new Random().nextInt(usersNum);
        seeds.add(firstSeed);
        clusterCentroids.add(0, featureMap.get(firstSeed));

        while (seeds.size() < clustersNum) {
            int currSeed = selectWithProbability(computeNormalizedMinDistances(seeds));
            if (seeds.add(currSeed)) {
                clusterCentroids.add(seeds.size() - 1, featureMap.get(currSeed));
            }
        }
        return clusterCentroids;
    }

    protected int findSimCluster(int instance, final ArrayList<ArrayList<Double>> clusterCentroids) {
        double maxSim = -1;
        int bestCluster = - 1;
        for (int i = 0; i < clusterCentroids.size(); i++) {
            double sim = 1 - distance.distance(featureMap.get(instance), clusterCentroids.get(i));
            if (sim > maxSim) {
                maxSim = sim;
                bestCluster = i;
            }
        }
        return bestCluster;
    }

    protected ArrayList<ArrayList<Double>> findCentroids(int[] clustering, int clustersNum) {
        ArrayList<ArrayList<Double>> clusterCentroids = new ArrayList<ArrayList<Double>>(clustersNum);
        for (int c = 0; c < clustersNum; c++) {
            clusterCentroids.add(c, new ArrayList<Double>(Collections.nCopies(featuresNum, 0.)));
        }
        int[] clusterSizes = new int[clustersNum];
        int i, j;
        for (i = 0; i < usersNum; i++) {
            int cIdx = clustering[i];
            clusterSizes[cIdx]++;
            for (j = 0; j < featuresNum; j++) {
                Double a = featureMap.get(i).get(j);
                Double b = clusterCentroids.get(cIdx).get(j);
                clusterCentroids.get(cIdx).set(j, a + b);
            }
        }
        IntStream.range(0, clustersNum).parallel().forEach(index
                -> normalize(clusterCentroids.get(index), clusterSizes[index]));
        return clusterCentroids;
    }

    protected void normalize(ArrayList<Double> vector, int size) {
        vector.stream().forEach((el) -> {
            el /= size;
        });
    }

    protected int selectWithProbability(double[] P) {
        double p = new Random().nextDouble();
        double cumulativeProb = 0.0;
        for (int i = 0; i < P.length; i++) {
            cumulativeProb += P[i];
            if (p <= cumulativeProb) {
                return i;
            }
        }
        return 0;
    }

    protected double[] computeNormalizedMinDistances(HashSet<Integer> seeds) {
        double[] D = new double[usersNum];
        double sqSum = 0;
        for (int i = 0; i < usersNum; i++) {
            double minDist = Double.MAX_VALUE;
            for (int s : seeds) {
                double currD = distance.distance(featureMap.get(s), featureMap.get(i));
                minDist = Math.min(minDist, currD);
            }
            D[i] = Math.pow(minDist, 2);
            sqSum += D[i];
        }
        final double sqSumf = sqSum;
        IntStream.range(0, usersNum).parallel().forEach(index -> D[index] /= sqSumf);
        return D;
    }

}
