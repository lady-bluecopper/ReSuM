package eu.unitn.disi.db.resum.clustering.quality;

/**
 *
 * @author bluecopper
 */
public class WeightedModularity extends QualityMeasure {
    
    public WeightedModularity(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double quality(int[] clustering, int clustersNum) {
        int UsersNum = clustering.length;
        double[] simSum = new double[UsersNum];
        double totalSum = 0;
        double weightedModularity = 0;
        
        for (int i = 0; i < UsersNum; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                simSum[i] += adjacencyMatrix[i][j];
                simSum[j] += adjacencyMatrix[i][j];
                totalSum += 2 * adjacencyMatrix[i][j];
            }
        }
        for (int i = 0; i < UsersNum; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                weightedModularity += (clustering[i] == clustering[j] ? 1 : 0) * (adjacencyMatrix[i][j] - ((simSum[i] * simSum[j]) / (2 * totalSum)));
            }
        }
        return weightedModularity / totalSum;
    }
    
}
