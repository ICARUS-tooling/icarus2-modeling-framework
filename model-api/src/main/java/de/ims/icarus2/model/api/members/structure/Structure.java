/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/members/structure/Structure.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.members.structure;

import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.util.annotations.OptionalMethod;
import de.ims.icarus2.util.collections.DataSequence;

/**
 * Provides a structural view on an {@link ItemLayer} by specifying a
 * set of nodes connected by edges. Typically a {@code Structure} object
 * will serve as a kind of <i>augmentation</i> of an existing {@code Container}:
 * <br>
 * It holds the required items from the original container (either
 * directly or via a general reference to the other container) and
 * (optionally) defines a set of virtual items. Over all those items
 * it then spans a collection of edges, thereby creating the <i>structural</i>
 * information.
 *
 * @author Markus Gärtner
 * @version $Id: Structure.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public interface Structure extends Container {

	@Override
	StructureManifest getManifest();

	@Override
	StructureEditVerifier createEditVerifier();

	/**
	 * Returns the <i>type</i> of this structure.
	 * @return the type of this structure
	 */
	StructureType getStructureType();

	/**
	 *
	 * @return
	 *
	 * @see #isItemsComplete()
	 */
	boolean isEdgesComplete();

	/**
	 *
	 * @return
	 */
	boolean isAugmented();

	@OptionalMethod
	StructureInfo getInfo();

	/**
	 * Returns the total number of edges this structure hosts.
	 * @return the total number of edges this structure hosts.
	 */
	long getEdgeCount();

	/**
	 * Returns the {@link Edge} stored at the given position within this
	 * structure.
	 *
	 * @param index The position of the desired {@code Edge} within this structure
	 * @return The {@code Edge} at position {@code index}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 */
	Edge getEdgeAt(long index);


	/**
	 * Returns the index of the given {@code Edge} within this structure's
	 * list of edges or {@code -1} if the item is not hosted within this
	 * structure.
	 * <p>
	 * Note that for every edge <i>e</i> that is hosted within some structure the
	 * following will always return a result different from {@code -1}:<br>
	 * {@code e.getStructure().indexOfEdge(e)}
	 *
	 * @param edge The {@code Edge} whose index is to be returned
	 * @return The index at which the {@code Edge} appears within this
	 * structure or {@code -1} if the edge is not hosted within this structure.
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	long indexOfEdge(Edge edge);

	/**
	 * Return the total number of edges for a given node.
	 *
	 * @param node the node to query for the number of edges.
	 * @return the total number of edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	long getEdgeCount(Item node);

	/**
	 * Return the number of either outgoing or incoming edges for a given node
	 * depending on the {@code isSource} argument.
	 *
	 * @param node the node to query for the number of outgoing edges.
	 * @return the number of <b>outgoing</i> edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	long getEdgeCount(Item node, boolean isSource);

	/**
	 * Return the either outgoing or incoming edge at position {@code index}
	 * for a given node depending on the {@code isSource} argument.
	 *
	 * @param node the {@code Item} in question
	 * @param index the position of the desired {@code Edge} in the list of
	 * <i>outgoing</i> edges for the given node
	 * @return the edge at position {@code index} for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount(Item,boolean)</tt>)
	 *         with the given {@code node} and {@code isSource} parameters
	 */
	Edge getEdgeAt(Item node, long index, boolean isSource);

	/**
	 * For non-trivial structures returns the <i>generic root</i> node.
	 * To allow actual root nodes of the structure to contain edge
	 * annotations, they should all be linked to the single
	 * <i>generic root</i> which makes it easier for application code
	 * to collect them in a quick lookup manner.
	 * <p>
	 * What the actual root of a structure is meant to be depends on that
	 * structure's {@code StructureType}:<br>
	 * For a {@value StructureType#CHAIN} this is the first item in the chain,
	 * for a {@value StructureType#TREE} it is the one tree-root. In the case
	 * of general {@value StructureType#GRAPH} structures it will be either a
	 * single node specifically marked as root or each node that has no
	 * incoming edges.
	 *
	 * @return the <i>generic root</i> of this structure or {@code null} if this
	 * structure is of type {@value StructureType#SET}
	 */
	Item getVirtualRoot();

	/**
	 * Returns whether or not the given {@code Item} is a root in this structure.
	 * The {@code root} property is determined by a node being directly linked to the
	 * <i>generic root</i> node as returned by {@link #getVirtualRoot()}.
	 *
	 * @param node The {@code Item} in question
	 * @return {@code true} iff the given {@code node} is a root in this structure
	 * @throws NullPointerException if the {@code node} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member of this
	 * structure
	 */
	boolean isRoot(Item node);

	// TREE METHODS

	/**
	 * Utility method to fetch the <i>parent</i> of a given item in this
	 * structure. The meaning of the term <i>parent</i> is depending on the
	 * {@code StructureType} as defined in this structure's {@code ContainerManifest}
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor
	 * {@value StructureType#GRAPH}.
	 *
	 * @param node the node whose parent is to be returned
	 * @return the node's parent or {@code null} if the node has no defined parent
	 *
	 * @throws UnsupportedOperationException in case the structure type does not support
	 * the optional tree methods
	 */
	@OptionalMethod
	Item getParent(Item node);

	/**
	 * Returns the index of the {@link Edge} that links the given {@code child} item
	 * with its parent within the list of that parent's outgoing edges.
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor
	 * {@value StructureType#GRAPH}.
	 *
	 * @param child
	 * @return
	 *
	 * @throws UnsupportedOperationException in case the structure type does not support
	 * the optional tree methods
	 */
	@OptionalMethod
	long indexOfChild(Item child);

	/**
	 * Returns the
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor
	 * {@value StructureType#GRAPH}.
	 *
	 * @param child
	 * @param offset
	 * @return
	 *
	 * @throws ModelException if the {@code offset} parameter is {@code 0} or exceeds the
	 * number of siblings of the specified {@code child} node in the direction defined
	 * by the parameter's algebraic sign.
	 * @throws UnsupportedOperationException in case the structure type does not support
	 * the optional tree methods
	 */
	@OptionalMethod
	Item getSiblingAt(Item child, long offset);

	/**
	 * Returns the length of the longest path to a leaf node in the tree whose root the given
	 * item is. If the given {@code node} is a leaf itself the method returns {@code 0}.
	 *
	 * @param node
	 * @return
	 */
	@OptionalMethod
	long getHeight(Item node);

	@OptionalMethod
	default long getHeight() {
		return getHeight(getVirtualRoot());
	}

	/**
	 * Returns the length of the path up to the virtual root of this structure.
	 * If the given {@code node} is the virtual root itself the method returns {@code 0}. Note
	 * that a return value of {@code -1} signals that the specified node has no valid 'parent'
	 * path to the virtual root and the actual depth could not be calculated.
	 *
	 * @param node
	 * @return
	 */
	@OptionalMethod
	long getDepth(Item node);

	/**
	 * Returns the total size of the tree whose root the given item is (minus the node itself).
	 * This includes children of the given node and all successive grand children. If called
	 * for a leaf node this method returns {@code 0}.
	 *
	 * @param parent
	 * @return
	 */
	@OptionalMethod
	long getDescendantCount(Item parent);

	// EDIT METHODS

	/**
	 * Adds the given {@code edge} to the internal edge storage and
	 * returns the position the edge is positioned at. Note that it is
	 * up to the actual implementation whether or not to honor the
	 * specified {@code index} at which to insert the new edge.
	 *
	 * @param index
	 * @param edge
	 * @return
	 */
	long addEdge(long index, Edge edge);

	/**
	 * Adds the given sequence of edges to this structure. Note that
	 * it is up to the actual implementation whether or not to honor
	 * specified {@code index} at which to insert the new edges.
	 *
	 * @param index
	 * @param edges
	 */
	void addEdges(long index, DataSequence<? extends Edge> edges);

	/**
	 * Removes and returns the edge at the given index. Shifts the
	 * indices of all edges after the given position to account
	 * for the missing one.
	 *
	 * @param index The position of the edge to be removed
	 * @return The edge previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Edge removeEdge(long index);

	/**
	 * Removes from this structure all edges.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	DataSequence<? extends Edge> removeEdges(long index0, long index1);

	/**
	 * Moves the edge currently located at position {@code index0}
	 * over to position {@code index1}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void moveEdge(long index0, long index1);

	/**
	 * Changes the specified terminal (source or target) of the given edge to
	 * the supplied item. Note that the {@code item} argument has to
	 * be already contained within the "node" container of this structure.
	 *
	 * @param edge The edge whose terminal should be changed
	 * @param item The new terminal for the edge
	 * @param isSource Specifies which terminal (source or target) should be changed
	 * @throws NullPointerException if either one the {@code edge} or {@code item}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code item} is unknown to
	 * this structure (i.e. not a member of its "node" container")
	 * @throws IllegalArgumentException if the given {@code item} is not a valid
	 * candidate for the specified terminal
	 */
	void setTerminal(Edge edge, Item item, boolean isSource);

	/**
	 * Creates a new edge, linking it to this structure as its host and with the
	 * specified {@code source} and {@code target} terminals. Note that this method
	 * does <b>not</b> add the new edge to this structure. This is left to the client
	 * code since it is undefined where to insert the newly created edge.
	 * This method exists to leave implementation decisions for its edges to a structure.
	 * It is therefore advised that all edges to be added to a structure are first created
	 * using this method!
	 *
	 * @param source
	 * @param target
	 * @return
	 */
	Edge newEdge(Item source, Item target);

	default void forEachEdge(Consumer<? super Edge> action) {
		long edgeCount = getEdgeCount();
		for(long i = 0L; i<edgeCount; i++) {
			action.accept(getEdgeAt(i));
		}
	}

	default void forEachEdge(Item node, boolean isSource, Consumer<? super Edge> action) {
		long edgeCount = getEdgeCount(node, isSource);
		for(long i = 0L; i<edgeCount; i++) {
			action.accept(getEdgeAt(node, i, isSource));
		}
	}

	default void forEachIncomingEdge(Item node, Consumer<? super Edge> action) {
		forEachEdge(node, false, action);
	}

	default void forEachOutgoingEdge(Item node, Consumer<? super Edge> action) {
		forEachEdge(node, true, action);
	}

	default void forEachEdge(Item node, Consumer<? super Edge> action) {
		forEachEdge(node, false, action);
		forEachEdge(node, true, action);
	}
}
