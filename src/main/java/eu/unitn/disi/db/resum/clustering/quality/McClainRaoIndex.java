package eu.unitn.disi.db.resum.clustering.quality;

import eu.unitn.disi.db.resum.distance.ClusterDistance;
import eu.unitn.disi.db.resum.distance.CompleteLinkDistance;

/**
 *
 * @author bluecopper
 */
public class McClainRaoIndex extends QualityMeasure {
    
    ClusterDistance clusterDistance;

    public McClainRaoIndex(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
        clusterDistance = new CompleteLinkDistance(adjacencyMatrix);
    }

    public double quality(int[] clustering, int clustersNum) {
        double SW = 0;
        double SB = 0;
        int NW = 0;
        int NB = 0;
        int UsersNum = clustering.length;
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    SW += adjacencyMatrix[i][j];
                }
            }
        }
        double[][] BC = clusterDistance.computeInterClusterDistanceMatrix(clustering, clustersNum);
        for (int i = 0; i < clustersNum - 1; i++) {
            for (int j = i + 1; j < clustersNum; j++) {
                SB += BC[i][j];
            }
        }
        int[] NWcounts = new int[clustersNum];
        for (int i = 0; i < UsersNum; i++) {
            NWcounts[clustering[i]]++;
        }
        for (int i = 0; i < clustersNum; i++) {
            NW += (NWcounts[i] * (NWcounts[i] - 1) / 2);
        }
        NB = (UsersNum * (UsersNum - 1) / 2) - NW;
        if (NW > 0 && NB > 0) {
            return (NB * SW) / (NW * SB);
        }
        return 0;
    }

}
