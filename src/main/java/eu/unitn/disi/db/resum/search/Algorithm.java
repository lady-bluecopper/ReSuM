/**
 * created May 16, 2006
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

import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import sa.edu.kaust.grami.algorithmInterface.AlgorithmInterface;
import sa.edu.kaust.grami.dataStructures.DFSCode;
import sa.edu.kaust.grami.dataStructures.GSpanEdge;
import java.util.Map.Entry;
import java.util.TreeMap;
import sa.edu.kaust.grami.dataStructures.Edge;
import sa.edu.kaust.grami.dataStructures.GNode;
import sa.edu.kaust.grami.dataStructures.Generic;
import sa.edu.kaust.grami.dataStructures.Graph;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.IntFrequency;
import sa.edu.kaust.grami.dataStructures.MultiUserWeightedEdge;
import sa.edu.kaust.grami.dataStructures.gEdgeComparator;

/**
 * Creates a mining chain according to the gSpan algorithm, extended by
 * different options.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels
 * @param <EdgeType> the type of the edge labels
 */
public class Algorithm<NodeType, EdgeType> implements
        AlgorithmInterface<NodeType, EdgeType>,
        Generic<NodeType, EdgeType> {
    
    private static final long serialVersionUID = 1L;
    
    private transient Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
    
    private final Graph singleGraph;
    
    private final ArrayList<Double> freqEdgeLabels;
    
    private ArrayList<Integer> sortedFrequentLabels;
    
    public static HashMap<Integer, ArrayList<Integer>> neighborLabels;
    
    public static HashMap<Integer, ArrayList<Integer>> revNeighborLabels;

    
    public Algorithm(ArrayList<double[]> edgeWeights) throws Exception {
        singleGraph = new Graph();
        singleGraph.loadGraph(edgeWeights);
        freqEdgeLabels = singleGraph.getFreqEdgeLabels();
        sortedFrequentLabels = singleGraph.getSortedFreqLabels();
        singleGraph.setShortestPaths_1hop();
    }

    /**
     * Inner class to iterate over the initial edges
     *
     * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
     *
     */
    private class MyIterator implements
            Iterator<SearchLatticeNode<NodeType, EdgeType>> {

        final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> entryit;

        Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> last = null;

        MyIterator(final Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials) {
            entryit = initials.entrySet().iterator();
        }

        public boolean hasNext() {
            return entryit.hasNext();
        }

        public SearchLatticeNode<NodeType, EdgeType> next() {
            last = entryit.next();
            return last.getValue();
        }

        public void remove() {
            entryit.remove();
        }
    }

    public Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> getInitials() {
        return initials;
    }
    
    public void initialize() {
        initials = new TreeMap<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>(new gEdgeComparator<NodeType, EdgeType>());
        HashMap<Integer, HashMap<Integer, GNode>> nodesByLabel = singleGraph.getNodesByLabel();
        HashSet<Integer> contains = new HashSet<Integer>();
        IntFrequency minFreq = new IntFrequency(Settings.frequency);
        
        for (Entry< Integer, HashMap<Integer, GNode>> ar : nodesByLabel.entrySet()) {
            int firstLabel = ar.getKey();
            contains.clear();
            HashMap<Integer, GNode> tmp = ar.getValue();
            // iterate over the nodes with label firstLabel
            for (GNode node : tmp.values()) {
                HashMap<Integer, ArrayList<MultiUserWeightedEdge<Integer, Double, double[]>>> neighbours = node.getReachableWithNodes();
                if (neighbours != null) {
                    for (int secondLabel : neighbours.keySet()) {
                        int labelA = sortedFrequentLabels.indexOf(firstLabel);
                        int labelB = sortedFrequentLabels.indexOf(secondLabel);

                        //iterate over all neighbor nodes to get edge labels as well
                        for (MultiUserWeightedEdge<Integer, Double, double[]> mp : neighbours.get(secondLabel)) {
                            double edgeLabel = mp.getEdgeLabel();
                            if (!freqEdgeLabels.contains(edgeLabel)) {
                                continue;
                            }
                            final GSpanEdge<NodeType, EdgeType> gedge = new GSpanEdge<NodeType, EdgeType>()
                                    .set(0, 1, labelA, (int) edgeLabel, labelB, 1, firstLabel, secondLabel);

                            if (!initials.containsKey(gedge)) {
                                final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(2);
                                parents.add(gedge);
                                parents.add(gedge);

                                HPListGraph<NodeType, EdgeType> lg = new HPListGraph<NodeType, EdgeType>();
                                gedge.addTo(lg);
                                DFSCode<NodeType, EdgeType> code = new DFSCode<NodeType, EdgeType>(sortedFrequentLabels, singleGraph, null)
                                        .set(lg, gedge, gedge, parents);
                                initials.put(gedge, code);
                            }
                        }
                    }
                }
            }
        }
        for (Iterator<Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> eit 
                = initials.entrySet().iterator(); eit.hasNext();) {
            
            final DFSCode<NodeType, EdgeType> code = eit.next().getValue();
            if (minFreq.compareTo(code.frequency()) > 0) {
                eit.remove();
            }
        }
        neighborLabels = new HashMap<Integer, ArrayList<Integer>>();
        revNeighborLabels = new HashMap<Integer, ArrayList<Integer>>();
        for (final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> eit = initials
                .entrySet().iterator(); eit.hasNext();) {
            final DFSCode<NodeType, EdgeType> code = eit.next().getValue();
            int labelA;
            int labelB;
            GSpanEdge<NodeType, EdgeType> edge = code.getFirst();
            if (edge.getDirection() == Edge.INCOMING) {
                labelA = edge.getThelabelB();
                labelB = edge.getThelabelA();
            } else {
                labelB = edge.getThelabelB();
                labelA = edge.getThelabelA();
            }
            //add to labels
            neighborLabels.putIfAbsent(labelA,new ArrayList<Integer>());
            neighborLabels.get(labelA).add(labelB);
            //add reverse labels
            revNeighborLabels.putIfAbsent(labelB, new ArrayList<Integer>());
            revNeighborLabels.get(labelB).add(labelA);
        }
    }

    public Extender<NodeType, EdgeType> getExtender(final int threadIdx) {
        // configure mining chain
        final GSpanExtender<NodeType, EdgeType> extender = new GSpanExtender<NodeType, EdgeType>();
        // from last steps (filters after child computation)
        MiningStep<NodeType, EdgeType> curFirst = extender;
        GenerationStep<NodeType, EdgeType> gen;

        // ... over generation ...
        curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(curFirst);
        curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst);
        curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);

        // build generation chain
        GenerationPartialStep<NodeType, EdgeType> generationFirst = gen.getLast();
        generationFirst = new RightMostExtension<NodeType, EdgeType>(generationFirst);
        // insert generation chain
        gen.setFirst(generationFirst);
        // insert mining chain
        extender.setFirst(curFirst);
        return extender;
    }

    public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes() {
        return new MyIterator(initials);
    }
}
