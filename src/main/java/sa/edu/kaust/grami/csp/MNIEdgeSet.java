package sa.edu.kaust.grami.csp;

import java.util.Iterator;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.utilities.Settings;
import sa.edu.kaust.grami.dataStructures.WeightedEdge;
import org.apache.commons.collections4.list.TreeList;

/**
 *
 * @author bluecopper
 */
public class MNIEdgeSet {

    private TreeList<WeightedEdge<Integer, Double, Double>>[] edgeSets;
    private final int ID;

    public MNIEdgeSet(int ID) {
        this.ID = ID;
        this.edgeSets = new TreeList[Settings.actualNumOfEdgeWeights];
        for (int i = 0; i < edgeSets.length; i++) {
            edgeSets[i] = new TreeList<WeightedEdge<Integer, Double, Double>>();
        }
    }

    public TreeList<WeightedEdge<Integer, Double, Double>> getEdgeSet(int index) {
        return edgeSets[index];
    }

    public TreeList<WeightedEdge<Integer, Double, Double>>[] getEdgeSets() {
        return edgeSets;
    }

    public double[] getTopKWeights(int user, int k) {
        Iterator<WeightedEdge<Integer, Double, Double>> edgeIterator = edgeSets[user].iterator();
        double[] weights = new double[k];
        int i = 0; 
        while (i != k && edgeIterator.hasNext()) {
            weights[i] = edgeIterator.next().getEdgeWeight();
            i ++;
        }
        if (i != k) {
            System.err.println(">>>>> " + edgeSets[user].size() + " U " + user + " - " + i + " Should not happen!!");
        }
        return weights;
    }

    public int getID() {
        return ID;
    }

    public void setEdgeSets(TreeList<WeightedEdge<Integer, Double, Double>>[] edgeSet) {
        this.edgeSets = edgeSet;
    }

    public void addEdge(MultiUserWeightedEdge<Integer, Double, double[]> edge) {
        double[] edgeweights = edge.getMaxWeights();
        for (int i = 0; i < edgeweights.length; i++) {
            edgeSets[i].add(new WeightedEdge(edge.getNodeID(), edge.getEdgeLabel(), edgeweights[i]));
        }
    }

    public int numDistinctEdges() {
        return edgeSets[0].size();
    }

}
