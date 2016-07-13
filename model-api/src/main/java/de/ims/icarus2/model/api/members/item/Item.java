/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 * $Revision: 451 $
 *
 */
package de.ims.icarus2.model.api.members.item;

import de.ims.icarus2.model.api.ModelConstants;
import de.ims.icarus2.model.api.corpus.CorpusModel;
import de.ims.icarus2.model.api.corpus.CorpusView;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * The {@code Item} interface describes the basic building blocks of all corpora.
 * <p>
 * An item is a definite portion of the layer it appears in. The physical equivalent
 * of an item in the raw data is completely unrestricted. Therefore items can represent
 * tokens, sentences, document, text sections, spans over words, parts of an image or
 * samples/frames in audio data. No matter what an item currently represents, there are
 * a couple of common properties:
 * <p>
 * Each item other than the root containers of a layer must have a host container assigned
 * to them which can be fetched via {@link #getContainer()}. Note that an item can appear
 * in many containers (imagine word tokens being referenced by their enclosing sentence and
 * an arbitrary number of phrases or mentions). However, there is at most <b>one</b> container
 * that really owns an item!
 * <p>
 * Every item within a layer has a unique index defining the order of items in that layer.
 * This index is required to be equal to the position of the item in its layer. This means
 * that adding or removing items potentially changes a (potentially) huge number of item
 * indices. It is therefore advised not to use an item's internal index for hashing, since
 * it could compromise the storage when changes to a corpus occur.
 * <p>
 * An item can optionally provide information about its location in a corpus via the
 * {@link #getBeginOffset() beginOffset} and {@link #getEndOffset() endOffset} values.
 * If returning values other than {@link ModelConstants#NO_INDEX -1} those offsets are
 * expected to define a span in some foundation layer covered by this item. This allows
 * for positional comparison of items originating from different layers as long as they all
 * refer to the same foundation layer. In case at least one of the two aforementioned methods
 * returns {@code -1} the item in question is considered <i>virtual</i>, i.e. it has no
 * defined position or area of coverage in the corpus. An example for a virtual item is the
 * artificial root node each {@link Structure} is holding.
 * <p>
 * Besides properties defining its location and environment, an item holds a selection of
 * boolean flags indicating its internal state.
 * <p>
 * <table>
 * <tr>
 *  <th>Flag</th>
 *  <th>True</th>
 *  <th>False</th>
 *  <th>Default</th>
 *  <th>Behavior of {@link CorpusModel}</th>
 * </tr>
 * <tr>
 * 	<td>{@link #isAlive() alive}</td>
 *  <td>successfully returned from a driver's<br> {@link Driver#getItem(long, ItemLayer) getItem} method</td>
 *  <td>either uninitialized or recycled</td>
 *  <td>{@code true}</td>
 *  <td>Exception for any method on dead items</td>
 * </tr>
 * <tr>
 *  <td>{@link #isLocked() locked}</td>
 *  <td>currently processed by the driver</td>
 *  <td>in full management by model</td>
 *  <td>{@code false}</td>
 *  <td>Exception for any <i>write</i> method on locked items</td>
 * </tr>
 * <tr>
 *  <td>{@link #isDirty() dirty}</td>
 *  <td>inconsistent state (potentially unrecoverable)</td>
 *  <td>consistent with background data storage</td>
 *  <td>{@code false}</td>
 *  <td>Exception for any <i>read</i> method on dirty items</td>
 * </tr>
 * </table>
 * <p>
 * Note that flags are only changed by the driver responsible for an item's internal data. If a driver wishes
 * to not use the flag mechanism, it should make sure that the implementations it uses follow the above default
 * values.
 * To minimize management and event propagation overhead it is advised for drivers to only lock items if they
 * are subject to long running background computations (like reading and converting audio or video data).
 * Visualizations of items should include dedicated optical feedback of an item's state (especially if a state
 * differs from the default).
 *
 * @author Markus Gärtner
 *
 * @see Layer
 */
@AccessControl(AccessPolicy.DENY)
public interface Item extends CorpusMember, ModelConstants {

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
	@AccessRestriction(AccessMode.ALL)
	Container getContainer();

	/**
	 * Returns the {@code ItemLayer} this item is hosted in. For nested
	 * items this call should simply forward to the {@code Container} obtained
	 * via {@link #getContainer()} since storing a reference to the layer in each
	 * item in addition to the respective container is expensive. Top-level
	 * items should always store a direct reference to the enclosing layer.
	 *
	 * @return The enclosing {@code ItemLayer} that hosts this item object.
	 */
	@AccessRestriction(AccessMode.ALL)
	ItemLayer getLayer();

	/**
	 * Returns the item's global position in the hosting container. For base items
	 * this value will be equal to the begin and end offsets, but for aggregating objects
	 * like containers or structures the returned value will actually differ from their
	 * bounding offsets.
	 * <p>
	 * Do <b>not</b> mix up the returned index with the result of a call to
	 * {@link Container#indexOfItem(Item)}! The latter returns the <i>current</i> position
	 * of a item within that container's internal storage.
	 * This index can change over time and is most likely different when using containers from
	 * multiple {@link CorpusView}s.
	 * The result of the {@code #getIndex()} method on the other hand is constant,
	 * no matter where the item in question is stored. The only way to modify
	 * a item's index is to remove or insert other items into the underlying data.
	 * <p>
	 * All <i>real</i> items are required to return a non-negative index value unless they
	 * are marked as {@link #isDirty() dirty} by their managing driver. The only items allowed
	 * to constantly return {@link ModelConstants#NO_INDEX -1} as index are the {@link Layer#getItemProxy() proxy} items
	 * assigned to every {@link Layer} and virtual {@link Structure#getVirtualRoot() root} nodes in {@link Structure structures}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	long getIndex();

	/**
	 * Changes the index value associated with this item object to {@code newIndex}.
	 * Note that inserting or removing items from containers or structures might result
	 * in huge numbers of index changes!
	 *
	 * @param newIndex
	 */
	@AccessRestriction(AccessMode.WRITE)
	void setIndex(long newIndex);

	/**
	 * Returns the zero-based offset of this item's begin within the
	 * <i>foundation-layer</i> of its host {@link #getLayer() layer}.
	 * The first {@code Item} in the {@link ItemLayer} obtained via
	 * {@link ItemLayer#getFoundationLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getBeginOffset()} on the left-most item
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> items to return
	 * {@code -1} indicating that they are not really placed within
	 * a real location of the <i>foundation-layer</i>.
	 *
	 * @return The zero-based offset of this item's begin within the corpus
	 * or {@code -1} if the item is <i>virtual</i>
	 */
	@AccessRestriction(AccessMode.ALL)
	long getBeginOffset();

	/**
	 * Returns the zero-based offset of this item's end within the
	 * <i>foundation-layer</i> of its host {@link #getLayer() layer}.
	 * The first {@code Item} in the {@link ItemLayer} obtained via
	 * {@link ItemLayer#getFoundationLayer()} is defined to have offset {@code 0}. All other
	 * offsets are calculated relative to this. If this {@code Item} is a
	 * {@link Container} or {@link Structure} then the returned offset is the
	 * result of calling {@link Item#getEndOffset()} on the right-most item
	 * hosted within this object.
	 * <p>
	 * Note that is perfectly legal for <i>virtual</i> items to return
	 * {@code -1} indicating that they are not really placed within
	 * a real location of the <i>foundation-layer</i>.
	 *
	 * @return The zero-based offset of this item's end within the corpus
	 * or {@code -1} if the item is <i>virtual</i>
	 */
	@AccessRestriction(AccessMode.ALL)
	long getEndOffset();

	// Flags

	/**
	 * Signals whether or not this item is <i>alive</i>. Check the section about an item's life cycle
	 * in the general documentation of this interface.
	 *
	 * @return
	 */
	boolean isAlive();
	boolean isLocked();
	boolean isDirty();
}
