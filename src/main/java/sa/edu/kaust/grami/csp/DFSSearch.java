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

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import sa.edu.kaust.grami.automorphism.Automorphism;
import sa.edu.kaust.grami.pruning.SPpruner;
import eu.unitn.disi.db.resum.utilities.Settings;
import eu.unitn.disi.db.resum.utilities.Util;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.Query;
import sa.edu.kaust.grami.dataStructures.GNode;
import java.util.stream.IntStream;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

public class DFSSearch {

    private Variable[] variables;
    private Variable[] result;
    private MNIEdgeSet[] resultEdgeSet;
    private double[] AVGScores;
    
    public double[] getAVGScores() {
        return AVGScores;
    }
    
    private final ArrayList<HashSet<Integer>[]> maxSupporters;
    private int resultCounter = 0;
    private HashSet<Integer> visitedVariables;
    private SearchOrder sOrder;
    private final int minFreqThreshold;
    private Query qry;
    private HashIntObjMap<HashSet<Integer>> nonCandidates;

    public HashIntObjMap<HashSet<Integer>> getNonCandidates() {
        return nonCandidates;
    }

    public DFSSearch(ConstraintGraph cg, int minFreqThreshold, HashIntObjMap<HashSet<Integer>> nonCands) {
        if (!Settings.CACHING) {
            nonCandidates = Util.clone(nonCands);
        } else {
            nonCandidates = nonCands;
        }
        this.minFreqThreshold = minFreqThreshold;
        variables = cg.getVariables();
        qry = cg.getQuery();
        visitedVariables = new HashSet<Integer>();
        sOrder = new SearchOrder(variables.length);
        result = new Variable[variables.length];
        resultEdgeSet = (Settings.score < 4 ? null : new MNIEdgeSet[variables.length]);
        AVGScores = new double[Settings.actualNumOfEdgeWeights];
        maxSupporters = new ArrayList<HashSet<Integer>[]>(Settings.score < 4 ? variables.length : 1);

        for (int i = 0; i < variables.length; i++) {
            HashMap<Integer, GNode> list = new HashMap<Integer, GNode>();
            result[i] = new Variable(variables[i].getID(), variables[i].getLabel(), list,
                    variables[i].getDistanceConstrainedWith(),
                    variables[i].getDistanceConstrainedBy());
            if (Settings.score < 4) {
                maxSupporters.add(i, new HashSet[Settings.actualNumOfEdgeWeights]);
                final int z = i;
                IntStream.range(0, Settings.actualNumOfEdgeWeights)
                        .parallel()
                        .forEach(index -> {
                            maxSupporters.get(z)[index] = new HashSet<Integer>();
                        });
            } else {
                resultEdgeSet[i] = new MNIEdgeSet(variables[i].getID());
            }
        }
    }

    // for automorphisms and non-cached search
    public DFSSearch(SPpruner sp, Query qry, int minFreqThreshold) {
        this.minFreqThreshold = minFreqThreshold;
        nonCandidates = HashIntObjMaps.<HashSet<Integer>>newUpdatableMap();
        variables = sp.getVariables();
        this.qry = qry;
        visitedVariables = new HashSet<Integer>();
        sOrder = new SearchOrder(variables.length);
        result = new Variable[variables.length];
        resultEdgeSet = (Settings.score < 4 ? null : new MNIEdgeSet[variables.length]);
        AVGScores = new double[Settings.actualNumOfEdgeWeights];
        maxSupporters = new ArrayList<HashSet<Integer>[]>(Settings.score < 4 ? variables.length : 1);

        for (int i = 0; i < variables.length; i++) {
            HashMap<Integer, GNode> list = new HashMap<Integer, GNode>();
            result[i] = new Variable(variables[i].getID(), variables[i].getLabel(), list,
                    variables[i].getDistanceConstrainedWith(),
                    variables[i].getDistanceConstrainedBy());
            if (Settings.score < 4) {
                maxSupporters.add(i, new HashSet[Settings.actualNumOfEdgeWeights]);
                final int z = i;
                IntStream.range(0, Settings.actualNumOfEdgeWeights)
                        .parallel()
                        .forEach(index -> {
                            maxSupporters.get(z)[index] = new HashSet<Integer>();
                        });
            } else {
                resultEdgeSet[i] = new MNIEdgeSet(variables[i].getID());
            }
        }
    }

