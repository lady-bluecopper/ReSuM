package eu.unitn.disi.db.resum.clustering;

import com.koloboke.collect.map.hash.HashIntObjMap;
import eu.unitn.disi.db.resum.clustering.quality.DunnIndex;
import eu.unitn.disi.db.resum.clustering.quality.QualityMeasure;
import eu.unitn.disi.db.resum.clustering.quality.SilhouetteCoefficient;
import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public abstract class Clusterer {
    
    protected HashIntObjMap<double[]> featureMap;
    
    protected final int usersNum;
    
    protected int featuresNum;
    
    protected Distance distance;
    
    protected final QualityMeasure qualityMeasure;
    
    
    protected Clusterer(HashIntObjMap<double[]> featureMap, Distance distance) {
        this.featureMap = featureMap;
        this.usersNum = Settings.numberOfEdgeWeights;
        this.featuresNum = (featureMap.get(0)).length;
        this.distance = distance;
        this.qualityMeasure = new SilhouetteCoefficient(createAdjacencyMatrix());
    }
    
    protected Clusterer(HashIntObjMap<double[]> patternSets, int patternsNum, Distance distance) {
        this.featureMap = patternSets;
        this.usersNum = Settings.numberOfEdgeWeights;
        this.featuresNum = patternsNum;
        this.distance = distance;
        this.qualityMeasure = new DunnIndex(createAdjacencyMatrix());
    }
    
    public int[] findRandomClustering(int numClusters) {
        int[] clusters = new int[usersNum];
        Random rand = new Random();
        IntStream.range(0, usersNum).parallel().forEach(i -> 
            clusters[i] = rand.nextInt(numClusters));
        return clusters;
    }
    
    public int[] findBestClustering(int maxK) {
        double maxQuality = -1;
        int bestK = -1;
        int[] bestClustering = new int[usersNum];

        if (maxK > 1) {
            for (int k = 2; k <= maxK; k++) {
                int[] currClustering = findClustering(k);
                double currQuality = qualityMeasure.quality(currClustering, k);
                if (currQuality > maxQuality) {
                    maxQuality = currQuality;
                    bestClustering = currClustering;
                    bestK = k;
                }
                if (currQuality <= maxQuality) {
                    break;
                }
            }
        }
        else {
            System.err.println("[WARN] Max K should be greater than 2.");
        }
        System.out.println("Best clustering is: " + bestK + " with quality: " + maxQuality);
        return bestClustering;
    }
    
    protected Double[][] createAdjacencyMatrix() {
        Double[][] adjacencyMatrix = new Double[Settings.numberOfEdgeWeights][Settings.numberOfEdgeWeights];
        IntStream.range(0, Settings.numberOfEdgeWeights - 1).parallel().forEach(index -> {
            IntStream.range(index + 1, Settings.numberOfEdgeWeights).parallel().forEach(index2 -> {
                double dist = distance.distance(featureMap.get(index), featureMap.get(index2));
                adjacencyMatrix[index][index2] = dist;
                
            });
        });
        return adjacencyMatrix;
    }
    
    public double computeClusteringQuality(int[] clustering, int clustersNum) {
        return qualityMeasure.quality(clustering, clustersNum);
    }
    
    public abstract int[] findClustering(int clustersNum);
    
    public void updateFeatureMap(double[] newVector) {
        featureMap.put(featureMap.size(), newVector);
    }
    
    public HashIntObjMap<double[]> getFeatureMap() {
        return featureMap;
    }
    
    public Distance getDistMeasure() {
        return distance;
    }
    
}
