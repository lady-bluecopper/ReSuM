/**
 * created May 25, 2006
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 *
 * This file is part of parsemis.
 *
 * Licence:
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package eu.unitn.disi.db.resum.search;

import eu.unitn.disi.db.resum.multithread.GThreadEnvironment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import sa.edu.kaust.grami.csp.Variable;
import sa.edu.kaust.grami.dataStructures.DFSCode;
import sa.edu.kaust.grami.dataStructures.Extension;
import sa.edu.kaust.grami.dataStructures.GSpanEdge;
import sa.edu.kaust.grami.dataStructures.GSpanExtension;
import sa.edu.kaust.grami.dataStructures.Graph;
import sa.edu.kaust.grami.dataStructures.HPGraph;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.HPMutableGraph;
import sa.edu.kaust.grami.dataStructures.StaticData;
import sa.edu.kaust.grami.dataStructures.freqComparator;
import eu.unitn.disi.db.resum.utilities.Util;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;

/**
 * Represents the right most extension of gSpan.
 * <p>
 * For gSpan just backward edges from the last inserted node, or forward edges
 * staring in nodes of the right most path (path of forward edges between the
 * "root" node to the last inserted node) are relevant.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class RightMostExtension<NodeType, EdgeType> extends
        GenerationPartialStep<NodeType, EdgeType> {
    
    protected final GThreadEnvironment<NodeType, EdgeType> tenv;

    public static int counter = 0;
    
    private final Map<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>> children;

    /**
     * creates a new pruning
     *
     * @param next the next step of the generation chain
     * @param tenv
     */
    public RightMostExtension(final GenerationPartialStep<NodeType, EdgeType> next,
            final GThreadEnvironment<NodeType, EdgeType> tenv) {
        super(next);
        this.tenv = tenv;
        this.children = new TreeMap<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>>();
    }

    /**
     * includes the found extension to the corresponding fragment
     *
     * @param gEdge
     * @param code
     * @param type
     */
    protected void add(final GSpanEdge<NodeType, EdgeType> gEdge,
            final DFSCode<NodeType, EdgeType> code, int type) {
        // search corresponding extension
        GSpanExtension<NodeType, EdgeType> ext = children.get(gEdge);

        if (ext == null) {
            int labelA = gEdge.getThelabelA();
            int labelB = gEdge.getThelabelB();
            double edgeLabel = gEdge.getEdgeLabel();
            String hn;
            if (labelA < labelB) {
                hn = labelA + "_" + edgeLabel + "_" + labelB;
            } else {
                hn = labelB + "_" + edgeLabel + "_" + labelA;
            }
            // TODO edge already used or not valid
            if (StaticData.hashedEdges.get(hn) == null) {
                return;
            }
            // create new extension
            HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code.getHPlistGraph().clone();
            gEdge.addTo(ng);
            ext = new GSpanExtension<NodeType, EdgeType>(tenv);
            ext.edge = gEdge;
            ext.frag = new DFSCode<NodeType, EdgeType>(tenv, code.getSortedFreqLabels(), 
                    code.getSingleGraph(), 
                    Util.clone(code.getNonCandidates())).set((HPListGraph<NodeType, EdgeType>) ng,code.getFirst(), code.getLast(), code.getParents());
            ext.frag = (DFSCode<NodeType, EdgeType>) code.extend(ext);

            children.put(gEdge, ext);
        } else {
            gEdge.release(tenv);
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
     */
    @Override
    public void call(final SearchLatticeNode<NodeType, EdgeType> node,
            final Collection<Extension<NodeType, EdgeType>> extensions) {
        // just give YOUR extensions to the next step
        extensions.clear();
        extensions.addAll(children.values());
        callNext(node, extensions);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.Embedding)
     */
    @Override
    public void call(final SearchLatticeNode<NodeType, EdgeType> node) {
        counter++;
        extend((DFSCode<NodeType, EdgeType>) node);
        callNext(node);
    }

    protected final void extend(final DFSCode<NodeType, EdgeType> code) {
        
//        System.out.println("EXTENDING: " + code.toString());

        final HPGraph<NodeType, EdgeType> subGraph = code.getHPlistGraph();

        final int lastNode = subGraph.getNodeCount() - 1;

        Graph singleGraph = code.getSingleGraph();

        ArrayList<Double> freqEdgeLabels = singleGraph.getFreqEdgeLabels();
        
        ArrayList<Integer> sortedFreqLabels = singleGraph.getSortedFreqLabels();

        Variable[] vrs = code.getCurrentVariables();
        // find extensions of the last node;
        {
            Variable lastVariable = vrs[lastNode];

            HashSet<Integer> labelDC = lastVariable.getLabelsDistanceConstrainedWith();

            ArrayList<Point> pairs = new ArrayList<Point>();
            for (int theLabelB : labelDC) {
                int index = sortedFreqLabels.indexOf(theLabelB);
                pairs.add(new Point(theLabelB, index));
            }
            Collections.sort(pairs, new freqComparator());
            //Forward Edges
            int theLabelA = lastVariable.getLabel();
            for (int i = 0; i < pairs.size(); i++) {
                Point currentPoint = pairs.get(i);
                int label = currentPoint.x;
                int index = currentPoint.y;

                for (int j = 0; j < freqEdgeLabels.size(); j++) {
                    final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                            .set(lastNode, lastNode + 1, sortedFreqLabels.indexOf(theLabelA), 
                                    freqEdgeLabels.get(j).intValue(), index, 1, theLabelA, label);
                    if ((code.getLast().compareTo(gEdge) < 0)) {
                        add(gEdge, code, 0);
                    } else {
                        gEdge.release(tenv);
                    }
                }
            }

            labelDC = lastVariable.getLabelsDistanceConstrainedBy();

            pairs = new ArrayList<Point>();
            for (int theLabelB : labelDC) {
                int index = sortedFreqLabels.indexOf(theLabelB);
                pairs.add(new Point(theLabelB, index));
            }
            Collections.sort(pairs, new freqComparator());

            //Forward Edges
            theLabelA = lastVariable.getLabel();
            for (int i = 0; i < pairs.size(); i++) {
                Point currentPoint = pairs.get(i);
                int label = currentPoint.x;
                int index = currentPoint.y;
                for (int j = 0; j < freqEdgeLabels.size(); j++) {
                    final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                            .set(lastNode, lastNode + 1, sortedFreqLabels.indexOf(theLabelA), 
                                    freqEdgeLabels.get(j).intValue(), index, -1, theLabelA, label);
                    if ((code.getLast().compareTo(gEdge) < 0)) {
                        add(gEdge, code, 0);
                    } else {
                        gEdge.release(tenv);
                    }
                }
            }

            //Backward Edges!! 
            //now pass by each variable and check if we could add an edge!
            ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>> connected = lastVariable.getDistanceConstrainedWith();
            labelDC = lastVariable.getLabelsDistanceConstrainedWith();
            for (Variable candidateVB : vrs) {
                if (candidateVB.getID() == lastVariable.getID()) {
                    continue;
                }

                if (labelDC.contains(candidateVB.getLabel())) {
                    boolean isConstrainedWith = false;
                    for (int j = 0; j < connected.size(); j++) {
                        Variable connectVB = vrs[connected.get(j).getNodeID()];
                        if (connectVB.getID() == candidateVB.getID()) {
                            isConstrainedWith = true;
                            break;
                        }
                    }

                    if (isConstrainedWith == true) {
                        continue;
                    }

                    //else create Gedge
                    int theLabelB = candidateVB.getLabel();
                    for (int j = 0; j < freqEdgeLabels.size(); j++) {
                        final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                                .set(lastNode, candidateVB.getID(), sortedFreqLabels.indexOf(theLabelA), 
                                        freqEdgeLabels.get(j).intValue(), sortedFreqLabels.indexOf(theLabelB), 1, theLabelA, theLabelB);
                        if ((code.getLast().compareTo(gEdge) < 0)) {
                            add(gEdge, code, 1);
                        } else {
                            gEdge.release(tenv);
                        }
                    }
                }
            }

            connected = lastVariable.getDistanceConstrainedBy();
            labelDC = lastVariable.getLabelsDistanceConstrainedBy();
            for (Variable candidateVB : vrs) {
                if (candidateVB.getID() == lastVariable.getID()) {
                    continue;
                }
                if (labelDC.contains(candidateVB.getLabel())) {
                    


                    boolean isConstrainedWith = false;

                    for (int j = 0; j < connected.size(); j++) {
                        Variable connectVB = vrs[connected.get(j).getNodeID()];
                        if (connectVB.getID() == candidateVB.getID()) {
                            isConstrainedWith = true;
                            break;
                        }
                    }

                    if (isConstrainedWith == true) {
                        continue;
                    }

                    //else create Gedge
                    int theLabelB = candidateVB.getLabel();
                    for (int j = 0; j < freqEdgeLabels.size(); j++) {
                        
                        final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                                .set(lastNode, candidateVB.getID(), sortedFreqLabels.indexOf(theLabelA), 
                                        freqEdgeLabels.get(j).intValue(), sortedFreqLabels.indexOf(theLabelB), -1, theLabelA, theLabelB);
                        if ((code.getLast().compareTo(gEdge) < 0)) {
                            add(gEdge, code, 1);
                        } else {
                            gEdge.release(tenv);
                        }
                    }
                }
            }

        }

        // if findPathsOnly then only extensions at node 0 are necessary
        int ackNode = lastNode;
        do {
            // find extension of the right most path
            final GSpanEdge<NodeType, EdgeType> ack = code.getParent(ackNode);
            ackNode = ack.getNodeA();//patternID

            Variable currentVariable = vrs[ackNode];
            HashSet<Integer> labelDC = currentVariable.getLabelsDistanceConstrainedWith();
            ArrayList<Point> pairs = new ArrayList<Point>();
            for (int theLabelB : labelDC) {
                int index = sortedFreqLabels.indexOf(theLabelB);
                pairs.add(new Point(theLabelB, index));
            }
            Collections.sort(pairs, new freqComparator());
            //now create the forward edges!!!
            int theLabelA = currentVariable.getLabel();

            for (int i = 0; i < pairs.size(); i++) {
                Point currentPoint = pairs.get(i);
                int label = currentPoint.x;
                int index = currentPoint.y;
                for (int j = 0; j < freqEdgeLabels.size(); j++) {
                    final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                            .set(ackNode, lastNode + 1, sortedFreqLabels.indexOf(theLabelA), 
                                    freqEdgeLabels.get(j).intValue(), index, 1, theLabelA, label);
                    add(gEdge, code, 0);
                }
            }
        } while (ackNode > 0);

        ackNode = lastNode;
        do {
            // find extension of the right most path
            final GSpanEdge<NodeType, EdgeType> ack = code.getParent(ackNode);
            ackNode = ack.getNodeA();//patternID

            Variable currentVariable = vrs[ackNode];
            HashSet<Integer> labelDC = currentVariable.getLabelsDistanceConstrainedBy();
            ArrayList<Point> pairs = new ArrayList<Point>();
            for (int theLabelB : labelDC) {
                int index = sortedFreqLabels.indexOf(theLabelB);
                pairs.add(new Point(theLabelB, index));
            }
            Collections.sort(pairs, new freqComparator());
            //now create the forward edges!!!
            int theLabelA = currentVariable.getLabel();

            for (int i = 0; i < pairs.size(); i++) {
                Point currentPoint = pairs.get(i);
                int label = currentPoint.x;
                int index = currentPoint.y;
                for (int j = 0; j < freqEdgeLabels.size(); j++) {
                    final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>(tenv)
                            .set(ackNode, lastNode + 1, sortedFreqLabels.indexOf(theLabelA), 
                                    freqEdgeLabels.get(j).intValue(), index, -1, theLabelA, label);
                    add(gEdge, code, 0);
                }

            }
        } while (ackNode > 0);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
     */
    @Override
    public void reset() {
        children.clear();
        resetNext();
    }
}
