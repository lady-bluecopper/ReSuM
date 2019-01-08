/**
 * created May 2, 2006
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
package sa.edu.kaust.grami.und.algorithmInterface;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import sa.edu.kaust.grami.und.dataStructures.DFSCode;
import sa.edu.kaust.grami.und.dataStructures.GSpanEdge;

import sa.edu.kaust.grami.und.search.Extender;
import sa.edu.kaust.grami.und.search.Generic;
import sa.edu.kaust.grami.und.search.SearchLatticeNode;

/**
 * This interface encapsulate the required abilities of a mining algorithm.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public interface Algorithm<NodeType, EdgeType> extends
		Generic<NodeType, EdgeType>, Serializable {

	/**
	 * @return a (new) Extender Object for the given thread (index)
	 */
	public Extender<NodeType, EdgeType> getExtender();

	/**
	 * @return an iterator over the initial nodes for the search
	 */
	public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes();
        
        public Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> getInitials();

}
