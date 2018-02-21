package eu.unitn.disi.db.resum.multithread;

/**
 * created May 23, 2006
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 *
 * This file is part of parsemis.
 *
 * Licence: LGPL: http://www.gnu.org/licenses/lgpl.html EPL:
 * http://www.eclipse.org/org/documents/epl-v10.php See the LICENSE file in the
 * project's top-level directory for details.
 */
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import sa.edu.kaust.grami.dataStructures.DFSCode;
import sa.edu.kaust.grami.dataStructures.GSpanEdge;
import sa.edu.kaust.grami.dataStructures.GSpanExtension;
import sa.edu.kaust.grami.dataStructures.Generic;
import sa.edu.kaust.grami.dataStructures.Graph;
import sa.edu.kaust.grami.dataStructures.HPGraph;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.MinExtension;
import sa.edu.kaust.grami.dataStructures.MinExtensionSet;

/**
 * Represents the thread local object pool for the gSpan algorithm.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public final class GThreadEnvironment<NodeType, EdgeType> implements
        Generic<NodeType, EdgeType> {

    public final int threadIdx;

    private final MinExtensionSet<NodeType, EdgeType> mes = new MinExtensionSet<NodeType, EdgeType>(this);

    private GSpanEdge<NodeType, EdgeType> firstGSpanEdge = null;

    private GSpanExtension<NodeType, EdgeType> firstGSpanExtension = null;

    private MinExtension<NodeType, EdgeType> firstMinExtension = null;

    /**
     * creates a new environment
     *
     * @param threadIdx
     */
    public GThreadEnvironment(final int threadIdx) {
        final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment.env(this);
        this.threadIdx = threadIdx;
    }

    /**
     * @param sortedFreqLabels
     * @param singleGraph
     * @param me
     * @param nonCands
     * @param first
     * @param last
     * @param parents
     * @return a newly initialized DFSCode
     */
    public final DFSCode<NodeType, EdgeType> getCode(
            ArrayList<Integer> sortedFreqLabels,
            Graph singleGraph,
            HashMap<Integer, HashSet<Integer>> nonCands,
            final HPListGraph<NodeType, EdgeType> me,
            final GSpanEdge<NodeType, EdgeType> first,
            final GSpanEdge<NodeType, EdgeType> last,
            final ArrayList<GSpanEdge<NodeType, EdgeType>> parents) {

        return new DFSCode<NodeType, EdgeType>(this, sortedFreqLabels, singleGraph, nonCands)
                .set(me, first, last, parents);
    }

    /**
     *
     * @param nodeA
     * @param nodeB
     * @param labelA
     * @param edgeLabel
     * @param labelB
     * @param direction
     * @param theLabelA
     * @param theLabelB
     * @return a newly initialized GSpanEdge according to given parameters
     */
    public final GSpanEdge<NodeType, EdgeType> getEdge(final int nodeA,
            final int nodeB, final int labelA, final int edgeLabel,
            final int labelB, final int direction, final int theLabelA, final int theLabelB) {
        return nextGSpanEdge().set(nodeA, nodeB, labelA, edgeLabel, labelB, direction, theLabelA, theLabelB);
    }

    /**
     * @param gEdge
     * @param frag
     * @return a newly initialized GSpanExtension
     */
    public final GSpanExtension<NodeType, EdgeType> getExtension(
            final GSpanEdge<NodeType, EdgeType> gEdge,
            final DFSCode<NodeType, EdgeType> frag) {
        final GSpanExtension<NodeType, EdgeType> ret = nextGSpanExtension();
        ret.edge = gEdge;
        ret.frag = frag;
        return ret;
    }

    /**
     * @return the one and only ExtensionSet for each thread
     */
    public final MinExtensionSet<NodeType, EdgeType> getExtensionSet() {
        return mes;
    }

    /**
     *
     * @param nodeA
     * @param nodeB
     * @param labelA
     * @param edgeLabel
     * @param labelB
     * @param dir
     * @param graph
     * @param gNodeA
     * @param gEdge
     * @param gNodeB
     * @param theLabelA
     * @param theLabelB
     * @return a newly initialized MinExtension
     */
    public final MinExtension<NodeType, EdgeType> getMinExtension(
            final int nodeA, final int nodeB, final int labelA,
            final int edgeLabel, final int labelB, final int dir,
            final HPGraph<NodeType, EdgeType> graph, final int gNodeA,
            final int gEdge, final int gNodeB, int theLabelA, int theLabelB) {
        return nextMinExtension().set(nodeA, nodeB, labelA, edgeLabel, labelB,
                dir, gEdge, gNodeB, theLabelA, theLabelB);
    }

    private final GSpanEdge<NodeType, EdgeType> nextGSpanEdge() {
        if (firstGSpanEdge == null) {
            return new GSpanEdge<NodeType, EdgeType>(this);
        }
        final GSpanEdge<NodeType, EdgeType> ret = firstGSpanEdge;
//        firstGSpanEdge = ret.next;
        return ret;
    }

    private final GSpanExtension<NodeType, EdgeType> nextGSpanExtension() {
        if (firstGSpanExtension == null) {
            return new GSpanExtension<NodeType, EdgeType>(this);
        }
        final GSpanExtension<NodeType, EdgeType> ret = firstGSpanExtension;
//        firstGSpanExtension = ret.next;
        return ret;
    }

    private final MinExtension<NodeType, EdgeType> nextMinExtension() {
        if (firstMinExtension == null) {
            return new MinExtension<NodeType, EdgeType>(this);
        }
        final MinExtension<NodeType, EdgeType> ret = firstMinExtension;
//        firstMinExtension = (MinExtension<NodeType, EdgeType>) ret.next;
        return ret;
    }

    /**
     * stores the given object in the pool (if configured)
     *
     * @param obj
     */
    public final void push(final GSpanEdge<NodeType, EdgeType> obj) {
    }

    /**
     * stores the given object in the pool (if configured)
     *
     * @param obj
     */
    public final void push(final GSpanExtension<NodeType, EdgeType> obj) {
    }

    /**
     * stores the given object in the pool (if configured)
     *
     * @param first
     * @param last
     */
    public final void push(final GSpanEdge<NodeType, EdgeType> first,
            final GSpanEdge<NodeType, EdgeType> last) {
    }

    /**
     * stores the given object in the pool (if configured)
     *
     * @param obj
     */
    public final void push(final MinExtension<NodeType, EdgeType> obj) {
    }

    /**
     * release the one and only ExtensionSet for reuse
     *
     * @param set
     */
    public final void push(final MinExtensionSet<NodeType, EdgeType> set) {
    }

}
