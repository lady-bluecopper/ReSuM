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

import eu.unitn.disi.db.resum.search.Algorithm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sa.edu.kaust.grami.dataStructures.GNode;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

public class Variable {

    private HashMap<Integer, GNode> list;
    private final int label;
    private final int ID;  //represents patternNodeID

    private ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> distanceConstrainedWith;
    private ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> distanceConstrainedBy;
    private HashSet<Integer> labelDistanceConstrainedWith;
    private HashSet<Integer> labelDistanceConstrainedBy;

    @Override
    public String toString() {
        return "ID: " + ID;
    }

    public void setDistanceConstrainedWith(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> distanceConstrainedWith) {
        this.distanceConstrainedWith = distanceConstrainedWith;
    }

    public void setDistanceConstrainedBy(ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> distanceConstrainedBy) {
        this.distanceConstrainedBy = distanceConstrainedBy;
    }

    public Variable(int ID, int label, HashMap<Integer, GNode> list,
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> cons,
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> consBy) {

        this.list = list;
        this.label = label;
        this.ID = ID;

        if (cons == null) {
            distanceConstrainedWith = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        } else {
            distanceConstrainedWith = cons;
        }
        if (consBy == null) {
            distanceConstrainedBy = new ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>();
        } else {
            distanceConstrainedBy = consBy;
        }
    }

    public int getConstraintDegree() {
        return distanceConstrainedWith.size();
    }

    public void setList(HashMap<Integer, GNode> list) {
        this.list = list;
    }

    public int getListSize() {
        return list.size();
    }

    public void addConstraintWith(int va, double edgeLabel, double[] maxEdgeWeight) {
        distanceConstrainedWith.add(new MultiUserWeightedEdge<Integer, Double, double[]>(va, edgeLabel, maxEdgeWeight));
    }

    public void addConstrainedBy(int va, double edgeLabel, double[] maxEdgeWeight) {
        distanceConstrainedBy.add(new MultiUserWeightedEdge<Integer, Double, double[]>(va, edgeLabel, maxEdgeWeight));
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getDistanceConstrainedWith() {
        return distanceConstrainedWith;
    }

    public ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> getDistanceConstrainedBy() {
        return distanceConstrainedBy;
    }

    public HashMap<Integer, GNode> getList() {
        return list;
    }

    public int getLabel() {
        return label;
    }

    public int getID() {
        return ID;
    }

    public HashSet<Integer> getLabelsDistanceConstrainedWith() {
        if (labelDistanceConstrainedWith == null) {
            labelDistanceConstrainedWith = new HashSet<Integer>();

            ArrayList<Integer> temp = Algorithm.neighborLabels.get(this.getLabel());
            if (temp != null) {
                labelDistanceConstrainedWith.addAll(temp);
            }
        }
        return labelDistanceConstrainedWith;
    }

    public HashSet<Integer> getLabelsDistanceConstrainedBy() {
        if (labelDistanceConstrainedBy == null) {
            labelDistanceConstrainedBy = new HashSet<Integer>();

            ArrayList<Integer> temp = Algorithm.revNeighborLabels.get(this.getLabel());
            if (temp != null) {
                labelDistanceConstrainedBy.addAll(temp);
            }
        }
        return labelDistanceConstrainedBy;
    }
}
