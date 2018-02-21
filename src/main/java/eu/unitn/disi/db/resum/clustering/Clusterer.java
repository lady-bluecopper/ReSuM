package eu.unitn.disi.db.resum.clustering;

import eu.unitn.disi.db.resum.clustering.quality.DunnIndex;
import eu.unitn.disi.db.resum.clustering.quality.QualityMeasure;
import eu.unitn.disi.db.resum.clustering.quality.SilhouetteCoefficient;
import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public abstract class Clusterer {
    
    protected ArrayList<ArrayList<Double>> featureMap;
    
    protected final int usersNum;
    
    protected int featuresNum;
    
    protected Distance distance;
    
    protected final QualityMeasure qualityMeasure;
    
    
    protected Clusterer(ArrayList<ArrayList<Double>> relevanceMap, Distance distance) {
        this.featureMap = relevanceMap;
        this.usersNum = Settings.numberOfFunctions;
        this.featuresNum = relevanceMap.get(0).size();
        this.distance = distance;
        this.qualityMeasure = new SilhouetteCoefficient(createAdjacencyMatrix());
    }
    
    protected Clusterer(ArrayList<ArrayList<Double>> patternSets, int patternsNum, Distance distance) {
        this.featureMap = patternSets;
        this.usersNum = Settings.numberOfFunctions;
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
        Double[][] adjacencyMatrix = new Double[Settings.numberOfFunctions][Settings.numberOfFunctions];
        IntStream.range(0, Settings.numberOfFunctions - 1).parallel().forEach(index -> {
            IntStream.range(index + 1, Settings.numberOfFunctions).parallel().forEach(index2 -> {
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
    
}
