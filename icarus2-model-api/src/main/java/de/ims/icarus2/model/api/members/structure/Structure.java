/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.api.members.structure;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.util.stream.ModelStreams;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.annotations.Internal;
import de.ims.icarus2.util.annotations.OptionalMethod;
import de.ims.icarus2.util.collections.seq.DataSequence;

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
 *
 */
public interface Structure extends Container {

	//TODO: add support for "multi-structure", i.e. n-best lists, blend-graphs for trees, etc...

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
	 * Returns {@code true} in case the structure contains all the edges it
	 * is supposed to contain. For extremely large containers or containers that
	 * only serve a secondary role in the task at hand (like boundary containers)
	 * it is possible that a driver decides to omit certain elements which would
	 * either be unaccessible or which are simply not needed.
	 * <p>
	 * Note that in case this method returns {@code true}, its sibling method
	 * {@link #isItemsComplete()} is bound to also return {@code true}.
	 * However, the reverse implication doesn't hold: It is perfectly legal for a
	 * structure to be able to provide all the nodes but not having loaded all the
	 * edges yes!
	 *
	 * @return
	 *
	 * @see #isItemsComplete()
	 */
	boolean isEdgesComplete();

	/**
	 * Returns {@code true} if this structure contains items not hosted in any of its
	 * base containers.
	 *
	 * @return
	 */
	boolean isAugmented();

	/**
	 * Returns a set of metadata providing additional information about this structure.
	 * <p>
	 * Note that there is no defined caching policy imposed on the structure implementation,
	 * meaning that it is perfectly legal to compute a new {@code StructureInfo} object each
	 * time this method is called.
	 * If client code wishes to use a given {@code StructureInfo} instance multiple times it
	 * should employ its own caching mechanism.
	 *
	 * @return
	 */
	@OptionalMethod
	StructureInfo getInfo();

	/**
	 * Extends the semantics of {@link Container#containsItem(Item)}:
	 * An item is contained in a structure if it is either that structrue's
	 * {@link #getVirtualRoot() virtual root} or is a member of its node
	 * collection which is checked via the super method {@link Container#containsItem(Item)}.
	 *
	 * @see de.ims.icarus2.model.api.members.container.Container#containsItem(de.ims.icarus2.model.api.members.item.Item)
	 */
	@Override
	default boolean containsItem(Item item) {
		requireNonNull(item);
		return item==getVirtualRoot() || Container.super.containsItem(item);
	}

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

	default boolean containsEdge(Edge edge) {
		return indexOfEdge(edge)!=IcarusUtils.UNSET_LONG;
	}

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

	default long getOutgoingEdgeCount(Item node) {
		return getEdgeCount(node, true);
	}

	default long getIncomingEdgeCount(Item node) {
		return getEdgeCount(node, false);
	}

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
	 * structure's {@code StructureType type}:<br>
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
	 * this structure is neither {@value StructureType#SET} nor {@value StructureType#GRAPH}.
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
	 * <p>
	 * This is an optional method. If a structure does not support this kind of information
	 * it should simply return {@code -1};
	 *
	 * @param node
	 * @return
	 */
	@OptionalMethod
	long getHeight(Item node);

	/**
	 * Returns the length of the longest path from the {@link #getVirtualRoot() root node} to
	 * any of the reachable leafs in the structure.
	 * <p>
	 * This is an optional method. If a structure does not support this kind of information
	 * it should simply return {@code -1};
	 *
	 * @return
	 */
	@OptionalMethod
	default long getHeight() {
		return getHeight(getVirtualRoot());
	}

	/**
	 * Returns the length of the path up to the virtual root of this structure.
	 * If the given {@code node} is the virtual root itself the method returns {@code 0}. Note
	 * that a return value of {@code -1} signals that the specified node has no valid 'parent'
	 * path to the virtual root and the actual depth could not be calculated.
	 * <p>
	 * This is an optional method. If a structure does not support this kind of information
	 * it should simply return {@code -1};
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
	 * <p>
	 * This is an optional method. If a structure does not support this kind of information
	 * it should simply return {@code -1};
	 *
	 * @param parent
	 * @return
	 */
	@OptionalMethod
	long getDescendantCount(Item parent);

	// EDIT METHODS

	/**
	 * Short-hand version of {@link #addEdge(long, Edge)} that tries to
	 * append the given {@code edge} to the end of this structrue's internal
	 * edge list.
	 * <p>
	 * For all practical purposes this method should be preferred by client
	 * code over the {@link #addEdge(long, Edge)} method, as unlike a {@link Container}
	 * the responsibility for organizing edges resides solely by the respective
	 * {@link Structure} implementation. Therefore it is always safer to not
	 * assume a selected insertion index might be supported by structure.
	 *
	 * @param edge
	 * @return
	 */
	default long addEdge(Edge edge) {
		return addEdge(Math.max(0, getEdgeCount()-1), edge);
	}

	/**
	 * Adds the given {@code edge} to the internal edge storage and
	 * returns the position the edge is positioned at. Note that it is
	 * up to the actual implementation whether or not to honor the
	 * specified {@code index} at which to insert the new edge.
	 * <p>
	 * Note that this method should only be used by the model framework
	 * itself or by driver code. See the {@link #addEdge(Edge)} method
	 * for the preferred way of adding edges to a structure when
	 * doing it from client code.
	 *
	 * @param index
	 * @param edge
	 * @return the actual index the edge was inserted at
	 *
	 * @see #addEdge(Edge)
	 */
	@Internal
	long addEdge(long index, Edge edge);

	/**
	 * Public API equivalent of {@link #addEdges(long, DataSequence)}
	 * to allow batch insertions of edges.
	 *
	 * @param edges
	 */
	default void addEdges(DataSequence<? extends Edge> edges) {
		addEdges(getEdgeCount(), edges);
	}

	/**
	 * Adds the given sequence of edges to this structure. Note that
	 * it is up to the actual implementation whether or not to honor
	 * specified {@code index} at which to insert the new edges.
	 * <p>
	 * Note that this method should only be used by the model framework
	 * itself or by driver code. See the {@link #addEdges(DataSequence)} method
	 * for the preferred way of adding multiple edges to a structure when
	 * doing it from client code.
	 *
	 * @param index
	 * @param edges
	 */
	@Internal
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
	 * Removes from this structure all edges in the given range.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	DataSequence<? extends Edge> removeEdges(long index0, long index1);

	/**
	 * Swaps the edges currently located at positions {@code index0} and {@code index1}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void swapEdges(long index0, long index1);

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

	/**
	 * Performs the given {@code action} for every edge in this structure.
	 * @param action
	 */
	default void forEachNode(BiConsumer<? super Structure, ? super Item> action) {
		long itemCount = getItemCount();
		for(long i = 0L; i<itemCount; i++) {
			action.accept(this, getItemAt(i));
		}
	}

	/**
	 * Performs the given {@code action} for every edge in this structure.
	 * @param action
	 */
	default void forEachEdge(BiConsumer<? super Structure, ? super Edge> action) {
		long edgeCount = getEdgeCount();
		for(long i = 0L; i<edgeCount; i++) {
			action.accept(this, getEdgeAt(i));
		}
	}

	/**
	 * Applies the given {@code action} to all edges in the order in which they are returned when
	 * iterating via {@link #getEdgeAt(long)} with increasing index values.
	 *
	 * @param action
	 */
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

	/**
	 * Returns a sequential {@link Stream} of the nodes in this container.
	 * This includes all the items provided by the regular {@link #elements()}
	 * stream plus the {@link #getVirtualRoot() virtual root node}.
	 *
	 * @return
	 */
	default Stream<Item> nodes() {
		return ModelStreams.newNodeStream(this);
	}

	/**
	 * Returns a sequential {@link Stream} of the edges in this container.
	 *
	 * @return
	 */
	default Stream<Edge> edges() {
		return ModelStreams.newEdgeStream(this);
	}
}
