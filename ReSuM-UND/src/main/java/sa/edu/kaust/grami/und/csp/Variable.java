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
package sa.edu.kaust.grami.und.csp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sa.edu.kaust.grami.und.dataStructures.GNode;
import sa.edu.kaust.grami.und.dataStructures.UnEdge;
import sa.edu.kaust.grami.und.search.Algorithm;

public class Variable {

    private HashMap<Integer, GNode> list;
    private int label;
    private int ID;  //represents patternNodeID
    private ArrayList<UnEdge<Integer, Double>> distanceConstrainedWith;
    private boolean newVar = false;

    private HashSet<Integer> labelDistanceConstrainedWith;

    public void setDistanceConstrainedWith(ArrayList<UnEdge<Integer, Double>> distanceConstrainedWith) {
        this.distanceConstrainedWith = distanceConstrainedWith;
    }

    public Variable(int ID, int label, HashMap<Integer, GNode> list, ArrayList<UnEdge<Integer, Double>> cons) {
        this.list = list;
        this.label = label;
        this.ID = ID;
        this.distanceConstrainedWith = (cons == null) ? new ArrayList<UnEdge<Integer, Double>>() : cons;
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

    public void addConstraintWith(int va, double edgeLabel) {
        distanceConstrainedWith.add(new UnEdge<Integer, Double>(va, edgeLabel));
    }

    public ArrayList<UnEdge<Integer, Double>> getDistanceConstrainedWith() {
        return distanceConstrainedWith;
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
            labelDistanceConstrainedWith.addAll(Algorithm.neighborLabels.get(this.getLabel()));
        }
        return labelDistanceConstrainedWith;
    }

    public void setNew() {
        this.newVar = true;
    }

    public void setOld() {
        this.newVar = false;
    }

    public boolean isNew() {
        return this.newVar;
    }

}
