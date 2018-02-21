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
package sa.edu.kaust.grami.pruning;

import com.koloboke.collect.map.hash.HashIntObjMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import sa.edu.kaust.grami.csp.Variable;
import sa.edu.kaust.grami.csp.VariablePair;
import sa.edu.kaust.grami.dataStructures.ConnectedComponent;
import sa.edu.kaust.grami.dataStructures.Graph;
import sa.edu.kaust.grami.dataStructures.Query;
import sa.edu.kaust.grami.dataStructures.GNode;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

public class SPpruner {

    private Variable[] variables;

    public Variable[] getVariables() {
        return variables;
    }

    public void getPrunedLists(ArrayList<HashMap<Integer, GNode>> candidatesByNodeID, Query qry) {

        ArrayList<ConnectedComponent> cls = qry.getConnectedLabels();
        Integer numberOfNodes = qry.getListGraph().getNodeCount();
        //create the variables
        variables = new Variable[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            int label = qry.getListGraph().getNodeLabel(i);
            variables[i] = new Variable(i, label, candidatesByNodeID.get(i), null, null);
        }

        for (int i = 0; i < cls.size(); i++) {
            ConnectedComponent c = cls.get(i);
            int nodeA = c.getIndexA();
            int nodeB = c.getIndexB();
            variables[nodeA].addConstraintWith(nodeB, c.getEdgeLabel(), c.getEdgeWeights());
            variables[nodeB].addConstrainedBy(nodeA, c.getEdgeLabel(), c.getEdgeWeights());
        }

        AC_3_New(variables, -1);
    }

    public void getPrunedLists(HashMap<Integer, GNode> nodesMap, Query qry) {
        // {Label -> (NodeId -> Node)} for each variable, the set of nodes of the graph having the same label
        HashMap<Integer, HashMap<Integer, GNode>> pruned = new HashMap<Integer, HashMap<Integer, GNode>>();
        ArrayList<ConnectedComponent> cls = qry.getConnectedLabels();
        Integer numberOfNodes = qry.getListGraph().getNodeCount();

        //refine according to nodeLabels
        for (int i = 0; i < numberOfNodes; i++) {
            pruned.put(i, (HashMap<Integer, GNode>) nodesMap.clone());
        }

        //refine according to degree
        //{nodeID -> (label, degree)} for each node, for each label, how many out-neighbors with that label
        HashMap<Integer, HashMap<Integer, Integer>> nodeOutLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();
        //{nodeID -> (label, degree)} for each node, for each label, how many in-neighbors with that label
        HashMap<Integer, HashMap<Integer, Integer>> nodeInLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();

        for (int i = 0; i < cls.size(); i++) {
            ConnectedComponent c = cls.get(i);
            int nodeA = c.getIndexA();
            int nodeB = c.getIndexB();
            HashMap<Integer, Integer> nodeAmap = nodeOutLabelDegrees.get(nodeA);
            HashMap<Integer, Integer> nodeBmap = nodeInLabelDegrees.get(nodeB);
            if (nodeAmap == null) {
                nodeAmap = new HashMap<Integer, Integer>();
                nodeOutLabelDegrees.put(nodeA, nodeAmap);
            }
            if (nodeBmap == null) {
                nodeBmap = new HashMap<Integer, Integer>();
                nodeInLabelDegrees.put(nodeB, nodeBmap);
            }

            Integer degreeA = nodeAmap.get(c.getLabelB());
            if (degreeA == null) {
                degreeA = 0;
            }
            Integer degreeB = nodeBmap.get(c.getLabelA());
            if (degreeB == null) {
                degreeB = 0;
            }

            nodeAmap.put(c.getLabelB(), degreeA + 1);
            nodeBmap.put(c.getLabelA(), degreeB + 1);
        }

        for (int i = 0; i < numberOfNodes; i++) {
            HashMap<Integer, Integer> degreeOutCons = nodeOutLabelDegrees.get(i);
            HashMap<Integer, Integer> degreeInCons = nodeInLabelDegrees.get(i);

            HashMap<Integer, GNode> candidates = pruned.get(i);
            boolean isValidNode = true;

            for (Iterator<Entry<Integer, GNode>> it = candidates.entrySet().iterator(); it.hasNext();) {
                Entry<Integer, GNode> nodeEntry = it.next();
                GNode node = nodeEntry.getValue();
                isValidNode = true;
                if (degreeOutCons != null) {
                    // check outDegree
                    for (Entry<Integer, Integer> entry : degreeOutCons.entrySet()) {
                        int label = entry.getKey();
                        int degree = entry.getValue();

                        if (node.getOutDegree(label) < degree) {
                            isValidNode = false;
                            break;
                        }
                    }
                }
                if (isValidNode && degreeInCons != null) {
                    // check inDegree
                    for (Entry<Integer, Integer> entry : degreeInCons.entrySet()) {
                        int label = entry.getKey();
                        int degree = entry.getValue();

                        if (node.getinDegree(label) < degree) {
                            isValidNode = false;
                            break;
                        }
                    }
                }
                if (isValidNode == false) {
                    it.remove();
                }
            }
        }

        //create the variables
        variables = new Variable[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            int label = qry.getListGraph().getNodeLabel(i);
            variables[i] = new Variable(i, label, pruned.get(i), null, null);
        }
        for (int i = 0; i < cls.size(); i++) {
            ConnectedComponent c = cls.get(i);
            int nodeA = c.getIndexA();
            int nodeB = c.getIndexB();
            variables[nodeA].addConstraintWith(nodeB, c.getEdgeLabel(), c.getEdgeWeights());
            variables[nodeB].addConstrainedBy(nodeA, c.getEdgeLabel(), c.getEdgeWeights());
        }

        AC_3_New(variables, -1);
    }

