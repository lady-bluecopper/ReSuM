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
package sa.edu.kaust.grami.dataStructures;


/**
 * Declares the functionality of a graph Node.
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
public interface Node<NodeType, EdgeType> extends Generic<NodeType, EdgeType> {

	/**
         * @param label
	 * @return the number of all incoming edges
	 */
	public int getInDegree(NodeType label);

	/**
	 * @return the node index of this node in the corresponding graph
	 */
	public int getID();

	/**
	 * @return the connected label
	 */
	public NodeType getLabel();

	/**
         * @param label
	 * @return the number of all outgoing edges
	 */
	public int getOutDegree(NodeType label);

	/**
	 * set the label of this edge
	 * 
	 * @param label
	 */
	public void setLabel(NodeType label);

}
