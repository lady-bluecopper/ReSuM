package sa.edu.kaust.grami.und.csp;

import eu.unitn.disi.db.resum.und.utilities.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.und.utilities.Settings;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author bluecopper
 */
public class MNIEdgeSet {

    private ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> edgeSets;

    public MNIEdgeSet() {
        this.edgeSets = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
    }
    
    public MNIEdgeSet(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> edgeSets) {
        this.edgeSets = edgeSets;
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getEdgeSets() {
        return edgeSets;
    }

    public double[] getSumsofTopKWeights(int k) {
        Queue<Double>[] topKWeights = new PriorityQueue[Settings.actualNumOfEdgeWeights];
        for (int u = 0; u < Settings.actualNumOfEdgeWeights; u++) {
            topKWeights[u] = new PriorityQueue<Double>();
        }
        edgeSets.stream().forEach(edge -> {
            double[] edgeWeights = edge.getMaxWeights();
            for (int u = 0; u < Settings.actualNumOfEdgeWeights; u++) {
                if (topKWeights[u].size() < k) {
                    topKWeights[u].add(edgeWeights[u]);
                } else if (topKWeights[u].peek() < edgeWeights[u]) {
                    topKWeights[u].poll();
                    topKWeights[u].add(edgeWeights[u]);
                }
            }
        });
        double[] thisDomainSums = new double[Settings.actualNumOfEdgeWeights];
            for (int u = 0; u < Settings.actualNumOfEdgeWeights; u++) {
                thisDomainSums[u] = topKWeights[u].parallelStream().mapToDouble(Double::doubleValue).sum();
            }
        return thisDomainSums;
    }

    public void setEdgeSets(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> edgeSet) {
        this.edgeSets = edgeSet;
    }

    public void addEdge(MultiUserWeightedEdge<Integer, Double, double[]> edge) {
        edgeSets.add(edge);
    }

    public void addAllEdges(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> otherList) {
        edgeSets.addAll(otherList);
    }
}
