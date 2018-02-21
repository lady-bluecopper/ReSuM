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

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import eu.unitn.disi.db.resum.utilities.Settings;
import eu.unitn.disi.db.resum.utilities.Util;
import java.awt.Point;
import java.util.Collections;
import java.util.Iterator;

public class Graph {

    public final static int NO_EDGE = 0;
    private HPListGraph<Integer, Double> m_matrix;
    private int nodeCount = 0;
    private ArrayList<GNode> nodes;
    private HashMap<Integer, HashMap<Integer, GNode>> nodesByLabel;
    private ArrayList<Integer> NodeLabels;
    private HashMap<Double, Integer> edgeLabelsWithFreq;
    private HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel;
    private ArrayList<Double> freqEdgeLabels;
    private ArrayList<Point> sortedFreqLabelsWithFreq;
    private ArrayList<Integer> sortedFreqLabels;

    public Graph() {

        NodeLabels = new ArrayList<Integer>();
        m_matrix = new HPListGraph<Integer, Double>();
        nodesByLabel = new HashMap<Integer, HashMap<Integer, GNode>>();
        nodes = new ArrayList<GNode>();
        edgeLabelsWithFreq = new HashMap<Double, Integer>();
        freqEdgeLabels = new ArrayList<Double>();
        sortedFreqLabels = new ArrayList<Integer>();
        sortedFreqLabelsWithFreq = new ArrayList<Point>();
        freqNodesByLabel = new HashMap<Integer, HashMap<Integer, GNode>>();

        if (StaticData.hashedEdges != null) {
            StaticData.hashedEdges = null;
        }
        StaticData.hashedEdges = new HashMap<String, HashMap<Integer, Integer>[]>();
    }

    public ArrayList<Integer> getNodeLabels() {
        return NodeLabels;
    }

    public ArrayList<Double> getFreqEdgeLabels() {
        return this.freqEdgeLabels;
    }
    
    public ArrayList<Integer> getSortedFreqLabels() {
        return sortedFreqLabels;
    }

    public HashMap<Integer, HashMap<Integer, GNode>> getNodesByLabel() {
        return nodesByLabel;
    }

    public void loadGraph(ArrayList<double[]> edgeWeights) throws Exception {
        final BufferedReader rows = new BufferedReader(new FileReader(Paths.get(Settings.datasetsFolder, Settings.inputFileName).toFile()));
        System.out.println("Start Reading Graph");
        int counter = 0;
        int numberOfNodes = 0;
        int numberOfEdges = 0;
        int parsed = 0;
        String line = rows.readLine();
        while (line != null) {
            if (line.startsWith("v")) {
                final String[] parts = line.split("\\s+");
                final int index = Integer.parseInt(parts[1]);
                int label = Integer.parseInt(parts[2]);
                if (Settings.NOLABEL) {
                    label = 1;
                }
                if (index != counter) {
                    throw new ParseException("The node list is not sorted", counter);
                }
                addNode(label);
                GNode n = new GNode(numberOfNodes, label);
                nodes.add(n);

                nodesByLabel.putIfAbsent(label, new HashMap<Integer, GNode>());
                nodesByLabel.get(label).put(n.getID(), n);

                numberOfNodes++;
                counter++;

            } else if (line.startsWith("e")) {
                final String[] parts = line.split("\\s+");
                final int index1 = Integer.parseInt(parts[1]);
                final int index2 = Integer.parseInt(parts[2]);
                final double label = Double.parseDouble(parts[3]);
                double[] maxEdgeWeights;
                if (edgeWeights != null) {
                    maxEdgeWeights = edgeWeights.get(numberOfEdges);
                } else {
                    maxEdgeWeights = new double[1];
                }
                addEdge(index1, index2, label, maxEdgeWeights, Edge.OUTGOING);
                parsed += 1;
                numberOfEdges += 1;
                if (parsed % 1000000 == 0) {
                    System.out.println("Edges added: " + parsed + " " + System.currentTimeMillis());
                }
//                if (!Settings.directed) {
//                    addEdge(index2, index1, label, maxEdgeWeights, Edge.OUTGOING);
//                }
            }
            line = rows.readLine();
        }
        rows.close();
        System.out.println("Graph Read");
        nodeCount = numberOfNodes;
        // compute node labels
        for (Entry<Integer, HashMap<Integer, GNode>> ar : nodesByLabel.entrySet()) {
            NodeLabels.add(ar.getKey());
        }
        // prune infrequent edge labels
        for (Entry<Double, Integer> ar : this.edgeLabelsWithFreq.entrySet()) {
            if (ar.getValue() >= Settings.frequency) {
                this.freqEdgeLabels.add(ar.getKey());
            }
        }
        // prune the infrequent nodes
        for (Entry<Integer, HashMap<Integer, GNode>> ar : this.nodesByLabel.entrySet()) {
            if (ar.getValue().size() >= Settings.frequency) {
                sortedFreqLabelsWithFreq.add(new Point(ar.getKey(), ar.getValue().size()));
                freqNodesByLabel.put(ar.getKey(), ar.getValue());
            }
        }

        Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());

        for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) {
            sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
        }
        // prune infrequent hashedEdges
        ArrayList<String> toBeDeleted = new ArrayList<String>();
        Set<String> s = StaticData.hashedEdges.keySet();
        for (String sig : s) {
            HashMap[] hm = StaticData.hashedEdges.get(sig);
            if (hm[0].size() < Settings.frequency || hm[1].size() < Settings.frequency) {
                toBeDeleted.add(sig);
            }
        }
        for (String sig : toBeDeleted) {
            StaticData.hashedEdges.remove(sig);
        }
        System.out.println("Graph Pruned.");
    }

    public void setShortestPaths_1hop() {
        for (Entry<Integer, HashMap<Integer, GNode>> ar : nodesByLabel.entrySet()) {
            HashMap<Integer, GNode> nodes = ar.getValue();
            for (GNode node : nodes.values()) {
                node.setReachableNodes_1hop(this, nodesByLabel);
            }
        }
    }

    public GNode getNode(int ID) {
        return nodes.get(ID);
    }

    public HPListGraph<Integer, Double> getListGraph() {
        return m_matrix;
    }

    public int getDegree(int node) {
        return m_matrix.getDegree(node);
    }

    public int getNumberOfNodes() {
        return nodeCount;
    }

    public int addNode(int nodeLabel) {
        return m_matrix.addNodeIndex(nodeLabel);
    }

    public int addEdge(int nodeA, int nodeB, double edgeLabel,
            double[] maxEdgeWeights, int direction) {

        edgeLabelsWithFreq.putIfAbsent(edgeLabel, 0);
        edgeLabelsWithFreq.put(edgeLabel, edgeLabelsWithFreq.get(edgeLabel) + 1);
        // add edge frequency
        int labelA = nodes.get(nodeA).getLabel();
        int labelB = nodes.get(nodeB).getLabel();

        String hn;
        hn = labelA + "_" + edgeLabel + "_" + labelB;

        HashMap<Integer, Integer>[] hm = StaticData.hashedEdges.get(hn);
        if (hm == null) {
            hm = new HashMap[2];
            hm[0] = new HashMap();
            hm[1] = new HashMap();
            StaticData.hashedEdges.put(hn, hm);
        }
        hm[0].put(nodeA, nodeA);
        hm[1].put(nodeB, nodeB);

        return m_matrix.addEdgeIndex(nodeA, nodeB, edgeLabel, maxEdgeWeights, direction);
    }

    public ArrayList<GNode> getNodes() {
        return nodes;
    }
}
