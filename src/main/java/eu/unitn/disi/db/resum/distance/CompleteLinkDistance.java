package eu.unitn.disi.db.resum.distance;

import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class CompleteLinkDistance extends ClusterDistance {

    public CompleteLinkDistance(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double distance(Collection<Integer> first, Collection<Integer> second) {
        double distance = 0;
        for (int e1 : first) {
            for (int e2 : second) {
                distance = Math.max(distance, adjacencyMatrix[e1][e2]);
            }
        }
        return distance;
    }

    public double[][] computeInterClusterDistanceMatrix(int[] clustering, int clustersNum) {
        double[][] distanceMatrix = new double[clustersNum][clustersNum];
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] != clustering[j]) {
                    distanceMatrix[clustering[i]][clustering[j]] = Math.max(distanceMatrix[clustering[i]][clustering[j]], adjacencyMatrix[i][j]);
                    distanceMatrix[clustering[j]][clustering[i]] = Math.max(distanceMatrix[clustering[j]][clustering[i]], adjacencyMatrix[i][j]);
                }
            }
        }
        return distanceMatrix;
    }

    public double[] computeIntraClusterDistanceVector(int[] clustering, int clustersNum) {
        double[] distanceMatrix = new double[clustersNum];
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    distanceMatrix[clustering[i]] = Math.max(distanceMatrix[clustering[i]], adjacencyMatrix[i][j]);
                }
            }
        }
        return distanceMatrix;
    }

    public double computeMaxInterClusterDistance(int[] clustering) {
        double maxDistance = 0;
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] != clustering[j]) {
                    maxDistance = Math.max(maxDistance, adjacencyMatrix[i][j]);
                }
            }
        }
        return maxDistance;
    }

    public double computeMaxIntraClusterDistance(int[] clustering) {
        double maxDistance = 0;
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    maxDistance = Math.max(maxDistance, adjacencyMatrix[i][j]);
                }
            }
        }
        return maxDistance;
    }

}
