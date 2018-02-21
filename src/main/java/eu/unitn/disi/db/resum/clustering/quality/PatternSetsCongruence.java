package eu.unitn.disi.db.resum.clustering.quality;

/**
 *
 * @author bluecopper
 */
public class PatternSetsCongruence extends QualityMeasure {

    public PatternSetsCongruence(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double[] computeAvgDistances(int[] clustering, int clustersNum) {
        int UsersNum = clustering.length;
        double[] sumPairwiseDist = new double[clustersNum];
        int[] pairs = new int[clustersNum];
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    sumPairwiseDist[clustering[i]] += adjacencyMatrix[i][j];
                    pairs[i]++;
                }
            }
        }
        for (int i = 0; i < clustersNum; i++) {
            sumPairwiseDist[i] /= pairs[i];
        }
        return sumPairwiseDist;
    }

    public double[] computeMaxDistances(int[] clustering, int clustersNum) {
        int UsersNum = clustering.length;
        double[] maxDists = new double[clustersNum];
        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                if (clustering[i] == clustering[j]) {
                    maxDists[clustering[i]] = Math.max(maxDists[clustering[i]], adjacencyMatrix[i][j]);
                }
            }
        }
        return maxDists;
    }

    public double quality(int[] clustering, int clustersNum) {
        double[] maxDists = computeMaxDistances(clustering, clustersNum);
        double quality = 0;
        for (int i = 0; i < clustersNum; i++) {
            quality -= maxDists[i];
        }
        return quality / clustersNum;
    }

}
