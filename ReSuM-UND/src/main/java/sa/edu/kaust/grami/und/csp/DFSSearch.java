package sa.edu.kaust.grami.und.csp;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import sa.edu.kaust.grami.und.automorphism.Automorphism;
import sa.edu.kaust.grami.und.pruning.SPpruner;
import eu.unitn.disi.db.resum.und.utilities.Settings;
import eu.unitn.disi.db.resum.und.utilities.Util;
import sa.edu.kaust.grami.und.dataStructures.HPListGraph;
import sa.edu.kaust.grami.und.dataStructures.Query;
import sa.edu.kaust.grami.und.dataStructures.GNode;
import java.util.stream.IntStream;
import eu.unitn.disi.db.resum.und.utilities.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.und.utilities.Pair;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import sa.edu.kaust.grami.und.dataStructures.UnEdge;

public class DFSSearch {

    private Variable[] variables;
    private Variable[] result;
    private HashMap<String, MNIEdgeSet> resultEdgeSet;
    private HashMap<String, HashSet<String>> edgeAutomorphisms;
    private int numEdges;
    private ArrayList<HashSet<Integer>[]> maxSupporters;
    private int resultCounter = 0;
    private HashSet<Integer> visitedVariables;
    private SearchOrder sOrder;
    private int minFreqThreshold;
    private Query qry;
    private HashIntObjMap<HashSet<Integer>> nonCandidates;
    private double[] AVGScores;

    public double[] getAVGScores() {
        return AVGScores;
    }

    public HashIntObjMap<HashSet<Integer>> getNonCandidates() {
        return nonCandidates;
    }

    public DFSSearch(ConstraintGraph cg, int minFreqThreshold, HashIntObjMap<HashSet<Integer>> nonCands, int numEdges) {
        if (!Settings.CACHING) {
            nonCandidates = Util.clone(nonCands);
        } else {
            nonCandidates = nonCands;
        }

        this.minFreqThreshold = minFreqThreshold;
        this.numEdges = numEdges;
        variables = cg.getVariables();
        qry = cg.getQuery();
        result = new Variable[variables.length];
        resultEdgeSet = (Settings.score < 4 ? null : new HashMap<String, MNIEdgeSet>());
        edgeAutomorphisms = (Settings.isAutomorphismOn) ? new HashMap<String, HashSet<String>>() : null;
        AVGScores = new double[Settings.actualNumOfEdgeWeights];
        maxSupporters = new ArrayList<HashSet<Integer>[]>(Settings.score < 4 ? variables.length : 1);

        for (int i = 0; i < variables.length; i++) {
            HashMap<Integer, GNode> list = new HashMap<Integer, GNode>();
            result[i] = new Variable(variables[i].getID(), variables[i].getLabel(), list, variables[i].getDistanceConstrainedWith());
            if (Settings.score < 4) {
                maxSupporters.add(i, new HashSet[Settings.actualNumOfEdgeWeights]);
                final int z = i;
                IntStream.range(0, Settings.actualNumOfEdgeWeights).parallel()
                        .forEach(index -> maxSupporters.get(z)[index] = new HashSet<Integer>());
            }
        }
        visitedVariables = new HashSet<Integer>();
        sOrder = new SearchOrder(variables.length);
    }

