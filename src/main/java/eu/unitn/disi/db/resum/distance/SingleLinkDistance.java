package eu.unitn.disi.db.resum.distance;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author bluecopper
 */
public class SingleLinkDistance extends ClusterDistance {

    public SingleLinkDistance(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double distance(Collection<Integer> first, Collection<Integer> second) {
        if (first.size() > 0 && second.size() > 0) {
            double distance = Double.MAX_VALUE;
            for (int e1 : first) {
                for (int e2 : second) {
                    distance = Math.min(distance, adjacencyMatrix[e1][e2]);
                }
            }
            return distance;
        } else {
            return 0;
        }
    }

    public double[][] computeInterClusterDistanceMatrix(int[] clustering, int clustersNum) {
        double[][] distanceMatrix = new double[clustersNum][clustersNum];
        for (int i = 0; i < clustersNum; i++) {
            Arrays.fill(distanceMatrix[i], Double.MAX_VALUE);
        }
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] != clustering[j]) {
                    distanceMatrix[clustering[i]][clustering[j]] = Math.min(distanceMatrix[clustering[i]][clustering[j]], adjacencyMatrix[i][j]);
                    distanceMatrix[clustering[j]][clustering[i]] = Math.min(distanceMatrix[clustering[j]][clustering[i]], adjacencyMatrix[i][j]);
                }
            }
        }
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (Double.compare(Double.MAX_VALUE, distanceMatrix[i][j]) == 0) {
                    distanceMatrix[i][j] = 0;
                }
            }
        }
        return distanceMatrix;
    }

    public double computeMinInterClusterDistance(int[] clustering) {
        double minDistance = Double.MAX_VALUE;
        boolean oneCluster = true;
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] != clustering[j]) {
                    oneCluster = false;
                    System.out.println(adjacencyMatrix[i][j]);
                    minDistance = Math.min(minDistance, adjacencyMatrix[i][j]);
                }
            }
        }
        if (oneCluster) {
            return 0;
        }
        return minDistance;
    }

    public double[] computeIntraClusterDistanceVector(int[] clustering, int clustersNum) {
        double[] distanceMatrix = new double[clustersNum];
        Arrays.fill(distanceMatrix, Double.MAX_VALUE);
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    distanceMatrix[clustering[i]] = Math.min(distanceMatrix[clustering[i]], adjacencyMatrix[i][j]);
                }
            }
        }
        for (int i = 0; i < UsersNum - 1; i++) {
            if (Double.compare(Double.MAX_VALUE, distanceMatrix[i]) == 0) {
                distanceMatrix[i] = 0;
            }
        }
        return distanceMatrix;
    }

    public double computeMinIntraClusterDistance(int[] clustering) {
        double minDistance = Double.MAX_VALUE;
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    minDistance = Math.min(minDistance, adjacencyMatrix[i][j]);
                }
            }
        }
        if (Double.compare(Double.MAX_VALUE, minDistance) == 0) {
            return 0;
        }
        return minDistance;
    }

}
