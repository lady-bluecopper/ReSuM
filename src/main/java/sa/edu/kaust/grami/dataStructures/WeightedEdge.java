/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sa.edu.kaust.grami.dataStructures;

import sa.edu.kaust.grami.dataStructures.GSpanEdge;
import java.util.Objects;

/**
 *
 * @author bluecopper
 * @param <SRC>
 * @param <DST>
 * @param <EDGELABEL>
 * @param <WEIGHT>
 */
public class WeightedEdge<NODE, EDGELABEL, WEIGHT> implements Comparable {

    private final NODE n;
    private final EDGELABEL l;
    private final WEIGHT w;

    public WeightedEdge(NODE n, EDGELABEL l, WEIGHT w) {
        this.n = n;
        this.w = w;
        this.l = l;
    }

    public NODE getNodeID() {
        return n;
    }

    public WEIGHT getEdgeWeight() {
        return w;
    }

    public EDGELABEL getEdgeLabel() {
        return l;
    }

    @Override
    public boolean equals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof WeightedEdge) {
            WeightedEdge<Integer, Double, Double> edge = (WeightedEdge<Integer, Double, Double>) other;
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
        hash = 67 * hash + Objects.hashCode(this.n);
        hash = 67 * hash + Objects.hashCode(this.l);
        return hash;
    }

    public boolean actualEquals(Object other) {
        //check for self-comparison
        if (this == other) {
            return true;
        }
        //actual comparison
        if (other instanceof WeightedEdge) {
            WeightedEdge<Integer, Double, Double> edge = (WeightedEdge<Integer, Double, Double>) other;
            if (((Integer) this.n).equals(edge.n)) {
                    if (((Double) this.l).equals(edge.l)) {
                        return (((Double) this.w).equals(edge.w));
                    }
                    return false;
            }
            return false;
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof WeightedEdge) {
            WeightedEdge<Integer, Double, Double> edge = (WeightedEdge<Integer, Double, Double>) o;
            if (((Double) this.w).equals(edge.w)) {
                if (((Integer) this.n).equals(edge.n)) {
                        if (((Double) this.l).equals(edge.l)) {
                            return 0;
                        }
                        return -((Double) this.l).compareTo(edge.l);
                }
                return -((Integer) this.n).compareTo(edge.n);
            } else {
                return -((Double) this.w).compareTo(edge.w);
            }
        } else {
            throw new IllegalArgumentException("Not comparable objects");
        }
    }

    @Override
    public String toString() {
        return "(" + this.n.toString() + "," + this.l.toString() + ") - " + this.w.toString();
    }

}
