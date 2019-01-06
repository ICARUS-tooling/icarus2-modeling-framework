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
package de.ims.icarus2.model.api.members.item;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.members.CorpusMember;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.api.view.paged.CorpusModel;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.util.IcarusUtils;
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
 * Every item within a layer has a unique constant id assigned to it either externally by the
 * backend storage it originated from or by the model framework when the resource is accessed
 * for the first time. This id is unique within the same layer and can be used for hashing or
 * as key in a map. Note that for static resources, especially file resources, an item's id is
 * typically equal to its position in the host layer.
 * <p>
 * Every item has a position with its host container or layer (in case the item is a top-level
 * member of that layer) available via {@link #getIndex()}. This position can be subject to
 * change over time depending on whether the corpus is {@link CorpusManifest#isEditable() editable}
 * and new items get inserted between already existing ones.
 * <p>
 * An item can optionally provide information about its location in a corpus via the
 * {@link #getBeginOffset() beginOffset} and {@link #getEndOffset() endOffset} values.
 * If returning values other than {@link IcarusUtils#UNSET_LONG -1} those offsets are
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
public interface Item extends CorpusMember {

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
	 * item is a top-level member and not hosted within a container.
	 */
	@AccessRestriction(AccessMode.ALL)
	Container getContainer();

	/**
	 * @see de.ims.icarus2.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	default Corpus getCorpus() {
		return getContainer().getCorpus();
	}

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
	default ItemLayer getLayer() {
		return getContainer().getLayer();
	}

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
	 * multiple {@link PagedCorpusView}s.
	 * The result of the {@code #getIndex()} method on the other hand is constant,
	 * no matter where the item in question is stored. The only way to modify
	 * a item's index is to remove or insert other items into the underlying data.
	 * <p>
	 * All <i>real</i> items are required to return a non-negative index value unless they
	 * are marked as {@link #isDirty() dirty} by their managing driver. The only items allowed
	 * to constantly return {@link IcarusUtils#UNSET_LONG -1} as index are the {@link Layer#getItemProxy() proxy} items
	 * assigned to every {@link Layer} and virtual {@link Structure#getVirtualRoot() root} nodes in {@link Structure structures}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	long getIndex();

	/**
	 * Returns the id that uniquely identifies this item within the scope of the
	 * host layer.
	 * <p>
	 * An item is required to return a valid non-negative value if it is declared
	 * to be {@link #isUsable() usable} and if it is <b>not</b> a {@link #isTopLevel() top-level}
	 * member! Otherwise, for example during construction time, a return value of {@link -1} is allowed.
	 *
	 * @return
	 */
	long getId();

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
	default long getBeginOffset() {
		return getIndex();
	}

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
	default long getEndOffset() {
		return getIndex();
	}

	default long getSpan() {
		long begin = getBeginOffset();
		if(begin==IcarusUtils.UNSET_LONG) {
			return IcarusUtils.UNSET_LONG;
		}

		long end = getEndOffset();
		if(end==IcarusUtils.UNSET_LONG) {
			return IcarusUtils.UNSET_LONG;
		}

		return end-begin+1;
	}

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

	/**
	 * Method combining all internal flags into one check.
	 * An item is considered usable if all 3 of the following conditions hold true:
	 * <ul>
	 * <li>It is {@link #isAlive() alive}</li>
	 * <li>It is <b>not</b> {@link #isLocked() locked}</li>
	 * <li>It is <b>not</b> {@link #isDirty() dirty}</li>
	 * </ul>
	 *
	 * The default implementation just calls the corresponding methods in succession.
	 * Subclasses might want to replace that behavior with one more suited to how
	 * they represent flag states.
	 *
	 * @return
	 */
	default boolean isUsable() {
		return isAlive() && !isLocked() && !isDirty();
	}

	/**
	 * Returns whether or not this item is considered <i>virtual</i>.
	 * Virtual items have no corresponding "portion" in their potential
	 * foundation layer. This is signaled by either of the two offset
	 * related methods ({@link #getBeginOffset() or {@link #getEndOffset()}})
	 * returning {@code -1}.
	 *
	 * @return
	 */
	default boolean isVirtual() {
		return getBeginOffset()==IcarusUtils.UNSET_LONG || getEndOffset()==IcarusUtils.UNSET_LONG;
	}

	/**
	 * Returns {@code true} if this item is directly nested within a {@link ItemLayer layer's}
	 * {@link Container#isProxy() proxy container}. The default implementation returns {@code true}
	 * if the {@link #getContainer() host container} is a proxy determined by {@link Container#isProxy()}.
	 * <p>
	 * Note that certain types of items, like
	 * {@link Edge edges} or {@link Structure#getVirtualRoot() virtual root nodes} can never be
	 * top-level members and their implementations should therefore override this method to
	 * simply always return {@code false}.
	 *
	 * @return
	 */
	default boolean isTopLevel() {
		return getContainer().isProxy();
	}

	/**
	 * Comfort interface to model items that are <i>externally</i> managed and therefore need
	 * setter methods for all important inner fields.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface ManagedItem extends Item {
		void setId(long id);
		void setContainer(Container container);
		void setAlive(boolean alive);
		void setDirty(boolean dirty);
		void setLocked(boolean locked);
	}
}
