package eu.unitn.disi.db.resum.clustering.quality;

import java.util.stream.IntStream;

/**
 *
 * @author bluecopper
 */
public class SilhouetteCoefficient extends QualityMeasure {
    
    public SilhouetteCoefficient(final Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double quality(final int[] clustering, int clustersNum) {
        final int usersNum = clustering.length;
        int realClustersNum = 0;
        double[] IntraD = new double[usersNum];
        double[][] InterD = new double[usersNum][clustersNum];
        double[] MINAVGdistanceFromCluster = new double[usersNum];
        int[] clustersSize = new int[clustersNum];
        int[] realIntraPairs = new int[usersNum];
        int[][] realInterPairs = new int[usersNum][clustersNum];
        int[] realUserNum = new int[clustersNum];
        double[] userSilhouette = new double[usersNum];
        double[] clusterSilhuette = new double[clustersNum];
        double silhouette = 0;
        
        IntStream.range(0, usersNum).parallel().forEach(id -> {
            clustersSize[clustering[id]] ++;
            for (int j = id + 1; j < usersNum; j++) {
                double dist = adjacencyMatrix[id][j];
                if (dist > -1) {
                    if (clustering[id] == clustering[j]) {
                        realIntraPairs[id] ++;
                        realIntraPairs[j] ++;
                        IntraD[id] += dist;
                        IntraD[j] += dist;
                    } else {
                        realInterPairs[id][clustering[j]] ++;
                        realInterPairs[j][clustering[id]] ++;
                        InterD[id][clustering[j]] += dist;
                        InterD[j][clustering[id]] += dist;
                    }
                }
            } 
        });
        IntStream.range(0, usersNum).parallel().forEach(id -> {
            IntraD[id] /= realIntraPairs[id];
            MINAVGdistanceFromCluster[id] = Double.MAX_VALUE;
            for (int j = 0; j < clustersNum; j++) {
                if (clustersSize[j] > 0 && j != clustering[id]) {
                    MINAVGdistanceFromCluster[id] = Math.min(MINAVGdistanceFromCluster[id], (realInterPairs[id][j] > 0) ? InterD[id][j] / realInterPairs[id][j] : InterD[id][j]);
                }
            }
            if (Math.max(MINAVGdistanceFromCluster[id], IntraD[id]) > 0) {
                userSilhouette[id] = (MINAVGdistanceFromCluster[id] - IntraD[id]) / (Math.max(MINAVGdistanceFromCluster[id], IntraD[id]));
                realUserNum[clustering[id]] ++;
            }
        });
        IntStream.range(0, usersNum).parallel().forEach(id -> {
            clusterSilhuette[clustering[id]] += userSilhouette[id];
        });
        for (int i = 0; i < clustersNum; i ++) {
            if (realUserNum[i] > 0) {
                clusterSilhuette[i] /= realUserNum[i];
                realClustersNum ++;
            }
        }
        for (int i = 0; i < clustersNum; i ++) {
            silhouette += clusterSilhuette[i];
        }
        if (realClustersNum > 0) {
            return silhouette / realClustersNum;
        }
        return silhouette;
    }
    
}
