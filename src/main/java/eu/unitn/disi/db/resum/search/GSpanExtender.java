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

import java.util.Collection;
import java.util.TreeSet;

import sa.edu.kaust.grami.dataStructures.Extension;
import sa.edu.kaust.grami.dataStructures.GSpanExtension;


/**
 * Contains the whole chain of mining steps to generate the children of a search node.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels
 * @param <EdgeType>
 *            the type of the edge labels
 */
public class GSpanExtender<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> implements Extender<NodeType, EdgeType> {

	private MiningStep<NodeType, EdgeType> first;
	private final Collection<Extension<NodeType, EdgeType>> dummy;
	private Collection<SearchLatticeNode<NodeType, EdgeType>> children;

	/**
	 * creates a new empty Extender that is also the end of the mining chain
	 * 
	 */
	public GSpanExtender() {
		super(null);
		first = this;
		dummy = new TreeSet<Extension<NodeType, EdgeType>>();
	}

	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node, final Collection<Extension<NodeType, EdgeType>> extensions) {	
		for (final Extension<NodeType, EdgeType> ext : extensions) {
        	 children.add(node.extend(ext));
        	 ((GSpanExtension<NodeType, EdgeType>) ext).release();
		}
	}

	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			final Collection<SearchLatticeNode<NodeType, EdgeType>> nodes) {
		throw new UnsupportedOperationException("not available for gSpan");
	}

	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(final SearchLatticeNode<NodeType, EdgeType> node) {
		children = new TreeSet<SearchLatticeNode<NodeType, EdgeType>>();
		dummy.clear();
		first.call(node, dummy);
		return children;
	}

	protected final void setFirst(final MiningStep<NodeType, EdgeType> first) {
		this.first = first;
	}
}
