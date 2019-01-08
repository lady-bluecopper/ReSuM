package eu.unitn.disi.db.resum.und.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author bluecopper
 * @param <NODE>
 * @param <EDGELABEL>
 * @param <MAXWEIGHTS>
 */
public class MultiUserWeightedEdge<NODE, EDGELABEL, MAXWEIGHTS> implements Comparable {

    private final NODE n;
    private final EDGELABEL l;
    private final MAXWEIGHTS max;

    public MultiUserWeightedEdge(NODE n, EDGELABEL b, MAXWEIGHTS c) {
        this.n = n;
        this.l = b;
        this.max = c;
    }

    public NODE getNodeID() {
        return n;
    }

    public EDGELABEL getEdgeLabel() {
        return l;
    }

    public MAXWEIGHTS getMaxWeights() {
        return max;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof MultiUserWeightedEdge) {
            MultiUserWeightedEdge<Integer, Double, double[]> edge = (MultiUserWeightedEdge<Integer, Double, double[]>) other;
            if (((Integer) this.n).equals(edge.n)) {
                    return (((Double) this.l).equals(edge.l));
                }
            else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.n);
        hash = 59 * hash + Objects.hashCode(this.l);
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof MultiUserWeightedEdge) {
            MultiUserWeightedEdge<Integer, Double, double[]> edge = (MultiUserWeightedEdge<Integer, Double, double[]>) o;
            if (((Integer) this.n).equals(edge.n)) {
                    return -((Double) this.l).compareTo(edge.l);
            }
            return -((Integer) this.n).compareTo(edge.n);
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    public boolean actualEquals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof MultiUserWeightedEdge) {
            MultiUserWeightedEdge<Integer, Double, double[]> edge = (MultiUserWeightedEdge<Integer, Double, double[]>) other;
            if (((Integer) this.n).equals(edge.getNodeID())) {
                    if (((Double) this.l).equals(edge.getEdgeLabel())) {
                        return (Arrays.equals((double[]) this.max, edge.getMaxWeights()));
                    }
                    return false;
            }
            return false;
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    public Pair<NODE, EDGELABEL> getUnweightedEdge() {
        return new Pair(this.n, this.l);
    }

    public static int getIndexOf(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> arr, int src, double label) {
        int i = 0;
        Iterator<MultiUserWeightedEdge<Integer, Double, double[]>> itr = arr.iterator();
        while (itr.hasNext()) {
            MultiUserWeightedEdge<Integer, Double, double[]> element = itr.next();
            if ((element.getNodeID().equals(src) && element.getEdgeLabel().equals(label))) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "NODE: " + this.n.toString() + " L: " + this.l.toString();
    }

}
