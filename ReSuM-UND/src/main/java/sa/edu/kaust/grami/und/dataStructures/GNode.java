/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid
 *
 * This file is part of Grami.
 *
 * Grami is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Grami is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */
package sa.edu.kaust.grami.und.dataStructures;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Arrays;
import eu.unitn.disi.db.resum.und.utilities.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.und.utilities.Settings;

public class GNode {

    private int ID;
    private int label;
    private HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> reachableNodes; //represented by Label~nodeID, represents the outgoing nodes -> Pair<NodeID, EdgeLabel>

    @Override
    public String toString() {

        String format = "(" + ID + ":" + label + ")";
        return format;
    }

    public GNode(int ID, int label) {
        this.ID = ID;
        this.label = label;
    }

    public int getOutDegree(int label) {
        if (reachableNodes == null) {
            return 0;
        }
        if (reachableNodes.get(label) == null) {
            return 0;
        }
        return reachableNodes.get(label).size();
    }

    public int getID() {
        return ID;
    }

    public int getLabel() {
        return label;
    }

    public void addreachableNode(GNode node, HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel, double edgeLabel, double[] maxEdgeWeight) {
        if (reachableNodes == null) {
            reachableNodes = new HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        int reachableLabel = node.getLabel();
        if (freqNodesByLabel == null || freqNodesByLabel.containsKey(reachableLabel)) {
            reachableNodes.putIfAbsent(reachableLabel, new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>());
            if (!reachableNodes.get(reachableLabel).contains(node.getID())) {
                reachableNodes.get(reachableLabel).add(new MultiUserWeightedEdge<Integer, Double, double[]>(node.getID(), edgeLabel, maxEdgeWeight));
            }
        }
    }

    public void addreachableNode(GNode node, HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel, double edgeLabel) {
        if (reachableNodes == null) {
            reachableNodes = new HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        int reachableLabel = node.getLabel();
        if (freqNodesByLabel == null || freqNodesByLabel.containsKey(reachableLabel)) {
            reachableNodes.putIfAbsent(reachableLabel, new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>());
            if (!reachableNodes.get(reachableLabel).contains(node.getID())) {
                double[] dummyWeights = new double[Settings.actualNumOfEdgeWeights];
                reachableNodes.get(reachableLabel).add(new MultiUserWeightedEdge<Integer, Double, double[]>(node.getID(), edgeLabel, dummyWeights));
            }
        }
    }

    public void printOutReachableNodes() {
        if (reachableNodes == null) {
            return;
        }
        reachableNodes.values().stream().forEach((arr) -> {
            for (int i = 0; i < arr.size(); i++) {
                System.out.println("Node: " + ID + " is within reach of Node " + arr.get(i));
            }
        });
    }

    /**
     * a fast set reachable function
     *
     * @param graph
     * @param freqNodesByLabel
     */
    public void setReachableNodes_1hop(Graph graph, HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel) {
        //get edge for each node
        IntIterator it = graph.getListGraph().getEdgeIndices(getID());
        for (; it.hasNext();) {
            int edge = it.next();
            GNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
            int otherLabel = otherNode.getLabel();
            if (freqNodesByLabel.containsKey(otherLabel)) {
                if (graph.getListGraph().getEdgeLabel(edge) != null) {
                    double edgeLabel = graph.getListGraph().getEdgeLabel(edge);
                    double[] edgeWeights = graph.getListGraph().getMaxEdgeWeights(edge);
                    addreachableNode(otherNode, freqNodesByLabel, edgeLabel, edgeWeights);
                } else {
                    addreachableNode(otherNode, freqNodesByLabel, 1);
                }
            }
        }
    }

    public boolean hasReachableNodes() {
        return (reachableNodes != null);
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getRechableWithNodeIDs(int label, double edgeLabel) {
        if (reachableNodes == null) {
            return new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        }
        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr 
                = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        tempArr.addAll(reachableNodes.get(label));
        for (int j = 0; j < tempArr.size(); j++) {
            MultiUserWeightedEdge<Integer, Double, double[]> mp = tempArr.get(j);
            if (!mp.getEdgeLabel().equals(edgeLabel)) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }

    public HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> getReachableWithNodes() {
        return reachableNodes;
    }
}
