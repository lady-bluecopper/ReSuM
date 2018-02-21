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

import sa.edu.kaust.grami.dijkstra.Main;
import java.util.ArrayList;
import java.util.Collection;

import sa.edu.kaust.grami.dataStructures.HPListGraph;
import java.util.BitSet;
import eu.unitn.disi.db.resum.utilities.Settings;
import java.util.Iterator;
import sa.edu.kaust.grami.dataStructures.StaticData;

/**
 * This class represents the local recursive strategy.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels
 * @param <EdgeType> the type of the edge labels
 */
public class RecursiveStrategy<NodeType, EdgeType> implements
        Strategy<NodeType, EdgeType> {

    private Extender<NodeType, EdgeType> extender;
    private Collection<HPListGraph<NodeType, EdgeType>> ret;
    private ArrayList<BitSet> bulkRet;
    private ArrayList<double[]> scores;

    //INITIAL NODES SEARCH
    public Collection<HPListGraph<NodeType, EdgeType>> search(final Algorithm<NodeType, EdgeType> algo) {
        ret = new ArrayList<HPListGraph<NodeType, EdgeType>>();
        bulkRet = new ArrayList<BitSet>();
        scores = new ArrayList<double[]>();
        extender = algo.getExtender(0);
        
        for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo.initialNodes(); it.hasNext();) {
            final SearchLatticeNode<NodeType, EdgeType> code = it.next();

            double edgeLabel = Double.parseDouble(code.getHPlistGraph().getEdgeLabel(code.getHPlistGraph().getEdge(0, 1)).toString());
            int node1Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(0).toString());
            int node2Label = Integer.parseInt(code.getHPlistGraph().getNodeLabel(1).toString());
            String signature;
            if (node1Label < node2Label) {
                signature = node1Label + "_" + edgeLabel + "_" + node2Label;
            } else {
                signature = node2Label + "_" + edgeLabel + "_" + node1Label;
            }

            search(code);
            it.remove();
            //TODO remove frequent edge labels that are already processed - test test test before approval
            StaticData.hashedEdges.remove(signature);
        }
        return ret;
    }

    //RECURSIVE NODES SEARCH
    private void search(final SearchLatticeNode<NodeType, EdgeType> node) {
        if (Main.watch.getElapsedTimeSecs() > Settings.MAX_COMPUTATION_TIME) {
            System.err.println("[WARN] Mining process was taking too long to complete");
            System.exit(0);
        }
        final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender.getChildren(node);
        for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {
            search(child);

        }
        if (node.someStoresIt()) {
            // add pattern to result set
            node.store(ret);
            System.out.print(".");
            bulkRet.add(node.getStoreValues());
            scores.add(node.getScoreValues());
            
        } else {
            node.release();
        }
        node.finalizeIt();
    }

    public ArrayList<BitSet> getRetMask() {
        return bulkRet;
    }
    
    public ArrayList<double[]> getScores() {
        return scores;
    }
    
}
