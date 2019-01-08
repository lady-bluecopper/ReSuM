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
package sa.edu.kaust.grami.dataStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import sa.edu.kaust.grami.dijkstra.DijkstraEngine;
import java.util.Arrays;
import eu.unitn.disi.db.resum.utilities.Settings;

public class GNode {

    private int ID;
    private int label;

    private HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> reachableNodes; // Label~<nodeID,edge_label, edge_weight>, outgoing nodes
    private HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> reachedBYNodes; // Label~<nodeID,edge_label, edge_weight>, ingoing nodes

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
        if ((reachableNodes == null) || (reachableNodes.get(label) == null)) {
            return 0;
        }
        return reachableNodes.get(label).size();
    }

    public int getinDegree(int label) {
        if ((reachedBYNodes == null) || (reachedBYNodes.get(label) == null)) {
            return 0;
        }
        return reachedBYNodes.get(label).size();
    }

    public int getID() {
        return ID;
    }

    public int getLabel() {
        return label;
    }
    
    public boolean nodeAlreadyAdded(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> edgeList, int nodeID) {
        return (edgeList.stream().anyMatch((edge) -> (edge.getNodeID() == nodeID)));
    }

    public void addreachableNode(GNode node, double edgeLabel, double[] maxEdgeWeight) {
        if (reachableNodes == null) {
            reachableNodes = new HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        reachableNodes.putIfAbsent(node.getLabel(), new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>());
        if (!nodeAlreadyAdded(reachableNodes.get(node.getLabel()), node.getID())) {
//        if (!reachableNodes.get(node.getLabel()).contains(node.getID())) {
            reachableNodes.get(node.getLabel()).add(new MultiUserWeightedEdge<Integer, Double, double[]>(node.getID(), edgeLabel, maxEdgeWeight));
        }
        node.addreachedBYNodes(this, edgeLabel, maxEdgeWeight);

    }

    public void addreachableNode(GNode node, double edgeLabel) {
        if (reachableNodes == null) {
            reachableNodes = new HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        double[] newWeights = new double[Settings.actualNumOfEdgeWeights];
        Arrays.fill(newWeights, 1.0);
        
        reachableNodes.putIfAbsent(node.getLabel(), new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>());
        if (!nodeAlreadyAdded(reachableNodes.get(node.getLabel()), node.getID())) {
//        if (!reachableNodes.get(node.getLabel()).contains(node.getID())) {
            reachableNodes.get(node.getLabel()).add(new MultiUserWeightedEdge<Integer, Double, double[]>(node.getID(), edgeLabel, newWeights));
        }
        node.addreachedBYNodes(this, edgeLabel, newWeights);
    }

    private void addreachedBYNodes(GNode node, double edgeLabel, double[] maxEdgeWeight) {
        if (reachedBYNodes == null) {
            reachedBYNodes = new HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
        }
        reachedBYNodes.putIfAbsent(node.getLabel(), new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>());
        if (!nodeAlreadyAdded(reachedBYNodes.get(node.getLabel()), node.getID())) {
//        if (!reachedBYNodes.get(node.getLabel()).contains(node.getID())) {
            reachedBYNodes.get(node.getLabel()).add(new MultiUserWeightedEdge<Integer, Double, double[]>(node.getID(), edgeLabel, maxEdgeWeight));
        }
    }
    
    public void printOutReachableNodes() {
        if (reachableNodes == null) {
            return;
        }
        for (ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> arr : reachableNodes.values()) {
            for (int i = 0; i < arr.size(); i++) {
                System.out.println("Node: " + ID + " is within reach of Node " + arr.get(i));
            }
        }
    }

    public void setReachableNodes(DijkstraEngine dj, HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel, HPListGraph graph) {
        for (Entry<Integer, HashMap<Integer, GNode>> ar : freqNodesByLabel.entrySet()) {
            HashMap<Integer, GNode> tmp = ar.getValue();

            for (GNode node : tmp.values()) {
                if (ID == node.getID()) {
                    continue;
                }
                double dist = dj.getShortestDistance(node.getID());
                if (dist != Double.MAX_VALUE) {
                    if (graph.getEdgeLabel(ID, node.getID()) != null) {
                        double edgeLabel = (Double) graph.getEdgeLabel(ID, node.getID());
                        double[] maxEdgeWeight = graph.getMaxEdgeWeights(ID, node.getID());
                        addreachableNode(node, edgeLabel, maxEdgeWeight);
                    } else {
                        addreachableNode(node, 1);
                    }
                }
            }
        }
    }

    /**
     * a fast set reachable function
     *
     * @param graph
     * @param NodesByLabel
     */
    public void setReachableNodes_1hop(Graph graph, HashMap<Integer, HashMap<Integer, GNode>> NodesByLabel) {
        // get edge for each node
        IntIterator it = graph.getListGraph().getOutEdgeIndices(getID());
        for (; it.hasNext();) {
            int edge = it.next();
            GNode otherNode = graph.getNode(graph.getListGraph().getOtherNode(edge, getID()));
            if (NodesByLabel.containsKey(otherNode.getLabel())) {
                addreachableNode(otherNode, graph.getListGraph().getEdgeLabel(getID(), otherNode.getID()),
                        graph.getListGraph().getMaxEdgeWeights(getID(), otherNode.getID()));
            }
        }
    }

    public boolean hasReachableNodes() {
        return (reachableNodes != null);
    }

    public boolean isWithinTheRangeOf(int NodeIndex, int nodeLabel) {
        if (reachableNodes.get(nodeLabel) == null) {
            return false;
        }
        return reachableNodes.get(nodeLabel).contains(NodeIndex);
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getRechableWithEdgeLabel(double edgeLabel) {
        if (reachableNodes == null) {
            return new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        }

        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr = 
                new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        for (Entry<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> entry : reachableNodes.entrySet()) {
            tempArr.addAll(entry.getValue());
        }
        for (int j = 0; j < tempArr.size(); j++) {
            MultiUserWeightedEdge<Integer, Double, double[]> mp = tempArr.get(j);
            if (mp.getEdgeLabel() != edgeLabel) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getRechableWithNodeIDs(int label, double edgeLabel) {
        if (reachableNodes == null) {
            return new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        }

        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        tempArr.addAll(reachableNodes.get(label));
        for (int j = 0; j < tempArr.size(); j++) {
            MultiUserWeightedEdge<Integer, Double, double[]> mp = tempArr.get(j);
            if (mp.getEdgeLabel() != edgeLabel) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }

    public HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> getReachableWithNodes() {
        return reachableNodes;
    }

    public HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> getReachableByNodes() {
        return reachedBYNodes;
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getRechableByNodeIDs(int label, double edgeLabel) {
        if (reachedBYNodes == null) {
            return new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        }

        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr 
                = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        tempArr.addAll(reachedBYNodes.get(label));
        for (int j = 0; j < tempArr.size(); j++) {
            MultiUserWeightedEdge<Integer, Double, double[]> mp = tempArr.get(j);
            if (!(mp.getEdgeLabel().equals(edgeLabel))) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }
}
