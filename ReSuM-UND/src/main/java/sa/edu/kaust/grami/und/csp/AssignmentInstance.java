package sa.edu.kaust.grami.und.csp;

import java.util.HashSet;

import sa.edu.kaust.grami.und.dataStructures.GNode;
import java.util.stream.IntStream;
import eu.unitn.disi.db.resum.und.utilities.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.und.utilities.Settings;
import java.util.Map;
import java.util.HashMap;


public class AssignmentInstance {

    private Integer minCOST;

    public int getMinCOST() {
        return minCOST;
    }

    public void setMinCOST(int minCOST) {
        this.minCOST = minCOST;
    }
    private GNode[] assignments;
    private Map<String, MultiUserWeightedEdge<Integer, Double, double[]>> edgeAssignments;
    private Integer[] assignmentOrder;
    private int order = 0;

    public AssignmentInstance(int numNodes, int numEdges) {
        assignments = new GNode[numNodes];
        edgeAssignments = new HashMap<String, MultiUserWeightedEdge<Integer, Double, double[]>>(numEdges); 
        assignmentOrder = new Integer[numNodes];
        for (int i = 0; i < assignments.length; i++) {
            assignments[i] = null;
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

    public void assign(int index, GNode node) {
        assignments[index] = node;
        assignmentOrder[index] = order;
        order++;
    }

    public void assignEdge(MultiUserWeightedEdge<Integer, Double, double[]> edge, String index) {
        edgeAssignments.put(index, edge);
    }

    public void deAssign(int index) {
        assignments[index] = null;
        assignmentOrder[index] = null;
        order--;
    }

    public void deAssignEdge(String index) {
        edgeAssignments.remove(index);
    }

    public void clear() {
        for (int i = 0; i < assignments.length; i++) {
            deAssign(i);
        }
        edgeAssignments.clear();
        order = 0;
        minCOST = null;
    }

    public GNode getAssignment(int index) {
        return assignments[index];
    }

    public MultiUserWeightedEdge<Integer, Double, double[]> getEdgeAssignment(String index) {
        return edgeAssignments.get(index);
    }
    
    public Map<String, MultiUserWeightedEdge<Integer, Double, double[]>> getEdgeAssignments() {
        return edgeAssignments;
    }

    public void printInstance() {
        System.out.print("Assignment: ");
        for (GNode node : assignments) {
            System.out.print(node.getID() + ",(" + node.getLabel() + ")   ");
        }
    }

    public static boolean ensureIDValidty(AssignmentInstance ass) {
        HashSet<Integer> container = new HashSet<Integer>();
        for (int i = 0; i < ass.getAssignmentSize(); i++) {
            GNode n = ass.getAssignment(i);
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
        for (MultiUserWeightedEdge<Integer, Double, double[]> edge : edgeAssignments.values()) {
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
        return relevance;
    }

    public boolean[] maxSatisfiesANY() {
        boolean[] relevance = new boolean[Settings.actualNumOfEdgeWeights];
        for (MultiUserWeightedEdge<Integer, Double, double[]> edge : edgeAssignments.values()) {
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
        return relevance;
    }

    public boolean[] maxSatisfiesSUM() {
        boolean[] relevance = new boolean[Settings.actualNumOfEdgeWeights];
        double[] maxSum = new double[Settings.actualNumOfEdgeWeights];
        edgeAssignments.values().stream().forEach(edge -> {
            double[] edgeWeights = edge.getMaxWeights();
            IntStream.range(0, edgeWeights.length).parallel().forEach(index -> maxSum[index] += edgeWeights[index]);
        });
        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                .parallel()
                .forEach(index -> relevance[index] = (maxSum[index] > Settings.relevance));
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
