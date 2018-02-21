package eu.unitn.disi.db.resum.clustering.quality;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author bluecopper
 */
public class CIndex extends QualityMeasure {

    public CIndex(Double[][] adjacencyMatrix) {
        super(adjacencyMatrix);
    }

    public double quality(int[] clustering, int clustersNum) {
        double SW = 0;
        double SMi = 0;
        double SMa = 0;
        int NW = 0;
        int UsersNum = clustering.length;
        int[] NWcounts = new int[clustersNum];
        for (int i = 0; i < UsersNum; i++) {
            NWcounts[clustering[i]]++;
        }
        for (int i = 0; i < clustersNum; i++) {
            NW += (NWcounts[i] * (NWcounts[i] - 1) / 2);
        }
        PriorityQueue<Double> SMax = new PriorityQueue<Double>();
        PriorityQueue<Double> SMin = new PriorityQueue<Double>(new Comparator<Double>() {
            public int compare(Double lhs, Double rhs) {
                return -1 * lhs.compareTo(rhs);
            }
        });
        if (NW > 0) {
            for (int i = 0; i < UsersNum - 1; i++) {
                for (int j = i + 1; j < UsersNum; j++) {
                    if (clustering[i] == clustering[j]) {
                        SW += adjacencyMatrix[i][j];
                    }
                    if (SMax.size() == NW) {
                        if (SMax.peek() < adjacencyMatrix[i][j]) {
                            SMax.poll();
                            SMax.add(adjacencyMatrix[i][j]);
                        }
                    } else {
                        SMax.add(adjacencyMatrix[i][j]);
                    }
                    if (SMin.size() == NW) {
                        if (SMin.peek() > adjacencyMatrix[i][j]) {
                            SMin.poll();
                            SMin.add(adjacencyMatrix[i][j]);
                        }
                    } else {
                        SMin.add(adjacencyMatrix[i][j]);
                    }
                }
            }
            while (!SMax.isEmpty()) {
                SMa += SMax.poll();
                SMi += SMin.poll();
            }
            if (SMa - SMi > 0) {
                return (SW - SMi) / (SMa - SMi);
            }
        }
        return 0;
    }

}
