/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.view.paged;

import java.util.function.Consumer;

import javax.swing.event.ChangeListener;

import de.ims.icarus2.apiguard.OptionalMethod;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.Annotation;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.util.AccessMode;
import de.ims.icarus2.util.Changeable;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.seq.DataSequence;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * Provides a wrapper around <i>raw</i> read and write methods of
 * various lower-level framework members.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface CorpusModel extends Part<PagedCorpusView>, Changeable {

	//---------------------------------------------
	//			GENERAL METHODS
	//---------------------------------------------

	/**
	 *
	 * @return {@code true} iff this model was created from a sub corpus that has an
	 * access mode of {@value AccessMode#WRITE} or {@value AccessMode#READ_WRITE}.
	 */
	boolean isModelEditable();

	/**
	 * Indicates whether the sub corpus backing this model is complete, i.e. it is holding
	 * the entirety of data that represents its original corpus.
	 *
	 * @return {@code true} iff this model contains the entire corpus.
	 */
	boolean isModelComplete();

	/**
	 * Returns {@code true} iff the surrounding {@link PagedCorpusView} is operational and
	 * all internal resources of this model are in a healthy state. Note that once the
	 * view that created this model gets closed, this method will always return {@code false}.
	 */
	boolean isModelActive();

	default Corpus getCorpus() {
		return getView().getCorpus();
	}

	PagedCorpusView getView();

	/**
	 * Adds a {@code ChangeListener} to the list of registered listeners.
	 * Those listeners are notified when the model is experiencing changes
	 * as a result of pages being loaded or closed.
	 */
	@Override
	void addChangeListener(ChangeListener listener);

	@Override
	void removeChangeListener(ChangeListener listener);

	//---------------------------------------------
	//			MEMBER METHODS
	//---------------------------------------------

	MemberType getMemberType(CorpusMember member);

	default boolean isItem(CorpusMember member) {
		return getMemberType(member)==MemberType.ITEM;
	}

	default boolean isContainer(CorpusMember member) {
		return getMemberType(member)==MemberType.CONTAINER;
	}

	default boolean isStructure(CorpusMember member) {
		return getMemberType(member)==MemberType.STRUCTURE;
	}

	default boolean isEdge(CorpusMember member) {
		return getMemberType(member)==MemberType.EDGE;
	}

	default boolean isFragment(CorpusMember member) {
		return getMemberType(member)==MemberType.FRAGMENT;
	}

	default boolean isLayer(CorpusMember member) {
		return getMemberType(member)==MemberType.LAYER;
	}

	//---------------------------------------------
	//			LAYER METHODS
	//---------------------------------------------

	/**
	 * Returns the total number of top level items in the specified
	 * layer. Note that the returned value is the total number of root
	 * elements in the specified layer. To get the number of elements
	 * that are accessible for the layer within this {@code CorpusModel}
	 * instance, use the {@link #getRootContainer(ItemLayer)} method to
	 * get the container hosting the current top level elements and call
	 * {@link Container#getItemCount()} on it.
	 * <p>
	 * Typically a model implementation will forward this request to the
	 * appropriate driver instance's {@link Driver#getItemCount(ItemLayer)}
	 * method.
	 *
	 * @param layer
	 * @return
	 */
	long getSize(ItemLayer layer);

	/**
	 * Returns the container that stores the top level elements of the specified layer
	 * for the enclosing {@link PagedCorpusView}.
	 * <p>
	 * Note that this method will fail for all layers except the one designated as the
	 * <i>primary layer</i> of the {@link Scope} responsible for the vertical filtering
	 * of this model's view. The returned container will contain a number of elements
	 * equal to the current page count of the enclosing view.
	 *
	 * @param layer
	 * @return
	 *
	 * @throws ModelException if no page has been loaded so far or if there is no root container
	 * available for the specified layer.
	 */
	Container getRootContainer(ItemLayer layer);

	default Container getRootContainer() {
		return getRootContainer(getView().fetchPrimaryLayer());
	}

	//---------------------------------------------
	//			ITEM METHODS
	//---------------------------------------------

	/**
	 * If this item is hosted within a container, returns that enclosing
	 * container. Otherwise it represents a top-level item and returns
	 * {@code null}.
	 * <p>
	 * Note that this method returns the container that <b>owns</b> this item
	 * and not necessarily the one through which it was obtained! It is perfectly
	 * legal for a container to reuse the elements of another container and to
	 * augment the collection with its own intermediate items. For this
	 * reason it is advised to keep track of the container the item was
	 * fetched from when this method is called.
	 *
	 * @return The enclosing container of this item or {@code null} if this
	 * item is not hosted within a container.
	 */
	@AccessRestriction(de.ims.icarus2.util.access.AccessMode.ALL)
	Container getContainer(Item item);

	/**
	 * Returns the {@code ItemLayer} this item is hosted in. For nested
	 * items this call should simply forward to the {@code Container} obtained
	 * via {@link #getContainer()} since storing a reference to the layer in each
	 * item in addition to the respective container is expensive. Top-level
	 * items should always store a direct reference to the enclosing layer.
	 *
	 * @return The enclosing {@code ItemLayer} that hosts this item object.
	 */
	@AccessRestriction(de.ims.icarus2.util.access.AccessMode.ALL)
	ItemLayer getLayer(Item item);

	/**
	 * Returns the item's global position in the hosting container. For base items
	 * this value will be equal to the begin and end offsets, but for aggregating objects
	 * like containers or structures the returned value will actually differ from their
	 * bounding offsets.
	 * <p>
	 * Do <b>not</b> mix up the returned index with the result of a call to
	 * {@link Container#indexOfItem(Item)}! The latter is limited to values within the container's size
	 * and returns the <i>current</i> position of a item within that container's internal storage.
	 * This index can change over time and is most likely different when using containers from
	 * multiple {@link PagedCorpusView views}.
	 * The result of the {@code #getIndex()} method on the other features a much larger value space
	 * and is constant, no matter where the item in question is stored. The only way to modify
	 * a item's index is to remove or insert other items into the underlying data.
	 *
	 * @return
	 */
	@AccessRestriction(de.ims.icarus2.util.access.AccessMode.ALL)
	long getIndex(Item item);

//	/**
//	 * Changes the index value associated with this item object to {@code newIndex}.
//	 * Note that inserting or removing items from containers or structures might result
//	 * in huge numbers of index changes!
//	 *
//	 * @param newIndex
//	 */
//	@AccessRestriction(AccessMode.WRITE)
//	void setIndex(Item item, long newIndex);

	/**
	 * Returns the zero-based offset of this item's begin within the corpus.
	 * The first {@code Item} in the {@link ItemLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getBeginOffset()} on the left-most item
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> items to return
	 * {@code -1} indicating that they are not really placed within the corpus.
	 *
	 * @return The zero-based offset of this item's begin within the corpus
	 * or {@code -1} if the item is <i>virtual</i>
	 */
	@AccessRestriction(de.ims.icarus2.util.access.AccessMode.ALL)
	long getBeginOffset(Item item);

	/**
	 * Returns the zero-based offset of this item's end within the corpus.
	 * The first {@code Item} in the {@link ItemLayer} obtained via
	 * {@link Corpus#getBaseLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getEndOffset()} on the right-most item
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> items to return
	 * {@code -1} indicating that they are not really placed within the corpus.
	 *
	 * @return The zero-based offset of this item's end within the corpus
	 * or {@code -1} if the item is <i>virtual</i>
	 */
	@AccessRestriction(de.ims.icarus2.util.access.AccessMode.ALL)
	long getEndOffset(Item item);

	/**
	 * Returns whether or not the given {@code Item} is virtual, in which case
	 * begin and end offset of the item are both {@code -1}
	 *
	 * @param item
	 * @return
	 */
	boolean isVirtual(Item item);

	//---------------------------------------------
	//			CONTAINER METHODS
	//---------------------------------------------

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Item}s are ordered and
	 * if they represent a continuous subset of the corpus.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @see ContainerType
	 */
	ContainerType getContainerType(Container container);

	/**
	 * @return The underlying containers if this container relies on the
	 * elements of other container objects.
	 */
	DataSet<Container> getBaseContainers(Container container);

	/**
	 * Returns the {@code Container} that serves as bounding
	 * box for the items in the given container. In most cases
	 * this will be a member of another {@code ItemLayer}
	 * that represents the sentence or document level. If the
	 * {@code Container} object only builds a virtual collection
	 * atop of other items and is not limited by previously
	 * defined <i>boundary containers</i> then this method
	 * returns {@code null}.
	 *
	 * @return
	 */
	Container getBoundaryContainer(Container container);

	/**
	 * Returns the number of {@code Item} objects hosted within the given
	 * container.
	 * <p>
	 * Note that this does <b>not</b> include possible {@code Edge}s stored
	 * within the container in case it is a {@link Structure}!
	 * Note also that the returned number is subject to horizontal filtering
	 * in case the container
	 *
	 * @return The number of {@code Item}s in this container
	 *
	 * @see #getSize(ItemLayer)
	 */
	long getItemCount(Container container);

	/**
	 * Returns the {@code Item} stored at position {@code index} within
	 * this {@code Container}. Note that however elements in a container may
	 * be unordered depending on the {@code ContainerType} as returned by
	 * {@link #getErrorType()}, the same index has always to be mapped to
	 * the exact same {@code Item} within a single container!
	 *
	 * @param index The index of the {@code Item} to be returned
	 * @return The {@code Item} at position {@code index} within this container
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 */
	Item getItemAt(Container container, long index);

	/**
	 * Returns the index of the given {@code Item} within this container's
	 * list of items or {@code -1} if the item is not hosted within this
	 * container.
	 * <p>
	 * Note that for every item <i>m</i> that is hosted within some container the
	 * following will always return a result different from {@code -1}:<br>
	 * {@code m.getContainer().indexOfItem(m)}
	 * <p>
	 * Implementations are advised to ensure that lookup operations such as this one
	 * scale well with the number of items contained. Constant execution cost
	 * should be the standard goal!
	 *
	 * @param item The {@code Item} whose index is to be returned
	 * @return The index at which the {@code Item} appears within this
	 * container or {@code -1} if the item is not hosted within this container.
	 * @throws NullPointerException if the {@code item} argument is {@code null}
	 */
	long indexOfItem(Container container, Item item);

	/**
	 * Returns {@code true} if this container hosts the specified item.
	 * Essentially equal to receiving {@code -1} as result to a {@link #indexOfItem(Item)}
	 * call.
	 *
	 * @param item The item to check
	 * @return {@code true} iff this container hosts the given item
	 * @throws NullPointerException if the {@code item} argument is {@code null}
	 *
	 * @see #indexOfItem(Item)
	 */
	boolean containsItem(Container container, Item item);

	/**
	 * Adds a new item to this container, appending it at to the end.
	 *
	 * @param item
	 * @throws NullPointerException if the {@code item} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addItem(Container container, Item item);

	/**
	 * Adds a new item to this container
	 *
	 * Note that calling this method with an {@code index} parameter
	 * equal to the size of the mutating container as returned by
	 * {@link Container#getItemCount()} is equivalent to
	 * using {@link #addItem()}.
	 *
	 * @param index The position to insert the new item at
	 * @param item
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt; getItemCount()</tt>)
	 * @throws NullPointerException if the {@code item} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void addItem(Container container, long index, Item item);

	void addItems(Container container, long index, DataSequence<? extends Item> items);

	/**
	 * Removes and returns the item at the given index. Shifts the
	 * indices of all items after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the item to be removed
	 * @return The item previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Item removeItem(Container container, long index);

	/**
	 * First determines the index of the given item object within
	 * this container and then calls {@link #removeItem(int)}.
	 *
	 * @param item
	 * @return
	 * @see Container#indexOfItem(Item)
	 */
	boolean removeItem(Container container, Item item);

	/**
	 * Removes from the mutating container all items in the specified range.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeItems(Container container, long index0, long index1);

//	/**
//	 * Removes from the mutating container all items.
//	 * This is a shorthand method for {@link #removeItems(Container, long, long)}
//	 * with {@code 0} and {@link #getItemCount(Container) getItemCount()-1} as
//	 * parameters (using the given container).
//	 *
//	 * @throws UnsupportedOperationException if the corpus
//	 * is not editable or the operation is not supported by the implementation
//	 *
//	 * @see #removeItems(Container, long, long)
//	 */
//	void removeItems(Container container);

	/**
	 * Moves the item currently located at position {@code index0}
	 * over to position {@code index1}. The item previously located
	 * at position {@code index1} will then be moved to {@code index0}.
	 *
	 * @param index0
	 * @param index1
	 * @throws IllegalArgumentException if <tt>index0 == index1</tt>
	 * @throws IndexOutOfBoundsException if either {@code index0} or {@code index1}
	 * is out of range (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void swapItems(Container container, long index0, long index1);

//	/**
//	 * Shorthand method for moving a given item object.
//	 *
//	 * First determines the index of the given item object within
//	 * this container and then calls {@link #moveItem(int, int)}.
//	 *
//	 * @param item The item to be moved
//	 * @param index The position the {@code item} argument should be moved to
//	 * @see Container#indexOfItem(Item)
//	 */
//	void moveItem(Container container, Item item, long index);

	//---------------------------------------------
	//			STRUCTURE METHODS
	//---------------------------------------------

	/**
	 * Returns the <i>type</i> of this structure.
	 * @return the type of this structure
	 */
	StructureType getStructureType(Structure structure);

	/**
	 * Returns the total number of edges this structure hosts.
	 * @return the total number of edges this structure hosts.
	 */
	long getEdgeCount(Structure structure);

	/**
	 * Returns the {@link Edge} stored at the given position within this
	 * structure.
	 *
	 * @param index The position of the desired {@code Edge} within this structure
	 * @return The {@code Edge} at position {@code index}
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 */
	Edge getEdgeAt(Structure structure, long index);


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
	long indexOfEdge(Structure structure, Edge edge);

	/**
	 * Returns {@code true} if this structure hosts the specified edge.
	 *
	 * @param edge The edge to check
	 * @return {@code true} iff this structure hosts the given edge
	 * @throws NullPointerException if the {@code edge} argument is {@code null}
	 */
	boolean containsEdge(Structure structure, Edge edge);

	/**
	 * Return the total number of edges for a given node.
	 *
	 * @param node the node to query for the number of outgoing edges.
	 * @return the total number of edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	long getEdgeCount(Structure structure, Item node);

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
	long getEdgeCount(Structure structure, Item node, boolean isSource);

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
	Edge getEdgeAt(Structure structure, Item node, long index, boolean isSource);

	// Optional tree methods

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
	 * @return the node's parent or {@code null} if the node has no parent
	 */
	@OptionalMethod
	Item getParent(Structure structure, Item node);

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
	long indexOfChild(Structure structure, Item child);

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
	Item getSiblingAt(Structure structure, Item child, long offset);

	/**
	 * Returns the length of the longest path to a leaf node in the tree whose root the given
	 * item is. If the given {@code node} is a leaf itself the method returns {@code 0}.
	 *
	 * @param node
	 * @return
	 */
	@OptionalMethod
	long getHeight(Structure structure, Item node);

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
	long getDepth(Structure structure, Item node);

	/**
	 * Returns the total size of the tree whose root the given item is (minus the node itself).
	 * This includes children of the given node and all successive grand children. If called
	 * for a leaf node this method returns {@code 0}.
	 *
	 * @param parent
	 * @return
	 */
	@OptionalMethod
	long getDescendantCount(Structure structure, Item parent);

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
	Item getVirtualRoot(Structure structure);

	/**
	 * Returns whether or not the given {@code Item} is a root in this structure.
	 * The {@code root} property is determined by a node being directly linked to the
	 * <i>generic root</i> node as returned by {@link #getRoot()}.
	 *
	 * @param node The {@code Item} in question
	 * @return {@code true} iff the given {@code node} is a root in this structure
	 * @throws NullPointerException if the {@code node} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member of this
	 * structure
	 */
	boolean isRoot(Structure structure, Item node);

	// EDIT METHODS

	/**
	 * Adds the given {@code edge} to the internal edge storage.
	 *
	 * @param edge
	 * @return
	 */
	void addEdge(Structure structure, Edge edge);

	/**
	 * Adds the given {@code edge} to the internal edge storage at
	 * the given position
	 *
	 * @param edge
	 * @param index
	 * @return
	 *
	 * @see #addEdge(Edge)
	 */
	void addEdge(Structure structure, long index, Edge edge);

	void addEdges(Structure structure, long index, DataSequence<? extends Edge> edges);

	/**
	 * Removes and returns the edge at the given index. Shifts the
	 * indices of all edges after the given position to account
	 * for the missing member.
	 *
	 * @param index The position of the edge to be removed
	 * @return The edge previously at position {@code index}.
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	Edge removeEdge(Structure structure, long index);

	/**
	 * First determines the index of the given edge object within
	 * this structure and then calls {@link #removeEdge(int)}.
	 *
	 * @param edge
	 * @return
	 * @see Structure#indexOfEdge(Edge)
	 */
	boolean removeEdge(Structure structure, Edge edge);

	/**
	 * Removes from this structure all edges in the given range.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable or the operation is not supported by the implementation
	 */
	void removeEdges(Structure structure, long index0, long index1);