    //for automorphisms and non-cached search
    public DFSSearch(SPpruner sp, Query qry, int minFreqThreshold, int numEdges) {
        this.minFreqThreshold = minFreqThreshold;
        this.numEdges = numEdges;
        nonCandidates = HashIntObjMaps.<HashSet<Integer>>newUpdatableMap();
        variables = sp.getVariables();
        this.qry = qry;
        result = new Variable[variables.length];
        resultEdgeSet = (Settings.score < 4 ? null : new HashMap<String, MNIEdgeSet>());
        edgeAutomorphisms = (Settings.isAutomorphismOn) ? new HashMap<String, HashSet<String>>() : null;
        AVGScores = new double[Settings.actualNumOfEdgeWeights];
        maxSupporters = new ArrayList<HashSet<Integer>[]>(Settings.score < 4 ? variables.length : 1);

        for (int i = 0; i < variables.length; i++) {
            HashMap<Integer, GNode> list = new HashMap<Integer, GNode>();
            result[i] = new Variable(variables[i].getID(), variables[i].getLabel(), list, variables[i].getDistanceConstrainedWith());
            if (Settings.score < 4) {
                maxSupporters.add(i, new HashSet[Settings.actualNumOfEdgeWeights]);
                final int z = i;
                IntStream.range(0, Settings.actualNumOfEdgeWeights).parallel()
                        .forEach(index -> maxSupporters.get(z)[index] = new HashSet<Integer>());
            }
        }
        visitedVariables = new HashSet<Integer>();
        sOrder = new SearchOrder(variables.length);
    }

    //if returns same index should search in it!!
    public int hasBeenPrecomputed(Variable[] autos, int[] preComputed, int index) {
        HashMap<Integer, GNode> list = autos[index].getList();
        for (int nodeIndex : list.keySet()) {
            if (preComputed[nodeIndex] == 1) {
                return nodeIndex;
            }
        }
        return index; //else return the same index
    }