    public void getPrunedLists(Graph graph, Query qry, HashIntObjMap<HashSet<Integer>> nonCandidates) {
        HashMap<Integer, HashMap<Integer, GNode>> pruned = new HashMap<Integer, HashMap<Integer, GNode>>();// {QueryID -> (NodeID->NODE)}
        ArrayList<ConnectedComponent> cls = qry.getConnectedLabels();
        Integer numberOfNodes = qry.getListGraph().getNodeCount();

        //refine according to nodeLabels
        for (int i = 0; i < numberOfNodes; i++) {
            int label = qry.getListGraph().getNodeLabel(i);
            pruned.put(i, (HashMap<Integer, GNode>) graph.getNodesByLabel().get(label).clone());
        }

        for (Entry<Integer, HashSet<Integer>> entry : nonCandidates.entrySet()) {
            int qryID = entry.getKey();
            HashMap<Integer, GNode> prunedCands = pruned.get(qryID);
            HashSet<Integer> nonCands = entry.getValue();
            for (Integer integer : nonCands) {
                prunedCands.remove(integer);
            }
        }

        //refine according to degree !!
        HashMap<Integer, HashMap<Integer, Integer>> nodeOutLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();// {nodeID-->(Label,Degree)}
        HashMap<Integer, HashMap<Integer, Integer>> nodeInLabelDegrees = new HashMap<Integer, HashMap<Integer, Integer>>();

        for (int i = 0; i < cls.size(); i++) {
            ConnectedComponent c = cls.get(i);
            int nodeA = c.getIndexA();
            int nodeB = c.getIndexB();
            HashMap<Integer, Integer> nodeAmap = nodeOutLabelDegrees.get(nodeA);
            HashMap<Integer, Integer> nodeBmap = nodeInLabelDegrees.get(nodeB);
            if (nodeAmap == null) {
                nodeAmap = new HashMap<Integer, Integer>();
                nodeOutLabelDegrees.put(nodeA, nodeAmap);
            }
            if (nodeBmap == null) {
                nodeBmap = new HashMap<Integer, Integer>();
                nodeInLabelDegrees.put(nodeB, nodeBmap);
            }

            Integer degreeA = nodeAmap.get(c.getLabelB());
            if (degreeA == null) {
                degreeA = 0;
            }
            Integer degreeB = nodeBmap.get(c.getLabelA());
            if (degreeB == null) {
                degreeB = 0;
            }

            nodeAmap.put(c.getLabelB(), degreeA + 1);
            nodeBmap.put(c.getLabelA(), degreeB + 1);
        }

        for (int i = 0; i < numberOfNodes; i++) {
            HashMap<Integer, Integer> degreeOutCons = nodeOutLabelDegrees.get(i);
            HashMap<Integer, Integer> degreeInCons = nodeInLabelDegrees.get(i);

            HashMap<Integer, GNode> candidates = pruned.get(i);
            boolean isValidNode = true;

            for (Iterator<Entry<Integer, GNode>> it = candidates.entrySet().iterator(); it.hasNext();) {
                Entry<Integer, GNode> nodeEntry = it.next();
                GNode node = nodeEntry.getValue();
                isValidNode = true;
                if (degreeOutCons != null) {
                    for (Entry<Integer, Integer> entry : degreeOutCons.entrySet()) {
                        int label = entry.getKey();
                        int degree = entry.getValue();

                        if (node.getOutDegree(label) < degree) {
                            isValidNode = false;
                            break;
                        }
                    }
                }
                if (isValidNode && degreeInCons != null) {
                    for (Entry<Integer, Integer> entry : degreeInCons.entrySet()) {
                        int label = entry.getKey();
                        int degree = entry.getValue();

                        if (node.getinDegree(label) < degree) {
                            isValidNode = false;
                            break;
                        }
                    }
                }
                if (isValidNode == false) {
                    it.remove();
                }
            }
        }

        //create the variables
        variables = new Variable[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            int label = qry.getListGraph().getNodeLabel(i);
            variables[i] = new Variable(i, label, pruned.get(i), null, null);
        }

        for (int i = 0; i < cls.size(); i++) {
            ConnectedComponent c = cls.get(i);
            int nodeA = c.getIndexA();
            int nodeB = c.getIndexB();
            variables[nodeA].addConstraintWith(nodeB, c.getEdgeLabel(), c.getEdgeWeights());
            variables[nodeB].addConstrainedBy(nodeA, c.getEdgeLabel(), c.getEdgeWeights());
        }
    }

