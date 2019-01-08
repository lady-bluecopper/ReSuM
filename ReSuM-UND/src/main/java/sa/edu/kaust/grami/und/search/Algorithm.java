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
package sa.edu.kaust.grami.und.search;

import eu.unitn.disi.db.resum.und.utilities.MultiUserWeightedEdge;
import eu.unitn.disi.db.resum.und.utilities.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import sa.edu.kaust.grami.und.dataStructures.DFSCode;
import sa.edu.kaust.grami.und.dataStructures.GSpanEdge;
import java.util.Map.Entry;
import java.util.TreeMap;
import sa.edu.kaust.grami.und.dataStructures.GNode;
import sa.edu.kaust.grami.und.dataStructures.Graph;
import sa.edu.kaust.grami.und.dataStructures.HPListGraph;
import sa.edu.kaust.grami.und.dataStructures.IntFrequency;
import sa.edu.kaust.grami.und.dataStructures.gEdgeComparator;

/**
 * Creates a mining chain according to the gSpan algorithm, extended by
 * different options.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class Algorithm<NodeType, EdgeType> implements
        sa.edu.kaust.grami.und.algorithmInterface.Algorithm<NodeType, EdgeType>,
        Generic<NodeType, EdgeType> {
    
    private static final long serialVersionUID = 1L;
    
    private final Graph singleGraph;
    private final ArrayList<Integer> sortedFrequentLabels;
    private final ArrayList<Double> freqEdgeLabels;
    Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
    public static HashMap<Integer, ArrayList<Integer>> neighborLabels;
    
    public Algorithm(ArrayList<double[]> edgeWeights) throws Exception {
        singleGraph = new Graph();
        singleGraph.loadGraph(edgeWeights);
        sortedFrequentLabels = singleGraph.getSortedFreqLabels();
        freqEdgeLabels = singleGraph.getFreqEdgeLabels();
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

        final Iterator<Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> entryit;

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
        HashMap<Integer, HashMap<Integer, GNode>> freqNodesByLabel = singleGraph.getNodesByLabel();
        HashSet<Integer> contains = new HashSet<Integer>();
        IntFrequency minFreq = new IntFrequency(Settings.frequency);
        freqNodesByLabel.entrySet().stream().forEach((ar) -> {
            int firstLabel = ar.getKey();
            contains.clear();
            HashMap<Integer, GNode> tmp = ar.getValue();
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

                            final GSpanEdge<NodeType, EdgeType> gedge = new GSpanEdge<NodeType, EdgeType>().set(0, 1, labelA, (int) edgeLabel, labelB, 0, firstLabel, secondLabel);

                            if (!initials.containsKey(gedge)) {
                                final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(2);
                                parents.add(gedge);
                                parents.add(gedge);

                                HPListGraph<NodeType, EdgeType> lg = new HPListGraph<NodeType, EdgeType>();
                                gedge.addTo(lg);
                                DFSCode<NodeType, EdgeType> code = new DFSCode<NodeType, EdgeType>(sortedFrequentLabels, freqEdgeLabels, singleGraph, null).set(lg, gedge, gedge, parents);

                                initials.put(gedge, code);
                            }
                        }
                    }
                }
            }
        });

        for (final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> eit = initials
                .entrySet().iterator(); eit.hasNext();) {
            final DFSCode<NodeType, EdgeType> code = eit.next().getValue();
            if (minFreq.compareTo(code.frequency()) > 0) {
                eit.remove();
            }
        }

        neighborLabels = new HashMap();

        for (final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> eit = initials
                .entrySet().iterator(); eit.hasNext();) {
            final DFSCode<NodeType, EdgeType> code = eit.next().getValue();

            //add possible neighbor labels for each label
            int labelA;
            int labelB;
            GSpanEdge<NodeType, EdgeType> edge = code.getFirst();
            labelA = edge.getThelabelA();
            labelB = edge.getThelabelB();
            neighborLabels.putIfAbsent(labelA, new ArrayList());
            neighborLabels.get(labelA).add(labelB);
            //now the reverse
            neighborLabels.putIfAbsent(labelB, new ArrayList());
            neighborLabels.get(labelB).add(labelA);
        }
    }

    public Extender<NodeType, EdgeType> getExtender() {
        // configure mining chain
        final GSpanExtender<NodeType, EdgeType> extender = new GSpanExtender<NodeType, EdgeType>();
        // from last steps (filters after child computation) ...
        MiningStep<NodeType, EdgeType> curFirst = extender;
        GenerationStep<NodeType, EdgeType> gen;

        // ... over generation ...
        curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(curFirst);
        // .. to prefilters
        curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst);
        curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);
        // build generation chain
        GenerationPartialStep<NodeType, EdgeType> generationFirst = gen.getLast();
        //YES
        generationFirst = new RightMostExtension<NodeType, EdgeType>(generationFirst);
        // insert generation chain
        gen.setFirst(generationFirst);
        // insert mining chain
        extender.setFirst(curFirst);
        return extender;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialNodes()
     */
    public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes() {
        return new MyIterator(initials);
    }
}