    public void searchExistances() {
        //fast check for the min size of all the domains
        int min = variables[0].getListSize();
        for (Variable variable : variables) {
            min = Math.min(min, variable.getListSize());
        }
        if (min < minFreqThreshold) {
            return;
        }
        //AUTOMORPHISMS
        Variable[] autos = null;
        Automorphism<Integer, Double> atm = null;
        int[] preComputed = null;
        if (Settings.isAutomorphismOn) {
            HPListGraph<Integer, Double> listGraph = qry.getListGraph();
            preComputed = new int[variables.length];
            for (int i = 0; i < preComputed.length; i++) {
                preComputed[i] = 0;
            }
            atm = new Automorphism<Integer, Double>(listGraph);
            autos = atm.getResult();
            if (atm.hasAutomorphisms() && Settings.score == 4) {
                for (Entry<String, MNIEdgeSet> e : atm.getResultEdges().entrySet()) {
                    String[] endpoints = e.getKey().split("-");
                    int src = Integer.parseInt(endpoints[0]);
                    int dst = Integer.parseInt(endpoints[2]);
                    Set<Integer> srcEqui = autos[src].getList().keySet();
                    Set<Integer> dstEqui = autos[dst].getList().keySet();
                    for (int s : srcEqui) {
                        for (int d : dstEqui) {
                            int newS, newD;
                            if (s <= d) {
                                newS = s;
                                newD = d;
                            } else {
                                newS = d;
                                newD = s;
                            }
                            if (atm.getResultEdges().containsKey(newS + "-" + endpoints[1] + "-" + newD)) {
                                HashSet<String> currentEqui = edgeAutomorphisms.getOrDefault(e.getKey(), new HashSet<String>());
                                currentEqui.add(newS + "-" + endpoints[1] + "-" + newD);
                                edgeAutomorphisms.put(e.getKey(), currentEqui);
                            }
                        }
                    }
                }
            }
        }
        //SEARCH
        for (int i = variables.length - 1; i >= 0; i--) {
            boolean search = true;
            if (Settings.isAutomorphismOn && atm.hasAutomorphisms()) {
                int preIndex = hasBeenPrecomputed(autos, preComputed, i);
                if (i != preIndex) {
                    search = false;
                    variables[i].setList((HashMap<Integer, GNode>) variables[preIndex].getList().clone());
                    result[i].setList((HashMap<Integer, GNode>) result[preIndex].getList().clone());
                    if (Settings.score < 4) {
                        final int z = i;
                        IntStream.range(0, Settings.actualNumOfEdgeWeights)
                                .parallel()
                                .forEach(index -> {
                                    maxSupporters.get(z)[index]
                                            = (HashSet<Integer>) maxSupporters.get(preIndex)[index].clone();
                                });
                    }
                }
            }
            if (search == true) {
                //fast check
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
                HashMap<Integer, GNode> firstList = firstVB.getList();
                AssignmentInstance instance = new AssignmentInstance(variables.length, numEdges);
                for (Iterator<GNode> iterator = firstList.values().iterator(); iterator.hasNext();) {
                    GNode firstNode = iterator.next();
                    sOrder.reset();
                    instance.assign(firstVB.getID(), firstNode);

                    int value = -1;
                    value = searchExistances(instance);

                    //ASSIGNMENT NOT VALID
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
                    //VALID ASSIGNMENT
                    if (value == -1) {
                        final boolean[] maxSupporting;
                        if (Settings.relevance < 0) {
                            maxSupporting = new boolean[Settings.actualNumOfEdgeWeights];
                            Arrays.fill(maxSupporting, true);
                        } else {
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
                        }
                        for (int j = 0; j < variables.length; j++) {
                            GNode assignedNode = instance.getAssignment(j);
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
                                }
                            }
                        }
                        if (Settings.score == 4) {
                            instance.getEdgeAssignments().entrySet().stream().forEach((e) -> {
                                MNIEdgeSet currentSet = resultEdgeSet.getOrDefault(e.getKey(), new MNIEdgeSet());
                                currentSet.addEdge(e.getValue());
                                resultEdgeSet.put(e.getKey(), currentSet);
                            });
                        }
                    }
                    instance.clear();
                }
                //EARLY TERMINATION
                if (result[index].getList().size() < minFreqThreshold) {
                    return;
                }
            }
            resetVariableVisitingOrder();
            AC_3_New(variables, minFreqThreshold);
            if (Settings.isAutomorphismOn) {
                preComputed[i] = 1;
            }
        }
    }

    public boolean searchParticularExistance(AssignmentInstance instance, int orderINdex) {
        sOrder.reset();
        setVariableVisitingOrder(orderINdex);
        int value = searchExistances(instance);
        return (value != -2);
    }

    private int searchExistances(AssignmentInstance instance) {
        int index = sOrder.getNext();
        if (index != -1) {
            Variable currentVB = variables[index];
            ArrayList<UnEdge<Integer, Double>> constrainingVariables = currentVB.getDistanceConstrainedWith();
            ArrayList<Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>> candidates = new ArrayList<Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>>();
            ArrayList<VariableCandidates> variableCandidates = new ArrayList<VariableCandidates>();

            //check Validty with constraintVariables
            for (int i = 0; i < constrainingVariables.size(); i++) {
                Variable cnVariable = variables[constrainingVariables.get(i).getNodeID()];
                Double edgeLabel = constrainingVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
                if (cnVariableInstance != null) {
                    ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr = cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel);
                    candidates.add(new Pair(cnVariable.getID(), tempArr));
                    variableCandidates.add(new VariableCandidates(cnVariableIndex, tempArr));
                }
            }
            ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>> finalCandidates = Util.getIntersection(candidates);
            Collections.sort(finalCandidates, (Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>> o1, Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>> o2) -> {
                double[] first = Arrays.copyOf(o1.getB().getMaxWeights(), Settings.actualNumOfEdgeWeights);
                double[] second = Arrays.copyOf(o2.getB().getMaxWeights(), Settings.actualNumOfEdgeWeights);
                Arrays.sort(first);
                Arrays.sort(second);
                return -Double.compare(first[Settings.actualNumOfEdgeWeights / 2], second[Settings.actualNumOfEdgeWeights / 2]);
            });
            if (finalCandidates.size() == 0) {
                //learn the new constraints !!!
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

            for (int i = 0; i < finalCandidates.size(); i++) {
                int candidateIndex = finalCandidates.get(i).getB().getNodeID();
                GNode candidateNode = currentVB.getList().get(candidateIndex);
                if (candidateNode != null) {
                    instance.assign(currentVB.getID(), candidateNode);

                    //check identity Validity
                    if (AssignmentInstance.ensureIDValidty(instance)) {
                        //proceed with next
                        hasResult = searchExistances(instance);
                        if (hasResult == -1) {
                            for (int otherVB : finalCandidates.get(i).getA()) {
                                Double label = finalCandidates.get(i).getB().getEdgeLabel();
                                String edgeIndex = (currentVB.getID() < otherVB) ? (currentVB.getID() + "-" + label + "-" + otherVB)
                                        : (otherVB + "-" + label + "-" + currentVB.getID());
                                instance.assignEdge(finalCandidates.get(i).getB(), edgeIndex);
                            }
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
            //after finishing... step back to previous state
            sOrder.stepBack();
            instance.deAssign(currentVB.getID());
        } else {
            return -1; //return True
        }
        return -2; //return False
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
        ArrayList<UnEdge<Integer, Double>> constrains = vb.getDistanceConstrainedWith();
        for (int i = 0; i < constrains.size(); i++) {
            Variable currentVB = variables[constrains.get(i).getNodeID()];
            if (!visitedVariables.contains(currentVB.getID())) {
                visitedVariables.add(currentVB.getID());
                sOrder.addNext(currentVB.getID());
                searchOrder(currentVB);
            }
        }
    }

    public int getMNISupport() {
        int min = result[0].getListSize();
        for (int i = 1; i < result.length; i++) {
            if (min > result[i].getListSize()) {
                min = result[i].getListSize();
            }
        }
        return min;
    }

    public double[] getMNIRelSupports() {
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

    public Variable[] getResultVariables() {
        return result;
    }

    public HashMap<String, MNIEdgeSet> getResultEdges() {
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

    // Automorphisms and non-cached search
    private void search(AssignmentInstance instance) {
        int index = sOrder.getNext();

        if (index != -1) {
            Variable currentVB = variables[index];
            ArrayList<UnEdge<Integer, Double>> constrainingVariables = currentVB.getDistanceConstrainedWith();
            ArrayList<Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>> candidates
                    = new ArrayList<Pair<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>>>();
            ArrayList<VariableCandidates> variableCandidates = new ArrayList<VariableCandidates>();

            //check Validty with constraintVariables
            for (int i = 0; i < constrainingVariables.size(); i++) {
                Variable cnVariable = variables[constrainingVariables.get(i).getNodeID()];
                double edgeLabel = constrainingVariables.get(i).getEdgeLabel();
                int cnVariableIndex = cnVariable.getID();
                GNode cnVariableInstance = instance.getAssignment(cnVariableIndex);
                if (cnVariableInstance != null) {
                    ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> tempArr = cnVariableInstance.getRechableWithNodeIDs(currentVB.getLabel(), edgeLabel);
                    candidates.add(new Pair(cnVariable.getID(), tempArr));
                    variableCandidates.add(new VariableCandidates(cnVariableIndex, tempArr));
                }
            }

            ArrayList<Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>>> finalCandidates = Util.getIntersection(candidates);
            Collections.sort(finalCandidates, (Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>> o1, Pair<HashSet<Integer>, MultiUserWeightedEdge<Integer, Double, double[]>> o2) -> {
                double[] first = Arrays.copyOf(o1.getB().getMaxWeights(), Settings.actualNumOfEdgeWeights);
                double[] second = Arrays.copyOf(o2.getB().getMaxWeights(), Settings.actualNumOfEdgeWeights);
                Arrays.sort(first);
                Arrays.sort(second);
                return -Double.compare(first[Settings.actualNumOfEdgeWeights / 2], second[Settings.actualNumOfEdgeWeights / 2]);
            });
            //end check Validity with constraintVariables
            for (int i = 0; i < finalCandidates.size(); i++) {
                int candidateIndex = finalCandidates.get(i).getB().getNodeID();
                GNode candidateNode = currentVB.getList().get(candidateIndex);
                if (candidateNode != null) {
                    instance.assign(currentVB.getID(), candidateNode);
                    //check identity Validity
                    if (AssignmentInstance.ensureIDValidty(instance)) {
                        for (int otherVB : finalCandidates.get(i).getA()) {
                            Double label = finalCandidates.get(i).getB().getEdgeLabel();
                            String edgeIndex = (currentVB.getID() < otherVB) ? (currentVB.getID() + "-" + label + "-" + otherVB)
                                    : (otherVB + "-" + label + "-" + currentVB.getID());
                            instance.assignEdge(finalCandidates.get(i).getB(), edgeIndex);
                        }
                        //proceed with next
                        search(instance);
                    } else {
                        instance.deAssign(currentVB.getID());
                    }
                }
            }
            //Step back to state before
            sOrder.stepBack();
            instance.deAssign(currentVB.getID());
        } else {
            //VALID ASSIGNMENT
            resultCounter++;
            for (int i = 0; i < instance.getAssignmentSize(); i++) {
                GNode nodeInstance = instance.getAssignment(i);
                if (!result[i].getList().containsKey(nodeInstance.getID())) {
                    result[i].getList().put(nodeInstance.getID(), nodeInstance);
                }
            }
            if (Settings.score == 4) {
                instance.getEdgeAssignments().entrySet().stream().forEach((e) -> {
                    MNIEdgeSet currentSet = resultEdgeSet.getOrDefault(e.getKey(), new MNIEdgeSet());
                    currentSet.addEdge(e.getValue());
                    resultEdgeSet.put(e.getKey(), currentSet);
                });
            }
        }
    }

    public void searchAll() {
        setVariableVisitingOrder(getMaxDegreeVariableIndex()); //set variable visit order
        int index = -1;

        index = sOrder.getNext();
        Variable firstVB = variables[index];
        HashMap<Integer, GNode> firstList = firstVB.getList();

        AssignmentInstance instance = new AssignmentInstance(variables.length, numEdges);

        for (GNode firstNode : firstList.values()) {
            instance.assign(firstVB.getID(), firstNode);
            search(instance);
        }
    }

    public Variable[] getVariables() {
        return this.variables;
    }

    //AC_3
    private void AC_3_New(Variable[] input, int freqThreshold) {
        LinkedList<VariablePair> Q = new LinkedList<VariablePair>();
        HashSet<String> contains = new HashSet<String>();
        VariablePair vp;

        //initialize...
        for (Variable currentVar : input) {
            ArrayList<UnEdge<Integer, Double>> list = currentVar.getDistanceConstrainedWith();
            for (int j = 0; j < list.size(); j++) {
                Variable consVar = variables[list.get(j).getNodeID()];
                vp = new VariablePair(currentVar, consVar, list.get(j).getEdgeLabel());
                insertInOrder(Q, vp);
                contains.add(vp.getString());
            }
        }

        while (!Q.isEmpty()) {
            vp = Q.poll();

            contains.remove(vp.getString());
            Variable v1 = vp.v1;
            Variable v2 = vp.v2;
            if (v1.getListSize() < freqThreshold || v2.getListSize() < freqThreshold) {
                return;
            }
            int oldV1Size = v1.getListSize();
            int oldV2Size = v2.getListSize();
            refine_Newest(v1, v2, vp.edgeLabel);
            if (oldV1Size != v1.getListSize()) {
                if (v1.getListSize() < freqThreshold) {
                    return;
                }
                //add to queue
                ArrayList<UnEdge<Integer, Double>> list = v1.getDistanceConstrainedWith();
                for (int j = 0; j < list.size(); j++) {
                    UnEdge<Integer, Double> tempMP = list.get(j);
                    Variable consVar = variables[tempMP.getNodeID()];
                    vp = new VariablePair(consVar, v1, tempMP.getEdgeLabel());
                    if (!contains.contains(vp.getString())) {
                        insertInOrder(Q, vp);
                        //add new variables at the begining
                        contains.add(vp.getString());
                    }
                }
            }
            if (oldV2Size != v2.getListSize()) {
                if (v2.getListSize() < freqThreshold) {
                    return;
                }
                //add to queue
                ArrayList<UnEdge<Integer, Double>> list = v2.getDistanceConstrainedWith();
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

    //refine
    private void refine_Newest(Variable v1, Variable v2, double edgeLabel) {
        HashMap<Integer, GNode> listA, listB;

        int labelB = v2.getLabel();//lebel of my neighbor
        listA = v1.getList();//the first column
        listB = v2.getList();//the second column
        HashMap<Integer, GNode> newList = new HashMap<Integer, GNode>();//the newly assigned first column
        HashMap<Integer, GNode> newReachableListB = new HashMap<Integer, GNode>();//the newly asigned second column
        //go over the first column
        for (GNode n1 : listA.values()) {
            //get the current node
            if (n1.hasReachableNodes() == false) {
                continue;
            }
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> neighbors = n1.getRechableWithNodeIDs(labelB, edgeLabel);//get a list of current node's neighbors
            for (MultiUserWeightedEdge<Integer, Double, double[]> mp : neighbors) {
                //get current neighbor details
                //check the second column if it contains the current neighbor node
                if (listB.containsKey(mp.getNodeID())) {
                    //if true, put the current node in the first column, and the neighbor node in the second column
                    newList.put(n1.getID(), n1);
                    newReachableListB.put(mp.getNodeID(), listB.get(mp.getNodeID()));
                }
            } //go over each neighbor
        }

        //set the newly assigned columns
        v1.setList(newList);
        v2.setList(newReachableListB);
    }

    //insert vp into order according to their variable values length
    private void insertInOrder(LinkedList<VariablePair> Q, VariablePair vp) {
        int i = 0;
        for (VariablePair tempVP : Q) {
            if (tempVP.getMinValuesLength() > vp.getMinValuesLength()) {
                Q.add(i, vp);
                return;
            }
            i++;
        }
        Q.add(i, vp);
    }

    public double[] computeAVGScore() {
        //Compute Scores
        double[] scores = new double[Settings.actualNumOfEdgeWeights];
        int k = getMNISupport();
        if (k >= Settings.frequency) {
            //Compute Automorphic Edge Domains
            HashMap<String, Integer> edgeIDXs = new HashMap<String, Integer>(resultEdgeSet.size());
            ArrayList<MNIEdgeSet> automorphicEdgeDomains = new ArrayList<MNIEdgeSet>(resultEdgeSet.size());

            for (Entry<String, HashSet<String>> edge : edgeAutomorphisms.entrySet()) {
                if (!edgeIDXs.containsKey(edge.getKey())) {
                    edgeIDXs.put(edge.getKey(), automorphicEdgeDomains.size());
                    MNIEdgeSet automorphicDomain = new MNIEdgeSet();
                    edge.getValue().stream()
                            .map((automorphicEdge) -> {
                                edgeIDXs.put(automorphicEdge, automorphicEdgeDomains.size());
                                return automorphicEdge;
                            })
                            .map((automorphicEdge) -> {
                                automorphicDomain.addAllEdges(resultEdgeSet.get(automorphicEdge).getEdgeSets());
                                return automorphicEdge;
                            })
                            .filter((automorphicEdge) -> (edgeAutomorphisms.get(automorphicEdge).contains(automorphicEdge)))
                            .forEach((automorphicEdge) -> {
                                automorphicDomain.addAllEdges(resultEdgeSet.get(automorphicEdge).getEdgeSets());
                            });
                    automorphicEdgeDomains.add(automorphicDomain);
                }
            }
            // Compute scores
            resultEdgeSet.keySet().stream()
                    .map((patternEdge) -> automorphicEdgeDomains.get(edgeIDXs.get(patternEdge)).getSumsofTopKWeights(k))
                    .forEach((thisEdgeSums) -> {
                        for (int u = 0; u < Settings.actualNumOfEdgeWeights; u++) {
                            scores[u] += thisEdgeSums[u];
                        }
                    });
        }
        AVGScores = scores;
        return scores;
    }

}
