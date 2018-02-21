/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa.edu.kaust.grami.dataStructures;

import sa.edu.kaust.grami.dijkstra.DijkstraEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import eu.unitn.disi.db.resum.utilities.MyPair;

/**
 *
 * @author bluecopper
 */
public class UNode implements Node<Integer, Double> {

    private int ID;
    private int label;

    private HashMap<Integer, ArrayList<MyPair<Integer, Double>>> reachableNodes; // Label~<nodeID,edge_label, edge_weight>, outgoing nodes
    private HashMap<Integer, ArrayList<MyPair<Integer, Double>>> reachedBYNodes; // Label~<nodeID,edge_label, edge_weight>, ingoing nodes

    @Override
    public String toString() {
        String format = "(" + ID + ":" + label + ")";
        return format;
    }

    public UNode(int ID, int label) {
        this.ID = ID;
        this.label = label;
    }

    public int getOutDegree(Integer label) {
        if ((reachableNodes == null) || (reachableNodes.get(label) == null)) {
            return 0;
        }
        return reachableNodes.get(label).size();
    }

    public int getInDegree(Integer label) {
        if ((reachedBYNodes == null) || (reachedBYNodes.get(label) == null)) {
            return 0;
        }
        return reachedBYNodes.get(label).size();
    }

    public int getID() {
        return ID;
    }

    public Integer getLabel() {
        return label;
    }
    
    public void setLabel(Integer label) {
        this.label = label;
    }

    public void addreachableNode(UNode node, double edgeLabel) {
        if (reachableNodes == null) {
            reachableNodes = new HashMap<Integer, ArrayList<MyPair<Integer, Double>>>();
        }

        ArrayList<MyPair<Integer, Double>> list = reachableNodes.get(node.getLabel());
        if (list == null) {
            list = new ArrayList<MyPair<Integer, Double>>();
            reachableNodes.put(node.getLabel(), list);
        }
        if (!list.contains(node.getID())) {
            list.add(new MyPair<Integer, Double>(node.getID(), edgeLabel));
        }
        node.addreachedBYNodes(this, edgeLabel);

    }

    private void addreachedBYNodes(UNode node, double edgeLabel) {
        if (reachedBYNodes == null) {
            reachedBYNodes = new HashMap<Integer, ArrayList<MyPair<Integer, Double>>>();
        }
        ArrayList<MyPair<Integer, Double>> list = reachedBYNodes.get(node.getLabel());
        if (list == null) {
            list = new ArrayList<MyPair<Integer, Double>>();
            reachedBYNodes.put(node.getLabel(), list);
        }
        if (!list.contains(node.getID())) {
            list.add(new MyPair<Integer, Double>(node.getID(), edgeLabel));
        }
    }

    public void printOutReachableNodes() {
        if (reachableNodes == null) {
            return;
        }
        for (ArrayList<MyPair<Integer, Double>> arr : reachableNodes.values()) {
            for (int i = 0; i < arr.size(); i++) {
                System.out.println("Node: " + ID + " is within reach of Node " + arr.get(i));
            }
        }
    }

    public void setReachableNodes(DijkstraEngine dj, HashMap<Integer, HashMap<Integer, Node>> freqNodesByLabel, HPListGraph graph) {
        for (Map.Entry<Integer, HashMap<Integer, Node>> ar : freqNodesByLabel.entrySet()) {
            HashMap<Integer, Node> tmp = ar.getValue();

            for (Node node : tmp.values()) {
                if (ID == node.getID()) {
                    continue;
                }
                double dist = dj.getShortestDistance(node.getID());
                if (dist != Double.MAX_VALUE) {
                    if (graph.getEdgeLabel(ID, node.getID()) != null) {
                        double edgeLabel = (Double) graph.getEdgeLabel(ID, node.getID());
                        addreachableNode((UNode) node, edgeLabel);
                    } else {
                        addreachableNode((UNode) node, 1);
                    }
                }
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

    public ArrayList<MyPair<Integer, Double>> getRechableWithEdgeLabel(double edgeLabel) {
        if (reachableNodes == null) {
            return new ArrayList<MyPair<Integer, Double>>();
        }

        ArrayList<MyPair<Integer, Double>> tempArr = new ArrayList<MyPair<Integer, Double>>();
        for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : reachableNodes.entrySet()) {
            tempArr.addAll(entry.getValue());
        }
        for (int j = 0; j < tempArr.size(); j++) {
            MyPair<Integer, Double> mp = tempArr.get(j);
            if (mp.getB() != edgeLabel) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }

    public ArrayList<MyPair<Integer, Double>> getRechableWithNodeIDs(int label, double edgeLabel) {
        if (reachableNodes == null) {
            return new ArrayList<MyPair<Integer, Double>>();
        }

        ArrayList<MyPair<Integer, Double>> tempArr = new ArrayList<MyPair<Integer, Double>>();
        tempArr.addAll(reachableNodes.get(label));
        for (int j = 0; j < tempArr.size(); j++) {
            MyPair<Integer, Double> mp = tempArr.get(j);
            if (mp.getB() != edgeLabel) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }

    public HashMap<Integer, ArrayList<MyPair<Integer, Double>>> getReachableWithNodes() {
        return reachableNodes;
    }

    public HashMap<Integer, ArrayList<MyPair<Integer, Double>>> getReachableByNodes() {
        return reachedBYNodes;
    }

    public ArrayList<MyPair<Integer, Double>> getRechableByNodeIDs(int label, double edgeLabel) {
        if (reachedBYNodes == null) {
            return new ArrayList<MyPair<Integer, Double>>();
        }
        ArrayList<MyPair<Integer, Double>> tempArr = new ArrayList<MyPair<Integer, Double>>();
        tempArr.addAll(reachedBYNodes.get(label));
        for (int j = 0; j < tempArr.size(); j++) {
            MyPair<Integer, Double> mp = tempArr.get(j);
            if ((mp.getB() != edgeLabel)) {
                tempArr.remove(j);
                j--;
            }
        }
        return tempArr;
    }
}
