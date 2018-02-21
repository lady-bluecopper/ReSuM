/**
 * created May 12, 2006
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

import java.io.Serializable;
import java.util.Collection;

import sa.edu.kaust.grami.dataStructures.Extension;
import sa.edu.kaust.grami.dataStructures.Generic;
import sa.edu.kaust.grami.dataStructures.HPListGraph;
import sa.edu.kaust.grami.dataStructures.serializableObject;
import java.util.BitSet;
import java.util.concurrent.CopyOnWriteArrayList;
import eu.unitn.disi.db.resum.utilities.Settings;

/**
 * This class defines the interface and basic functionality of a single node in
 * a search lattice.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels
 * @param <EdgeType> the type of the edge labels
 */
public abstract class SearchLatticeNode<NodeType, EdgeType> implements
        Generic<NodeType, EdgeType>, Serializable, serializableObject {

    private static final long serialVersionUID = 1L;
    private int level;
    private BitSet storeValues;
    protected double[] scores;

    protected SearchLatticeNode() {
        this.level = -1;
        this.storeValues = new BitSet();
        this.storeValues.set(0, Settings.structureSize, true);
    }

    protected SearchLatticeNode(final int level) {
        this.level = level;
        this.storeValues = new BitSet();
        this.storeValues.set(0, Settings.structureSize, true);
    }

    /**
     * @param extension
     * @return a new node resulted by extending this node with the given
     * <code>extension</code>
     */
    public abstract SearchLatticeNode<NodeType, EdgeType> extend(
            Extension<NodeType, EdgeType> extension);

    /**
     * release all internal structures to the local object pool that are never
     * needed even if the node is stored
     */
    public abstract void finalizeIt();

    /**
     * @return the <code>level</code> (= depth in the search tree) of this node
     */
    public final int getLevel() {
        return level;
    }

    /**
     * release all internal structures to the local object pool that are never
     * needed if the node is not stored
     */
    public abstract void release();

    /**
     * sets the <code>level</code> (= depth in the search tree) of this node
     *
     * @param level
     */
    public final void setLevel(final int level) {
        this.level = level;
    }

    /**
     * sets the thread index of the SearchLatticeNode
     *
     * @param threadIdx
     */
    public abstract void setThreadNumber(int threadIdx);

    /**
     * gets the thread index of the SearchLatticeNode
     *
     * @return the thread index
     */
    public abstract int getThreadNumber();

    /**
     * @return the set <code>store</code>-value
     */
    public final BitSet getStoreValues() {
        return storeValues;
    }

    public final void setStoreValues(BitSet newValues) {
        storeValues = newValues;
    }

    public final void setStoreValues(boolean[] newValues) {
        for (int b = 0; b < newValues.length; b++) {
            storeValues.set(b, newValues[b]);
        }
    }
    
    public final void setScoreValues(double[] newValues) {
        scores = newValues;
    }
    
    public final double[] getScoreValues() {
        return scores;
    }

    /**
     * sets the <code>store</code>-value of this node to the given boolean
     *
     * @param store
     * @param index
     */
    public final void store(final boolean store, int index) {
        if (index == -1) {
            storeValues.set(0, Settings.structureSize, store);
        } else {
            storeValues.set(index, store);
        }
    }

    public final boolean someStoresIt() {
        return storeValues.cardinality() > 0;
    }

    /**
     * stores the fragment into the given set
     *
     * @param set
     */
    public void store(final Collection<HPListGraph<NodeType, EdgeType>> set) {
        set.add(getHPlistGraph());
    }

    public void store(final CopyOnWriteArrayList<HPListGraph<NodeType, EdgeType>> set) {
        set.add(getHPlistGraph());
    }

    public abstract boolean[] isRelevant();

    public abstract HPListGraph<NodeType, EdgeType> getHPlistGraph();
}