    // AC-3 is used to ensure arc consistency
    private void AC_3_New(Variable[] input, int freqThreshold) {
        LinkedList<VariablePair> Q = new LinkedList<VariablePair>();
        HashSet<String> contains = new HashSet<String>();
        VariablePair vp;
        // initialize...
        for (Variable currentVar : input) {
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> list = currentVar.getDistanceConstrainedWith();
            for (int j = 0; j < list.size(); j++) {
                Variable consVar = variables[list.get(j).getNodeID()];
                vp = new VariablePair(currentVar, consVar, list.get(j).getEdgeLabel());
                Q.add(vp);
                contains.add(vp.getString());
            }
        }

        while (!Q.isEmpty()) {
            vp = Q.poll();
            contains.remove(vp.getString());
            Variable v1 = vp.v1;
            Variable v2 = vp.v2;

            if ((v1.getListSize() < freqThreshold) || (v2.getListSize() < freqThreshold)) {
                return;
            }
            int oldV1Size = v1.getListSize();
            int oldV2Size = v2.getListSize();
            refine_Newest(v1, v2, vp.edgeLabel);

            if (oldV1Size != v1.getListSize()) {
                if (v1.getListSize() < freqThreshold) {
                    return;
                }
                // add to queue
                ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> list = v1.getDistanceConstrainedBy();
                for (int j = 0; j < list.size(); j++) {
                    Integer tempMP = list.get(j).getNodeID();
                    Variable consVar = variables[tempMP];
                    vp = new VariablePair(consVar, v1, list.get(j).getEdgeLabel());
                    if (!contains.contains(vp.getString())) {
                        insertInOrder(Q, vp);
                        // add new variables at the beginning
                        contains.add(vp.getString());
                    }
                }
            }
            if (oldV2Size != v2.getListSize()) {
                if (v2.getListSize() < freqThreshold) {
                    return;
                }
                // add to queue
                ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> list = v2.getDistanceConstrainedBy();
                for (int j = 0; j < list.size(); j++) {
                    Variable consVar = variables[list.get(j).getNodeID()];
                    vp = new VariablePair(consVar, v2, list.get(j).getEdgeLabel());
                    if (!contains.contains(vp.getString())) {
                        insertInOrder(Q, vp);
                        contains.add(vp.getString());
                    }
                }
            }
        }
    }

    private void refine_Newest(Variable v1, Variable v2, double edgeLabel) {
        HashMap<Integer, GNode> listA, listB;

        int labelB = v2.getLabel();
        listA = v1.getList();
        listB = v2.getList();
        HashMap<Integer, GNode> newList = new HashMap<Integer, GNode>();
        HashMap<Integer, GNode> newReachableListB = new HashMap<Integer, GNode>();

        for (GNode n1 : listA.values()) {
            if (n1.hasReachableNodes() == false) {
                continue;
            }
            // get a list of current node's neighbors
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> neighbors
                    = n1.getRechableWithNodeIDs(labelB, edgeLabel);
            if (neighbors == null) {
                continue;
            }

            for (MultiUserWeightedEdge<Integer, Double, double[]> mp : neighbors) {
                // get current neighbor details
                // check the second column if it contains the current neighbor node
                if (listB.containsKey(mp.getNodeID())) {
                    // if true, put the current node in the first column, and
                    // the neighbor node in the second column
                    newList.put(n1.getID(), n1);
                    newReachableListB.put(mp.getNodeID(), listB.get(mp.getNodeID()));
                }
            }
        }
        // set the newly assigned columns
        v1.setList(newList);
        v2.setList(newReachableListB);
    }

    public int hasBeenPrecomputed(Variable[] autos, int[] preComputed, int index) {
        HashMap<Integer, GNode> list = autos[index].getList();

        for (int nodeIndex : list.keySet()) {
            if (preComputed[nodeIndex] == 1) {
                return nodeIndex;
            }
        }
        return index; // else return the same index
    }

