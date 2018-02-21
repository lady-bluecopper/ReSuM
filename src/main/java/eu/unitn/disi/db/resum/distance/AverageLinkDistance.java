package eu.unitn.disi.db.resum.distance;

import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class AverageLinkDistance extends ClusterDistance {
    
    public AverageLinkDistance(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double distance(Collection<Integer> first, Collection<Integer> second) {
        double distance = 0;
        int pairs = 0;
        for (int e1 : first) {
            for (int e2 : second) {
                distance = Math.max(distance, adjacencyMatrix[e1][e2]);
                pairs ++;
            }
        }
        return distance / pairs;
    }

    public double[][] computeInterClusterDistanceMatrix(int[] clustering, int clustersNum) {
        double[][] distanceMatrix = new double[clustersNum][clustersNum];
        int[][] pairs = new int[clustersNum][clustersNum];
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] != clustering[j]) {
                    pairs[clustering[i]][clustering[j]] ++;
                    pairs[clustering[j]][clustering[i]] ++;
                    distanceMatrix[clustering[i]][clustering[j]] += adjacencyMatrix[i][j];
                    distanceMatrix[clustering[j]][clustering[i]] += adjacencyMatrix[i][j];
                }
            }
        }
        for (int i = 0; i < clustersNum - 1; i ++) {
            for (int j = i + 1; j < clustersNum; j ++) {
                distanceMatrix[i][j] /= pairs[i][j];
                distanceMatrix[j][i] /= pairs[j][i];
            }
        }
        return distanceMatrix;
    }

    public double[] computeIntraClusterDistanceVector(int[] clustering, int clustersNum) {
        double[] distanceMatrix = new double[clustersNum];
        int[] pairs = new int[clustersNum];
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    pairs[clustering[i]] ++;
                    distanceMatrix[clustering[i]] += adjacencyMatrix[i][j];
                }
            }
        }
        for (int i = 0; i < clustersNum - 1; i ++) {
                distanceMatrix[i] /= pairs[i];
        }
        return distanceMatrix;
    }

}