//	/**
//	 * Removes from this structure all edges.
//	 * @throws UnsupportedOperationException if the corpus
//	 * is not editable or the operation is not supported by the implementation
//	 */
//	void removeEdges(Structure structure);

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
	void swapEdges(Structure structure, long index0, long index1);

//	/**
//	 * Shorthand method for moving a given edge object.
//	 *
//	 * First determines the index of the given edge object within
//	 * this structure and then calls {@link #moveEdge(int, int)}.
//	 *
//	 * @param item The item to be moved
//	 * @param index The position the {@code edge} argument should be moved to
//	 * @see Structure#indexOfEdge(Edge)
//	 */
//	void moveEdge(Structure structure, Edge edge, long index);

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
	Item setTerminal(Structure structure, Edge edge, Item item, boolean isSource);

	//---------------------------------------------
	//			EDGE METHODS
	//---------------------------------------------

	Structure getStructure(Edge edge);

	Item getSource(Edge edge);

	Item getTarget(Edge edge);

	//---------------------------------------------
	//			FRAGMENT METHODS
	//---------------------------------------------

	/**
	 * Returns the item this fragment is a part of.
	 *
	 * @return
	 */
	Item getItem(Fragment fragment);

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentBegin(Fragment fragment);

	/**
	 * Returns the position within the surrounding item of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentEnd(Fragment fragment);

	// Modification methods

	/**
	 * Changes the begin position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @return the previous begin position of the fragment
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 */
	Position setFragmentBegin(Fragment fragment, Position position);

	/**
	 * Changes the end position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @return the previous end position of the fragment
	 * @throws ModelException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting item
	 */
	Position setFragmentEnd(Fragment fragment, Position position);

	//---------------------------------------------
	//			VALUE_MANIFEST METHODS
	//---------------------------------------------

	/**
	 * Collects all the keys in this layer which are mapped to valid annotation values for
	 * the given item. This method returns {@code true} iff at least one key was added
	 * to the supplied {@code buffer}. Note that this method does <b>not</b> take
	 * default annotations into consideration, since they are not accessed via a dedicated
	 * key!
	 *
	 * @param item
	 * @param action
	 * @return
	 * @throws NullPointerException if any one of the two arguments is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	boolean collectKeys(AnnotationLayer layer, Item item, Consumer<String> action);

	/**
	 * Returns the annotation for a given item and key or {@code null} if that item
	 * has not been assigned an annotation value for the specified key in this layer.
	 * Note that the returned object can be either an actual value or an {@link Annotation}
	 * instance that wraps a value and provides further information.
	 *
	 * @param item
	 * @param key
	 * @return
	 * @throws NullPointerException if either the {@code item} or {@code key}
	 * is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	Object getValue(AnnotationLayer layer, Item item, String key);

	int getIntegerValue(AnnotationLayer layer, Item item, String key);
	long getLongValue(AnnotationLayer layer, Item item, String key);
	float getFloatValue(AnnotationLayer layer, Item item, String key);
	double getDoubleValue(AnnotationLayer layer, Item item, String key);
	boolean getBooleanValue(AnnotationLayer layer, Item item, String key);

	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Item} and {@code key}, replacing any previously defined value.
	 * If the {@code value} argument is {@code null} any stored annotation
	 * for the combination of {@code item} and {@code key} will be deleted.
	 * <p>
	 * This is an optional method
	 *
	 * @param item The {@code Item} to change the annotation value for
	 * @param key the key for which the annotation should be changed
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code item} and {@code key} should be deleted
	 * @throws NullPointerException if the {@code item} or {@code key}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the supplied {@code value} is not
	 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
	 * This is only checked if the manifest actually defines such restrictions.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	Object setValue(AnnotationLayer layer, Item item, String key, Object value);

	int setIntegerValue(AnnotationLayer layer, Item item, String key, int value);
	long setLongValue(AnnotationLayer layer, Item item, String key, long value);
	float setFloatValue(AnnotationLayer layer, Item item, String key, float value);
	double setDoubleValue(AnnotationLayer layer, Item item, String key, double value);
	boolean setBooleanValue(AnnotationLayer layer, Item item, String key, boolean value);

	/**
	 *
	 * @return {@code true} iff the layer holds at least one valid annotation object.
	 */
	boolean hasAnnotations(AnnotationLayer layer);

	/**
	 *
	 * @return {@code true} iff the layer holds at least one valid annotation object
	 * for the specified item.
	 */
	boolean hasAnnotations(AnnotationLayer layer, Item item);
}