    public void searchExistances() {
        // fast check for the min size of all the candidates, if any of them is
        // below the minimum threshold break
        int min = variables[0].getListSize();
        for (Variable variable : variables) {
            min = Math.min(min, variable.getListSize());
        }
        if (min < minFreqThreshold) {
            return;
        }
        // Automorphisms
        Variable[] autos = null;
        Automorphism<Integer, Double> atm = null;
        int[] preComputed = null;
        if (Settings.isAutomorphismOn) {
            HPListGraph<Integer, Double> listGraph = qry.getListGraph();
            preComputed = new int[variables.length];
            atm = new Automorphism<Integer, Double>(listGraph);
            autos = atm.getResult();
        }
        // SEARCH
        for (int i = variables.length - 1; i >= 0; i--) {
            boolean search = true;
            if (Settings.isAutomorphismOn && atm.hasAutomorphisms()) {
                int preIndex = hasBeenPrecomputed(autos, preComputed, i);
                // candidate nodes already computed
                if (i != preIndex) {
                    search = false;
                    variables[i].setList((HashMap<Integer, GNode>) variables[preIndex].getList().clone());
                    result[i].setList((HashMap<Integer, GNode>) result[preIndex].getList().clone());
                    if (Settings.score < 4) {
                        final int z = i;
                        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                                .parallel()
                                .forEach(index -> {
                                    maxSupporters.get(z)[index] = (HashSet<Integer>) maxSupporters.get(preIndex)[index].clone();
                                });

                    } else {
                        resultEdgeSet[i].setEdgeSets(resultEdgeSet[preIndex].getEdgeSets().clone());
                    }
                }
            }
            // if any domain has not enough candidates, return
            if (search == true) {
                // fast check
                min = variables[0].getListSize();
                for (Variable variable : variables) {
                    min = Math.min(min, variable.getListSize());
                }
                if (min < minFreqThreshold) {
                    return;
                }
                setVariableVisitingOrder(i);
                int index = -1;
                index = sOrder.getNext();
                Variable firstVB = variables[index];
                // firstList = domain of first variable
                HashMap<Integer, GNode> firstList = firstVB.getList();
                AssignmentInstance instance = new AssignmentInstance(variables.length);
                for (Iterator<GNode> iterator = firstList.values().iterator(); iterator.hasNext();) {
                    GNode firstNode = iterator.next();
                    // ---- TODO (doublecheck!)
//                    boolean stop = true;
//                    for (int u = 0; u < Settings.structureSize; u ++) {
//                        if (!maxSupporters.get(index)[u].contains(firstNode.getID())) {
//                            stop = false;
//                            break;
//                        }
//                    }
//                    if (stop) {
//                        continue;
//                    }
                    // -----
                    sOrder.reset();
                    instance.assign(firstVB.getID(), firstNode);

                    int value = -1;
                    value = searchExistances(instance);
                    // solution not found
                    if (value == -2) {
                        iterator.remove();
                        if (Settings.isAutomorphismOn && atm.hasAutomorphisms()) {
                            HashMap<Integer, GNode> list = autos[firstVB.getID()].getList();
                            for (int nodeIndex : list.keySet()) {
                                nonCandidates.putIfAbsent(nodeIndex, new HashSet<Integer>());
                                if (!nonCandidates.get(nodeIndex).contains(firstNode.getID())) {
                                    nonCandidates.get(nodeIndex).add(firstNode.getID());
                                }
                            }
                        } else {
                            nonCandidates.putIfAbsent(firstVB.getID(), new HashSet<Integer>());
                            if (!nonCandidates.get(firstVB.getID()).contains(firstNode.getID())) {
                                nonCandidates.get(firstVB.getID()).add(firstNode.getID());
                            }
                        }
                    }

                    // solution found
                    if (value == -1) {
                        final boolean[] maxSupporting;
                        switch (Settings.score) {
                            case 1:
                                maxSupporting = instance.maxSatisfiesALL();
                                break;
                            case 2:
                                maxSupporting = instance.maxSatisfiesANY();
                                break;
                            case 3:
                                maxSupporting = instance.maxSatisfiesSUM();
                                break;
                            default:
                                maxSupporting = new boolean[Settings.actualNumOfEdgeWeights];
                                break;
                        }
                        // instance Found
                        for (int j = 0; j < variables.length; j++) {
                            GNode assignedNode = instance.getNodeAssignment(j);
                            if (Settings.isAutomorphismOn && atm.hasAutomorphisms()) {
                                HashMap<Integer, GNode> list = autos[j].getList();
                                for (int nodeIndex : list.keySet()) {
                                    if (!result[nodeIndex].getList().containsKey(assignedNode.getID())) {
                                        result[nodeIndex].getList().put(assignedNode.getID(), assignedNode);
                                    }
                                    if (Settings.score < 4) {
                                        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                                                .parallel()
                                                .forEach(idx -> {
                                                    if (maxSupporting[idx]) {
                                                        maxSupporters.get(nodeIndex)[idx].add(assignedNode.getID());
                                                    }
                                                });
                                    } else {
                                        if (instance.getEdgeAssignment(j) != null) {
                                            resultEdgeSet[nodeIndex].addEdge(instance.getEdgeAssignment(j));
                                        }
                                    }

                                }
                            } else {
                                if (!result[j].getList().containsKey(assignedNode.getID())) {
                                    result[j].getList().put(assignedNode.getID(), assignedNode);
                                }
                                if (Settings.score < 4) {
                                    final int z = j;
                                    IntStream.range(0, Settings.actualNumOfEdgeWeights)
                                            .parallel()
                                            .forEach(idx -> {
                                                if (maxSupporting[idx]) {
                                                    maxSupporters.get(z)[idx].add(assignedNode.getID());
                                                }
                                            });
                                } else {
                                    if (instance.getEdgeAssignment(j) != null) {
                                        resultEdgeSet[j].addEdge(instance.getEdgeAssignment(j));
                                    }
                                }
                            }
                        }
                        // ----- TODO (doublecheck!)
//                        boolean stop2 = true;
//                        for (int u = 0; u < Settings.structureSize; u ++) {
//                            if (maxSupporters.get(index)[u].size() < minFreqThreshold) {
//                                stop2 = false;
//                                break;
//                            }
//                        }
//                        if (stop2) {
//                            break;
//                        }
                        // -----
                    }
                    instance.clear();
                }
                // end of Search if the domain does not contain enough
                // candidates
                if (result[index].getList().size() < minFreqThreshold) {
                    return;
                }
            }

            resetVariableVisitingOrder();
            // Arc consistency
            AC_3_New(variables, minFreqThreshold);
            if (Settings.isAutomorphismOn) {
                preComputed[i] = 1;
            }
        }
    }

