package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import eu.unitn.disi.db.resum.distance.Distance;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public abstract class KClusterer extends Clusterer {

    protected HashIntObjMap<double[]> centroids;

    protected KClusterer(HashIntObjMap<double[]> relevanceMap, Distance distance) {
        super(relevanceMap, distance);
    }

    protected KClusterer(HashIntObjMap<double[]> relevanceMap, int featuresNum, Distance distance) {
        super(relevanceMap, featuresNum, distance);
    }

    @Override
    public int[] findRandomClustering(int numClusters) {
        int[] clusters = new int[usersNum];
        HashIntObjMap clusterCentroids = randomSeeding(numClusters);
        IntStream.range(0, usersNum).parallel().forEach(index
                -> clusters[index] = findSimCluster(index, clusterCentroids));
        centroids = clusterCentroids;
        return clusters;
    }

    protected HashIntObjMap<double[]> randomSeeding(int clustersNum) {
        HashIntObjMap<double[]> clusterCentroids = HashIntObjMaps.newMutableMap();
        HashSet<Integer> seeds = new HashSet<Integer>();
        Random rand = new Random(42);

        int firstSeed = rand.nextInt(usersNum);
        seeds.add(firstSeed);
        clusterCentroids.put(0, featureMap.get(firstSeed));

        while (seeds.size() < clustersNum) {
            int newCentroid = rand.nextInt(usersNum);
            if (seeds.add(newCentroid)) {
                clusterCentroids.put(seeds.size() - 1, featureMap.get(newCentroid));
            }
        }
        return clusterCentroids;
    }

    protected HashIntObjMap<double[]> smartSeeding(int clustersNum) {
        HashIntObjMap<double[]> clusterCentroids = HashIntObjMaps.newMutableMap();
        HashSet<Integer> seeds = new HashSet<Integer>();

        int firstSeed = new Random().nextInt(usersNum);
        seeds.add(firstSeed);
        clusterCentroids.put(0, featureMap.get(firstSeed));

        while (seeds.size() < clustersNum) {
            int currSeed = selectWithProbability(computeNormalizedMinDistances(seeds));
            if (seeds.add(currSeed)) {
                clusterCentroids.put(seeds.size() - 1, featureMap.get(currSeed));
            }
        }
        return clusterCentroids;
    }
 
    protected int findSimCluster(int instance, final HashIntObjMap<double[]> clusterCentroids) {
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

    protected HashIntObjMap<double[]> findCentroids(int[] clustering, int clustersNum) {
        HashIntObjMap<double[]> clusterCentroids = HashIntObjMaps.newMutableMap();
        IntStream.range(0, clustersNum).forEach(c -> clusterCentroids.put(c, new double[featuresNum]));
        int[] clusterSizes = new int[clustersNum];
        int i, j;
        for (i = 0; i < usersNum; i++) {
            int cIdx = clustering[i];
            clusterSizes[cIdx]++;
            for (j = 0; j < featuresNum; j++) {
                double[] curr = clusterCentroids.get(cIdx);
                curr[j] += featureMap.get(i)[j];
                clusterCentroids.put(j, curr);
            }
        }
        IntStream.range(0, clustersNum).parallel().forEach(index
                -> clusterCentroids.put(index, normalize(clusterCentroids.get(index), clusterSizes[index])));
        return clusterCentroids;
    }

    protected double[] normalize(double[] vector, int size) {
        IntStream.range(0, vector.length).forEach(el -> vector[el] /= size);
        return vector;
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
    
    public HashIntObjMap<double[]> getCentroids() {
        return centroids;
    }
    
    public void updateCentroid(int index, double[] newCentroid) {
        centroids.put(index, newCentroid);
    }
    
    public abstract int[] recomputeClustering(int clustersNum);

}
