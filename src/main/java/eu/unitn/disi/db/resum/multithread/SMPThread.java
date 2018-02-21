/**
 * Created on Jun 26, 2006
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
package eu.unitn.disi.db.resum.multithread;

import eu.unitn.disi.db.resum.search.Algorithm;
import java.util.Collection;
import sa.edu.kaust.grami.dataStructures.Generic;
import sa.edu.kaust.grami.dataStructures.HPListGraph;

/**
 * This class represents a single thread for the multi-threaded parallelization
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class SMPThread<NodeType, EdgeType> extends Thread implements
        Generic<NodeType, EdgeType> {

    private final int idx;

    private final Algorithm<NodeType, EdgeType> algo;

    private final StackList<NodeType, EdgeType> list;

    private final Collection<HPListGraph<NodeType, EdgeType>> answer;

    /**
     * creates a new thread
     *
     * @param idx the index of the new thread
     * @param algo the algorithm that will be used
     * @param list the list of all stacks
     * @param answer the collection the found fragments will be stored in
     */
    public SMPThread(final int idx, final Algorithm<NodeType, EdgeType> algo,
            final StackList<NodeType, EdgeType> list,
            final Collection<HPListGraph<NodeType, EdgeType>> answer) {
        this.idx = idx;
        this.algo = algo;
        this.list = list;
        this.answer = answer;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        new Worker<NodeType, EdgeType>(new LocalStack<NodeType, EdgeType>(idx, list, LocalEnvironment.env(this)), answer, algo.getExtender(idx)).run();
    }

    public int getIdx() {
        return idx;
    }

}
