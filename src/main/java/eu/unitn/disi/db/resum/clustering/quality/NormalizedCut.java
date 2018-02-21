package eu.unitn.disi.db.resum.clustering.quality;

/**
 *
 * @author bluecopper
 */
public class NormalizedCut extends QualityMeasure {

    public NormalizedCut(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double quality(int[] clustering, int clustersNum) {
        int UsersNum = clustering.length;
        double[] cutcost = new double[clustersNum];
        double[] intracost = new double[clustersNum];
        double cost = 0;

        for (int i = 0; i < UsersNum - 1; i++) {
            for (int j = i + 1; j < UsersNum; j++) {
                int first = clustering[i];
                int second = clustering[j];
                if (first == second) {
                    intracost[first] += adjacencyMatrix[i][j];
                } else {
                    cutcost[first] += adjacencyMatrix[i][j];
                    cutcost[second] += adjacencyMatrix[i][j];
                }
            }
        }
        for (int i = 0; i < clustersNum; i++) {
            if (intracost[i] > 0) {
                cost += cutcost[i] / intracost[i];
            }
        }
        return cost;
    }

}
