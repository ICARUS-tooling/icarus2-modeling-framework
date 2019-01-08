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
package de.ims.icarus2.model.api.members.container;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.manager.ItemList;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifestBase;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.util.stream.ModelStreams;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.set.DataSet;


/**
 * Aggregation of {@link Item} object in an ordered fashion.
 *
 *
 * @author Markus Gärtner
 *
 */
public interface Container extends Item, ManifestOwner<ContainerManifestBase<?>>, ItemList {

	/**
	 * Returns the type of this container. This provides
	 * information about how contained {@code Item}s are ordered and
	 * if they represent a continuous subset of the corpus.
	 *
	 * @return The {@code ContainerType} of this {@code Container}
	 * @see ContainerType
	 */
	ContainerType getContainerType();

	default boolean isProxy() {
		return false;
	}

	/**
	 * Returns the {@link ContainerManifest} object that holds additional
	 * information about this container.
	 *
	 * @return
	 */
	@Override
	ContainerManifestBase<?> getManifest();

	/**
	 * Creates a verifier that can be used to check whether or not certain
	 * actions are currently possible on this container.
	 *
	 * @return
	 */
	ContainerEditVerifier createEditVerifier();

	/**
	 * @return The underlying containers if this container relies on the
	 * elements of other container objects. If the container is independent of any
	 * other containers it should return an empty set.
	 */
	DataSet<Container> getBaseContainers();

	/**
	 * Returns the {@code Container} that serves as bounding
	 * box for the items in this container. In most cases
	 * this will be a member of another {@code ItemLayer}
	 * that represents the sentence or document level. If this
	 * {@code Container} object only builds a virtual collection
	 * atop of other items and is not limited by previously
	 * defined <i>boundary containers</i> then this method should
	 * return {@code null}.
	 * <p>
	 * This is an optional method.
	 *
	 * @return
	 */
	Container getBoundaryContainer();

	/**
	 * Returns {@code true} in case the container contains all the elements it
	 * is supposed to contain. For extremely large containers or containers that
	 * only serve a secondary role in the task at hand (like boundary containers)
	 * it is possible that a driver decides to omit certain elements which would
	 * either be unaccessible or which are simply not needed.
	 *
	 * @return
	 */
	boolean isItemsComplete();

	/**
	 * Returns the number of {@code Item} objects hosted within this
	 * container.
	 * <p>
	 * Note that this does <b>not</b> include possible {@code Edge}s stored
	 * within this container in case it is a {@link Structure}!
	 *
	 * @return The number of {@code Item}s in this container
	 */
	@Override
	long getItemCount();

	/**
	 * Returns the {@code Item} stored at position {@code index} within
	 * this {@code Container}. Note that however elements in a container may
	 * be unordered depending on the {@code ContainerType} as returned by
	 * {@link #getContainerType()}, the same index has always to be mapped to
	 * the exact same {@code Item} within a single container!
	 *
	 * @param index The index of the {@code Item} to be returned
	 * @return The {@code Item} at position {@code index} within this container
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getItemCount()</tt>)
	 */
	@Override
	Item getItemAt(long index);

	default Item getFirstItem() {
		return getItemAt(0L);
	}

	default Item getLastItem() {
		return getItemAt(getItemCount()-1);
	}

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
	@Override
	long indexOfItem(Item item);

	default boolean containsItem(Item item) {
		return indexOfItem(item)!=IcarusUtils.UNSET_LONG;
	}

	/**
	 * Performs the given {@code action} for every element in this container.
	 *
	 * @param action
	 */
	default void forEachItem(BiConsumer<? super Container, ? super Item> action) {
		long size = getItemCount();
		for(long i = 0; i<size; i++) {
			action.accept(this, getItemAt(i));
		}
	}

	/**
	 * Returns a sequential {@link Stream} of the items in this container.
	 * <p>
	 * If this container is actually a {@link Structure} the returned stream will
	 * <b>not</b> contain the {@link Structure#getVirtualRoot() virtual root node}.
	 * If it is desired to also have this virtual node as part of the stream, the
	 * structure-specific {@link Structure#nodes()} method should be used.
	 *
	 * @return
	 */
	default Stream<Item> elements() {
		return ModelStreams.newElementStream(this);
	}
}
