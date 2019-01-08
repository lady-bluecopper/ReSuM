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

import java.util.Collection;

import sa.edu.kaust.grami.dataStructures.Extension;
import sa.edu.kaust.grami.dataStructures.Frequency;
import sa.edu.kaust.grami.dataStructures.Frequented;
import sa.edu.kaust.grami.dataStructures.IntFrequency;
import eu.unitn.disi.db.resum.utilities.Settings;

/**
 * This class implements the general pruning of fragments according to their
 * frequency.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class FrequencyPruningStep<NodeType, EdgeType> extends
        MiningStep<NodeType, EdgeType> {

    private final Frequency min;

    /**
     * creates a new frequency pruning
     *
     * @param next
     */
    public FrequencyPruningStep(final MiningStep<NodeType, EdgeType> next) {
        super(next);
        this.min = new IntFrequency(Settings.frequency);
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

        final Frequency freq = ((Frequented) node).frequency();  //HERE THE FREQUENCY CALCULATION OCCURS !!!
        
        node.setStoreValues(node.isRelevant());
        
        if (node.someStoresIt()) {
            System.out.println("*");
        }

        if (min.compareTo(freq) <= 0 && (node.getSize() < Settings.maxSize)) {
            if (Settings.score > 1 || node.getStoreValues().cardinality() > 0) {
                    callNext(node, extensions);
            } 
        }
    }
}