    // find one assignment for the instance
    private int searchExistances(AssignmentInstance instance) {
        int index = sOrder.getNext();
        if (index != -1) {
            Variable currentVB = variables[index];
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrainingVariables
                    = currentVB.getDistanceConstrainedWith();

            ArrayList<ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> candidates
                    = new ArrayList<ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();
            ArrayList<VariableCandidates> variableCandidates = new ArrayList<VariableCandidates>();

            // check Validity with constraintVariables
            for (int i = 0; i < constrainingVariables.size(); i++) {

                Variable cnVariable = variables[constrainingVariables.get(i).getNodeID()];
                Double edgeLabel = constrainingVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getNodeAssignment(cnVariableIndex);
                if (cnVariableInstance != null) {
                    candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel));
                    variableCandidates.add(new VariableCandidates(cnVariableIndex,
                            cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel)));
                }
            }

            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrainingBYVariables = currentVB.getDistanceConstrainedBy();
            for (int i = 0; i < constrainingBYVariables.size(); i++) {
                Variable cnVariable = variables[constrainingBYVariables.get(i).getNodeID()];
                Double edgeLabel = constrainingBYVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getNodeAssignment(cnVariableIndex);
                if (cnVariableInstance != null) {
                    candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel));
                    variableCandidates.add(new VariableCandidates(cnVariableIndex,
                            cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel)));
                }
            }

            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> finalCandidates
                    = Util.getIntersection(candidates);

            if (finalCandidates.size() == 0) {
                // learn the new constraints !!!
                ArrayList<Point> constrainedVariableIndices = Util.getZerosIntersectionIndices(variableCandidates);
                if (constrainedVariableIndices.size() != 0) {
                    Point p = constrainedVariableIndices.get(0);
                    int minValue = sOrder.getSecondOrderValue(p.x, p.y);
                    for (int i = 1; i < constrainedVariableIndices.size(); i++) {
                        p = constrainedVariableIndices.get(i);
                        int value = sOrder.getSecondOrderValue(p.x, p.y);
                        if (minValue > value) {
                            minValue = value;
                        }
                    }
                    int jumpToIndex = sOrder.getVariableIndex(minValue);
                    sOrder.stepBack();
                    instance.deAssign(currentVB.getID());
                    return jumpToIndex;
                }
            }

            int hasResult = 0;

            // for each candidate find an assignment
            for (int i = 0; i < finalCandidates.size(); i++) {
                int candidateIndex = finalCandidates.get(i).getNodeID();
                GNode candidateNode = currentVB.getList().get(candidateIndex);
                if (candidateNode != null) {
                    instance.assign(currentVB.getID(), candidateNode);
                    // check identity Validity
                    if (AssignmentInstance.ensureIDValidty(instance)) {
                        hasResult = searchExistances(instance);
                        if (hasResult == -1) {
                            instance.assignEdge(finalCandidates.get(i), currentVB.getID());
                            return -1;
                        } else if (hasResult >= 0) {
                            if (currentVB.getID() != hasResult) {
                                sOrder.stepBack();
                                instance.deAssign(currentVB.getID());
                                return hasResult;
                            }
                        }
                    } else {
                        instance.deAssign(currentVB.getID());
                    }
                }
            }
            // after finishing... step back to before state
            sOrder.stepBack();
            instance.deAssign(currentVB.getID());
        } // if index ==-1 the assignment is legal
        else {
            return -1; // return True
        }
        return -2; // return False
    }

    public int getResultCounter() {
        return resultCounter;
    }

    private void resetVariableVisitingOrder() {
        sOrder = new SearchOrder(variables.length);
        visitedVariables.clear();
    }

    private void setVariableVisitingOrder(int begin) {
        sOrder.addNext(begin);
        visitedVariables.add(begin);
        searchOrder(variables[begin]);
    }

    private void searchOrder(Variable vb) {
        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrains = vb.getDistanceConstrainedWith();
        for (int i = 0; i < constrains.size(); i++) {
            Variable currentVB = variables[constrains.get(i).getNodeID()];
            if (!visitedVariables.contains(currentVB.getID())) {
                visitedVariables.add(currentVB.getID());
                sOrder.addNext(currentVB.getID());
                searchOrder(currentVB);
            }
        }
        ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrainsBY = vb.getDistanceConstrainedBy();
        for (int i = 0; i < constrainsBY.size(); i++) {
            Variable currentVB = variables[constrainsBY.get(i).getNodeID()];
            if (!visitedVariables.contains(currentVB.getID())) {
                visitedVariables.add(currentVB.getID());
                sOrder.addNext(currentVB.getID());
                searchOrder(currentVB);
            }
        }
    }

    public int getFrequency() {
        int min = result[0].getListSize();
        for (int i = 1; i < result.length; i++) {
            min = Math.min(min, result[i].getListSize());
        }
        return min;
    }

    public double[] getMaxFrequencies() {
        double[] minFreqs = new double[Settings.actualNumOfEdgeWeights];
        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                .parallel()
                .forEach(idx -> {
                    minFreqs[idx] = maxSupporters.get(0)[idx].size();
                    for (int j = 1; j < result.length; j++) {
                        minFreqs[idx] = Math.min(minFreqs[idx], maxSupporters.get(j)[idx].size());
                    }
                });
        return minFreqs;
    }

    public double[] computeAVGScore() {
        double[] scores = new double[Settings.actualNumOfEdgeWeights];
        int k = getFrequency();
        if (k > 0) {
            IntStream.range(0, Settings.actualNumOfEdgeWeights)
                    .parallel()
                    .forEach(idx -> {
                        double[] edgeWeights;
                        int validMNISets = 0;
                        for (MNIEdgeSet patternEdges : resultEdgeSet) {
                            if (!patternEdges.getEdgeSet(0).isEmpty()) {
                                validMNISets++;
                                edgeWeights = patternEdges.getTopKWeights(idx, k);
                                for (double weight : edgeWeights) {
                                    scores[idx] += weight;
                                }
                            }
                        }
                        scores[idx] = scores[idx] / validMNISets;
                    });
        }
        // TODO modified
        resultEdgeSet = null;
        AVGScores = scores;
        return scores;
    }

    public Variable[] getResultVariables() {
        return result;
    }

    public MNIEdgeSet[] getResultEdges() {
        return resultEdgeSet;
    }

    public ArrayList<HashSet<Integer>[]> getMaxAssignments() {
        return maxSupporters;
    }

    private int getMaxDegreeVariableIndex() {
        Variable[] vs = variables;
        int index = 0;
        int max = vs[0].getConstraintDegree();
        for (int i = 1; i < vs.length; i++) {
            int degree = vs[i].getConstraintDegree();
            if (max < degree) {
                max = degree;
                index = i;
            }
        }
        return index;
    }

    //method used by Automorphism
    private void search(AssignmentInstance instance) {
        int index = sOrder.getNext();
        if (index != -1) {
            Variable currentVB = variables[index];
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrainingVariables
                    = currentVB.getDistanceConstrainedWith();
            ArrayList<ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> candidates
                    = new ArrayList<ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>();

            // examine outgoing edges
            for (int i = 0; i < constrainingVariables.size(); i++) {
                Variable cnVariable = variables[constrainingVariables.get(i).getNodeID()];
                double edgeLabel = constrainingVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getNodeAssignment(cnVariableIndex);
                // if an assignment for the variable cnVariableIndex has already been found
                if (cnVariableInstance != null) {
                    candidates.add(cnVariableInstance.getRechableByNodeIDs(currentVB.getLabel(), edgeLabel));
                }
            }

            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> constrainingBYVariables
                    = currentVB.getDistanceConstrainedBy();

            // examine ingoing edges
            for (int i = 0; i < constrainingBYVariables.size(); i++) {
                Variable cnVariable = variables[constrainingBYVariables.get(i).getNodeID()];
                double edgeLabel = constrainingBYVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getNodeAssignment(cnVariableIndex);
                if (cnVariableInstance != null) {
                    candidates.add(cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel));
                }
            }

            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> finalCandidates
                    = Util.getIntersection(candidates);

            for (int i = 0; i < finalCandidates.size(); i++) {
                int candidateIndex = finalCandidates.get(i).getNodeID();
                GNode candidateNode = currentVB.getList().get(candidateIndex);
                if (candidateNode != null) {
                    instance.assign(currentVB.getID(), candidateNode);
                    // check identity Validity
                    if (AssignmentInstance.ensureIDValidty(instance)) {
                        instance.assignEdge(finalCandidates.get(i), currentVB.getID());
                        // proceed with next
                        search(instance);
                    } else {
                        instance.deAssign(currentVB.getID());
                    }
                }
            }
            // step back to before state
            sOrder.stepBack();
            instance.deAssign(currentVB.getID());

        } // index ==-1 means that I reached the point where the assignment is legal
        else {
            resultCounter++;
            for (int i = 0; i < instance.getAssignmentSize(); i++) {
                GNode nodeInstance = instance.getNodeAssignment(i);
                if (!result[i].getList().containsKey(nodeInstance.getID())) {
                    result[i].getList().put(nodeInstance.getID(), nodeInstance);
                }
                if (Settings.score == 4) {
                    if (instance.getEdgeAssignment(i) != null) {
                        resultEdgeSet[i].addEdge(instance.getEdgeAssignment(i));
                    }
                }
            }
        }
    }

    // iterate over the candidate nodes of current variable to find solutions
    // method used by Automorphism
    public void searchAll() {
        // set variable visit order
        setVariableVisitingOrder(getMaxDegreeVariableIndex());
        int index = -1;
        index = sOrder.getNext();
        Variable firstVB = variables[index];
        HashMap<Integer, GNode> firstList = firstVB.getList();
        AssignmentInstance instance = new AssignmentInstance(variables.length);

        for (GNode firstNode : firstList.values()) {
            instance.assign(firstVB.getID(), firstNode);
            search(instance);
        }
    }

    // insert vp into order according to their variable values length
    private void insertInOrder(LinkedList<VariablePair> Q, VariablePair vp) {
        int i = 0;
        Iterator<VariablePair> itr = Q.iterator();
        while (itr.hasNext()) {
            VariablePair tempVP = (VariablePair) itr.next();
            if (tempVP.getMinValuesLength() > vp.getMinValuesLength()) {
                Q.add(i, vp);
                return;
            }
            i++;
        }
        Q.add(i, vp);
    }

}
