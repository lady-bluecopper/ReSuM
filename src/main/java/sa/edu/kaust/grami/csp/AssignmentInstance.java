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
package sa.edu.kaust.grami.csp;

import java.util.HashSet;

import sa.edu.kaust.grami.dataStructures.GNode;
import java.util.stream.IntStream;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.utilities.Settings;

public class AssignmentInstance {

    private Integer minCOST;

    public int getMinCOST() {
        return minCOST;
    }

    public void setMinCOST(int minCOST) {
        this.minCOST = minCOST;
    }

    private GNode[] assignments;
    private MultiUserWeightedEdge<Integer, Double, double[]>[] edgeAssignments;
    private Integer[] assignmentOrder;

    private int order = 0;

    public AssignmentInstance(int size) {
        assignments = new GNode[size];
        edgeAssignments = new MultiUserWeightedEdge[size];
        assignmentOrder = new Integer[size];
        for (int i = 0; i < assignments.length; i++) {
            assignments[i] = null;
            edgeAssignments[i] = null;
        }
    }

    public int getAssignnedValuesSize() {
        int counter = 0;
        for (GNode assignment : assignments) {
            if (assignment != null) {
                counter++;
            }
        }
        return counter;
    }

    public int getAssignmentSize() {
        return assignments.length;
    }

    public MultiUserWeightedEdge<Integer, Double, double[]>[] getEdgeAssignments() {
        return edgeAssignments;
    }

    public void assign(int index, GNode node) {
        assignments[index] = node;
        assignmentOrder[index] = order;
        order++;
    }

    public void assignEdge(MultiUserWeightedEdge<Integer, Double, double[]> edge, int index) {
        edgeAssignments[index] = edge;
    }

    public void deAssign(int index) {
        assignments[index] = null;
        assignmentOrder[index] = null;
        order--;
    }

    public void deAssignEdge(int index) {
        edgeAssignments[index] = null;
    }

    public void clear() {
        for (int i = 0; i < assignments.length; i++) {
            deAssign(i);
            deAssignEdge(i);
        }
        order = 0;
        minCOST = null;
    }

    public GNode getNodeAssignment(int index) {
        return assignments[index];
    }

    public MultiUserWeightedEdge<Integer, Double, double[]> getEdgeAssignment(int index) {
        return edgeAssignments[index];
    }

    public void printInstance() {
        System.out.print("Assignment: ");
        for (GNode node : assignments) {
            if (node != null) {
                System.out.print(node.getID() + ",(" + node.getLabel() + ")   ");
            }
        }
    }

    // check if a node has been assigned to different variable. If it is the case, return FALSE
    public static boolean ensureIDValidty(AssignmentInstance ass) {
        HashSet<Integer> container = new HashSet<Integer>();
        for (int i = 0; i < ass.getAssignmentSize(); i++) {
            GNode n = ass.getNodeAssignment(i);

            if (n == null) {
                continue;
            }
            int ID = n.getID();
            if (container.contains(ID)) {
                return false;
            }
            container.add(ID);
        }
        return true;
    }

    public boolean[] maxSatisfiesALL() {
        boolean[] relevance = new boolean[Settings.actualNumOfEdgeWeights];
        for (int e = 0; e < Settings.actualNumOfEdgeWeights; e++) {
            relevance[e] = true;
        }
        for (MultiUserWeightedEdge<Integer, Double, double[]> edge : edgeAssignments) {
            if (edge != null) {
                double[] edgeWeights = edge.getMaxWeights();
                IntStream.range(0, edgeWeights.length)
                        .parallel()
                        .forEach(index -> {
                            if (edgeWeights[index] <= Settings.relevance) {
                                relevance[index] = false;
                            }
                        });
                boolean stop = true;
                for (boolean e : relevance) {
                    if (e) {
                        stop = false;
                        break;
                    }
                }
                if (stop) {
                    break;
                }
            }
        }
        return relevance;
    }

    public boolean[] maxSatisfiesANY() {
        boolean[] relevance = new boolean[Settings.actualNumOfEdgeWeights];
        for (MultiUserWeightedEdge<Integer, Double, double[]> edge : edgeAssignments) {
            if (edge != null) {
                double[] edgeWeights = edge.getMaxWeights();
                IntStream.range(0, edgeWeights.length)
                        .parallel()
                        .forEach(index -> {
                            if (edgeWeights[index] > Settings.relevance) {
                                relevance[index] = true;
                            }
                        });
                boolean stop = true;
                for (boolean e : relevance) {
                    if (!e) {
                        stop = false;
                        break;
                    }
                }
                if (stop) {
                    break;
                }
            }
        }
        return relevance;
    }

    public boolean[] maxSatisfiesSUM() {
        boolean[] relevance = new boolean[Settings.actualNumOfEdgeWeights];
        double[] maxSum = new double[Settings.actualNumOfEdgeWeights];
        for (MultiUserWeightedEdge<Integer, Double, double[]> edge : edgeAssignments) {
            if (edge != null) {
                double[] edgeWeights = edge.getMaxWeights();
                IntStream.range(0, edgeWeights.length)
                        .parallel()
                        .forEach(index -> {
                            maxSum[index] += edgeWeights[index];
                        });
            }
        }
        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                .parallel()
                .forEach(index -> {
                    relevance[index] = (maxSum[index] > Settings.relevance);
                });
        return relevance;
    }

    @Override
    public String toString() {
        String rep = "";

        for (int i = 0; i < assignments.length; i++) {
            GNode node = assignments[i];
            if (node == null) {
                rep += " _";
            } else {
                rep += " " + node.getID() + "(" + assignmentOrder[i] + ")";
            }
        }
        return rep;
    }
}
