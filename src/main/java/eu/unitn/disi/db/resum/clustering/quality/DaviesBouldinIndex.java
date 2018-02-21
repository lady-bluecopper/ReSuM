package eu.unitn.disi.db.resum.clustering.quality;

import eu.unitn.disi.db.resum.distance.Distance;
import eu.unitn.disi.db.resum.distance.EuclideanDistance;
import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class DaviesBouldinIndex extends QualityMeasure {
    
    Double[][] centroids;
    Collection<Double>[] userVectors;
    Distance distance;

    public DaviesBouldinIndex(Collection<Double>[] userVectors, Double[][] centroids, Distance distance) {
        super(null);
        this.centroids = centroids;
        this.userVectors = userVectors;
        this.distance = distance;
    }
    
    private double[] computeAVGDistanceFromCentroids(int[] clustering) {
        int UsersNum = clustering.length;
        double[] variance = new double[centroids.length];
        int[] members = new int[centroids.length];
        for (int e = 0; e < UsersNum; e++) {
            int currCentroid = clustering[e];
            Double[] currentV = (Double[]) userVectors[e].toArray();
            variance[currCentroid] += distance.distance(centroids[currCentroid], currentV);
            members[currCentroid] += 1;
        }
        for (int c = 0; c < variance.length; c++) {
            if (members[c] > 0) {
                variance[c] /= members[c];
            }
        }
        return variance;
    }
    
    public double quality(int[] clustering, int clustersNum) {
        double[] avgDistances = computeAVGDistanceFromCentroids(clustering);
        double[] maxRatio = new double[clustersNum];
        for (int i = 0; i < clustersNum - 1; i ++) {
            for (int j = i + 1; j < clustersNum; j ++) {
                double currRatio = (avgDistances[i] + avgDistances[j]) / distance.distance(centroids[i], centroids[j]);
                maxRatio[i] = Math.max(maxRatio[i], currRatio);
                maxRatio[j] = Math.max(maxRatio[j], currRatio);
            }
        }
        double quality = 0;
        for (int i = 0; i < clustersNum; i ++) {
            quality += maxRatio[i];
        }
        return quality / clustersNum;
    }
    
}
