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
package sa.edu.kaust.grami.automorphism;

import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.HashMap;

import sa.edu.kaust.grami.pruning.SPpruner;

import sa.edu.kaust.grami.csp.DFSSearch;
import sa.edu.kaust.grami.csp.MNIEdgeSet;
import sa.edu.kaust.grami.csp.Variable;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.IntIterator;
import sa.edu.kaust.grami.dataStructures.Query;
import sa.edu.kaust.grami.dataStructures.GNode;

public class Automorphism<NodeType, EdgeType> {

    private Variable[] result;
    private MNIEdgeSet[] resultEdges;
    private HashMap<Integer, GNode> nodes;
    private final int resultCounter;

    public Variable[] getResult() {
        return result;
    }

    public MNIEdgeSet[] getResultEdges() {
        return resultEdges;
    }

    public Automorphism(HPListGraph<NodeType, EdgeType> graph) {
        Integer numberOfNodes = graph.getNodeCount();
        result = new Variable[numberOfNodes];
        resultEdges = (Settings.score < 4 ? null : new MNIEdgeSet[numberOfNodes]);
        nodes = new HashMap<Integer, GNode>();

        Query qry = new Query((HPListGraph<Integer, Double>) graph);

        //create my nodes first !!
        for (int i = 0; i < numberOfNodes; i++) {
            GNode newNode = new GNode(i, (Integer) graph.getNodeLabel(i));
            nodes.put(newNode.getID(), newNode);
        }
        for (int i = 0; i < numberOfNodes; i++) {
            GNode currentNode = nodes.get(i);
            for (IntIterator currentEdges = graph.getEdgeIndices(i); currentEdges.hasNext();) {
                int edge = currentEdges.next();
                int direction = graph.getDirection(edge, i);
                int otherNodeIndex = graph.getOtherNode(edge, i);
                GNode otherNode = nodes.get(otherNodeIndex);
                switch (direction) {
                    case 1:
                        currentNode.addreachableNode(otherNode, Double.parseDouble(graph.getEdgeLabel(edge) + ""));
                        break;
                    case -1:
                        otherNode.addreachableNode(currentNode, Double.parseDouble(graph.getEdgeLabel(edge) + ""));
                        break;
                    case 0:
                        currentNode.addreachableNode(otherNode, Double.parseDouble(graph.getEdgeLabel(edge) + ""));
                        otherNode.addreachableNode(currentNode, Double.parseDouble(graph.getEdgeLabel(edge) + ""));
                        break;
                    default:
                        break;
                }
            }
        }
        SPpruner sp = new SPpruner();
        sp.getPrunedLists(nodes, qry);
        DFSSearch df = new DFSSearch(sp, qry, -1);
        df.searchAll();
        resultCounter = df.getResultCounter();
        result = df.getResultVariables();
        if (Settings.score == 4) {
            resultEdges = df.getResultEdges();
        }
    }

    public boolean hasAutomorphisms() {
        return (resultCounter != 1);
    }
}