    //insert vp into order according to their variable values length
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

    private void AC_3_New(Variable[] input, int freqThreshold) {
        LinkedList<VariablePair> Q = new LinkedList<VariablePair>();
        HashSet<String> contains = new HashSet<String>();
        VariablePair vp;
        //initialize...
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
            refine_Newest(v1, v2, vp.edgeLabel, freqThreshold);
            if (oldV1Size != v1.getListSize()) {
                if (v1.getListSize() < freqThreshold) {
                    return;
                }
                //add to queue
                ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> list = v1.getDistanceConstrainedBy();
                for (int j = 0; j < list.size(); j++) {
                    Integer tempMP = list.get(j).getNodeID();
                    Variable consVar = variables[tempMP];
                    vp = new VariablePair(consVar, v1, list.get(j).getEdgeLabel());
                    if (!contains.contains(vp.getString())) {
                        insertInOrder(Q, vp);
                        //add new variables at the beginning
                        contains.add(vp.getString());
                    }
                }
            }
            if (oldV2Size != v2.getListSize()) {
                if (v2.getListSize() < freqThreshold) {
                    return;
                }
                //add to queue
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

    private void refine_Newest(Variable v1, Variable v2, double edgeLabel, int freqThreshold) {
        HashMap<Integer, GNode> listA, listB;

        int labelB = v2.getLabel();// label of my neighbor
        listA = v1.getList();// candidate nodes of the first variable
        listB = v2.getList();// candidate nodes of the second variable 
        HashMap<Integer, GNode> newList = new HashMap<Integer, GNode>();// the new candidate list of the first variable
        HashMap<Integer, GNode> newReachableListB = new HashMap<Integer, GNode>();// the new candidate list of the second variable
        for (GNode n1 : listA.values()) {
            // get the current candidate node
            if (n1.hasReachableNodes() == false) {
                continue;
            }
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> neighbors = n1.getRechableWithNodeIDs(labelB, edgeLabel);// get a list of current node's neighbors
            if (neighbors == null) {
                continue;
            }
            for (MultiUserWeightedEdge<Integer, Double, double[]> mp : neighbors) {
                // get current neighbor details
                //check the candidate list of the second variable to see if it contains the current neighbor node
                if (listB.containsKey(mp.getNodeID())) {
                    //if true, put the current node in the first column, and the neighbor node in the second column
                    newList.put(n1.getID(), n1);
                    newReachableListB.put(mp.getNodeID(), listB.get(mp.getNodeID()));
                }
            } // go over each neighbor
        }

        //set the newly assigned columns
        v1.setList(newList);
        v2.setList(newReachableListB);
    }
}
