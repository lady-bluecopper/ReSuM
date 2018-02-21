package eu.unitn.disi.db.resum.multithread;

import eu.unitn.disi.db.resum.search.Extender;
import eu.unitn.disi.db.resum.search.SearchLatticeNode;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import sa.edu.kaust.grami.dataStructures.HPListGraph;

/**
 * Created on Jun 26, 2006
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
/**
 * This class implements the depth-first working for all threads.
 *
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * @param <NodeType> the type of the node labels (will be hashed and checked
 * with .equals(..))
 * @param <EdgeType> the type of the edge labels (will be hashed and checked
 * with .equals(..))
 */
public class Worker<NodeType, EdgeType> implements Runnable {

    private final MiningStack<NodeType, EdgeType> stack;

    private final Extender<NodeType, EdgeType> extender;

    public Collection<HPListGraph<NodeType, EdgeType>> found;

    public ArrayList<BitSet> foundMask;

    /**
     * creates a new Worker
     *
     * @param stack the stack for storing and getting unextended fragments
     * @param found the set to store frequent fragments
     * @param extender
     */
    public Worker(final MiningStack<NodeType, EdgeType> stack,
            final Collection<HPListGraph<NodeType, EdgeType>> found,
            final Extender<NodeType, EdgeType> extender) {
        this.stack = stack;
        this.found = found;
        this.extender = extender;
    }

    public void run() {
        SearchLatticeNode<NodeType, EdgeType> node = stack.pop();
        // while work is available, extend it
        while (node != null) {
            // extends current node
            final Iterator<SearchLatticeNode<NodeType, EdgeType>> children = extender.getChildren(node).iterator();
            // get next node
            final SearchLatticeNode<NodeType, EdgeType> next = (children.hasNext() ? children.next() : stack.pop());
            // push other found ones
            while (children.hasNext()) {
                stack.push(children.next());
            }
            if (node.someStoresIt()) {
                // add pattern to result set
                found.add(node.getHPlistGraph()); 
                System.out.print(".");
                foundMask.add(node.getStoreValues());
            } else {
                node.release();
            }
            node.finalizeIt();
            node = next;
        }
    }

}
