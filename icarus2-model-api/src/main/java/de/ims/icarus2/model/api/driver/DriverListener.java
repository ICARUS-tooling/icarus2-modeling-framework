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
 */
package de.ims.icarus2.model.api.driver;

import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;

/**
 * Allows intercepting loaded or skipped chunks during load operations of a driver implementation.
 *
 * @author Markus Gärtner
 *
 */
public interface DriverListener {

//	Corpus getCorpus();

	/**
	 * Callback to signal the successful loading of a new data chunk
	 * (typically a {@link Container} implementation) by a driver.
	 * Note that this method will only be called for top-level members!
	 *
	 *
	 * @param layer the layer for which data chunks have been loaded (typically
	 * the primary layer of a {@code LayerGroup})
	 * @param info the collection of data chunks in the form of {@code Item} instances
	 * that have been loaded successfully
	 */
	void chunksLoaded(ItemLayer layer, ChunkInfo info);

	/**
	 * Callback to broadcast the imminent removal of data chunks from a driver's
	 * internal cache.
	 * Note that this method will only be called for top-level members!
	 * A driver instance will fire this event as result to a call to
	 * its {@link Driver#release(de.ims.icarus2.model.api.driver.indices.IndexSet[], ItemLayer)}
	 * method when the use counter for at least {@code 1} item reached {@code 0}.
	 * Note that by the time this method is called the driver is <b>not</b> required
	 * to still provide access to the items contained in the {@code info} argument.
	 * The sole purpose of this method is to give other components that rely on a driver's
	 * content (for example foreign drivers that add annotations to it) get notified
	 * and have the chance to clean up their own data to stay "synchronized".
	 *
	 * @param layer the layer for which data chunks will be removed (typically
	 * the primary layer of a {@code LayerGroup})
	 * @param info the collection of data chunks in the form of {@code Item} instances
	 * that are about to be removed
	 */
	void chunksReleased(ItemLayer layer, ChunkInfo info);

	/**
	 * Signals that a certain set data chunks could not be loaded. The reason is typically
	 * one of the following:
	 * <ol>
	 * <li>A data chunk has already been loaded before and the driver only added data
	 * to additional layers</li>
	 * <li>The driver detected an inconsistency and considers a data chunk for the respective
	 * index invalid. This only happens when there is an item object already existing for this
	 * index. As a result to this method call the {@code DriverListener} should discard all stored data
	 * for the specified chunk and later attempt to load it again.</li>
	 * </ol>
	 * The latter case is signaled with the associated {@code ChunkState} argument being {@link ChunkState#CORRUPTED}.
	 * Note that the {@code Item} associated with the given index will still be available
	 * through the layer's cache as long as the {@code corrupted} argument is {@code false}.
	 * <p>
	 * Note that this method will only be called for top-level members!
	 *
	 * @param layer
	 * @param info
	 */
	void chunksSkipped(ItemLayer layer, ChunkInfo info);

//	/**
//	 * Lookup an existing chunk in the specified layer. If no item could be found in the data
//	 * storage for the given {@code index} then this method should return {@code null}.
//	 * <p>
//	 * Note that there is no dedicated method for the lookup of {@link Edge}s, since it is not possible
//	 * to partially load the edges of a {@link Structure}. Unlike regular {@link Container}s they would lose an
//	 * important aspect of their content in discarding edges.
//	 *
//	 * @param layer
//	 * @param index the global index of the {@code Item} to be fetched, unaffected by horizontal
//	 * filtering and without index translation.
//	 * @return
//	 */
//	Item getChunk(ItemLayer layer, long index);
}