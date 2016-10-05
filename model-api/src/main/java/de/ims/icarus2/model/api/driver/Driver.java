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

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.driver.indices.IndexCollector;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.mapping.Mapping;
import de.ims.icarus2.model.api.driver.mapping.MappingReader;
import de.ims.icarus2.model.api.driver.mapping.MappingStorage;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.item.ItemLayerManager;
import de.ims.icarus2.model.api.meta.AnnotationValueDistribution;
import de.ims.icarus2.model.api.meta.AnnotationValueSet;
import de.ims.icarus2.model.api.registry.LayerMemberFactory;
import de.ims.icarus2.model.api.view.Scope;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 *
 */
public interface Driver extends ItemLayerManager {

	// General methods

	/**
	 * Connects to the given corpus and initializes all internal resources
	 * so that subsequent calls to methods like {@link #getContext()} or
	 * {@link #getMappings()} will be successful without basic connection
	 * errors.
	 */
	void connect(Corpus target) throws InterruptedException;

	/**
	 * Called when a context is removed from a corpus or the entire model framework is shutting down.
	 * The driver implementation is meant to release all previously held resources and to disconnect
	 * from databases or other remote storages.
	 * <p>
	 * In addition drivers that support transaction-like modifications should flush pending changes
	 * when asked to close down.
	 * <p>
	 * Note that the behavior of a driver is undefined once it has been closed! References to closed
	 * driver instances should be discarded immediately.
	 * @throws InterruptedException
	 */
	void disconnect(Corpus target) throws InterruptedException;

	/**
	 * Returns {@code true} iff a previous call to {@link #connect(Corpus) connect}
	 * was successful and has not yet been followed by a matching {@link #disconnect(Corpus) disconnect}
	 * call.
	 *
	 * @see de.ims.icarus2.util.Connectible#isConnected()
	 */
	boolean isConnected();

	DriverManifest getManifest();

	/**
	 * Returns the {@code Context} this driver is currently connected to.
	 *
	 * @return
	 *
	 * @throws ModelException if there was an error initializing the context
	 * or if the driver has already been closed.
	 */
	Context getContext();

	// Unconnected phase

	/**
	 * Attempts to fetch the number of elements stored in the top-level container for the given
	 * layer. The returned value is meant to be the total number of items in that layer,
	 * unaffected by horizontal filtering. Implementations should cache these counts for all
	 * layers they are meant to manage. A return value of {@code -1} indicates that the
	 * implementation has no information about the specified layer's item count.
	 *
	 * @param layer
	 * @return
	 * @throws ModelException
	 */
	long getItemCount(ItemLayerManifest layer);

	/**
	 * Returns all the mappings available for the context this driver manages.
	 * <p>
	 * Note that the returned {@code MappingStorage} is only required to contain
	 * mappings that have been defined explicitly in the manifest of this driver!
	 *
	 * @return
	 */
	MappingStorage getMappings();

	/**
	 * Looks up the mapping that maps elements from the given {@code sourceLayer}
	 * into the specified {@code targetLayer}. This can either be a mapping previously
	 * defined in this driver's manifest or a compound mapping that chains several
	 * declared mappings together.
	 * <p>
	 * TODO explain possible scenarios for complex mapping relations?
	 *
	 * @param sourceLayer
	 * @param targetLayer
	 * @return the mapping to be used to map from {@code sourceLayer} to {@code targetLayer}
	 * or {@code null} if no such mapping is available
	 *
	 * @throws NullPointerException if either of the two arguments is {@code null}
	 * @throws ModelException if the {@code sourceLayer} is not managed by this driver or
	 * if both layers are unrelated, that is there exists no hierarchical dependency
	 * between the two
	 */
	Mapping getMapping(ItemLayerManifest sourceLayer, ItemLayerManifest targetLayer);

	default Mapping getMapping(ItemLayer sourceLayer, ItemLayer targetLayer) {
		return getMapping(sourceLayer.getManifest(), targetLayer.getManifest());
	}

	default IndexValueType getValueTypeForLayer(ItemLayerManifest manifest) {

		IndexValueType valueType = IndexValueType.LONG;

		long size = getItemCount(manifest);
		if(size!=NO_INDEX) {
			valueType = IndexValueType.forValue(size);
		}

		return valueType;
	}

	/**
	 * Performs an index lookup to return indices of items in the designated target layer
	 * using the specified elements in the given {@code source} layer.
	 *
	 * @param sourceLayer
	 * @param targetLayer
	 * @param sourceIndices
	 * @return
	 * @throws NullPointerException if any of the arguments is {@code null}
	 * @throws IllegalArgumentException if {@code targetLayer} is neither directly nor indirectly
	 * 			depending on {@code sourceLayer} or if the {@code sourceLayer} is not a member of
	 * 			the context this driver manages.
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	default IndexSet[] mapIndices(ItemLayerManifest sourceLayer, ItemLayerManifest targetLayer, IndexSet[] sourceIndices) throws InterruptedException {


		Mapping mapping = getMapping(sourceLayer, targetLayer);

		if(mapping==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_MAPPING,
					Messages.missingMappingMessage(null, sourceLayer, targetLayer));

		MappingReader reader = mapping.newReader();

		IndexSet[] result;

		reader.begin();
		try {
			result = reader.lookup(sourceIndices, null);
		} finally {
			reader.end();
			reader.close();
		}

		return result;
	}

	default public boolean mapIndices(ItemLayerManifest targetLayer,
			ItemLayerManifest sourceLayer, IndexSet[] indices, IndexCollector collector)
			throws InterruptedException {

		Mapping mapping = getMapping(sourceLayer, targetLayer);

		if(mapping==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_MAPPING,
					Messages.missingMappingMessage(null, sourceLayer, targetLayer));

		MappingReader reader = mapping.newReader();

		boolean result;

		reader.begin();
		try {
			result = reader.lookup(indices, collector, null);
		} finally {
			reader.end();
			reader.close();
		}

		return result;
	}
	/**
	 * Accesses the driver's internal indexing system and tries to fetch all the occurring values for a given
	 * annotation layer. If the optional {@code key} argument is non-null it defines the <i>sub-level</i> to
	 * get annotation values for.
	 *
	 * @param layer
	 * @param key the annotation key to be used in order to narrow down the amount of annotations
	 * 			to be considered or {@code null} if the layer's default annotation should be used.
	 * @return
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the specified {@code key} does not represent a legal entry in the
	 * 			given {@code AnnotationLayer}
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	AnnotationValueSet lookupValues(AnnotationLayerManifest layer, String key) throws InterruptedException;

	/**
	 * Performs a lookup very similar to {@link #lookupValues(AnnotationLayer, String)} but in addition returns
	 * for each encountered value the total count of occurrences.
	 *
	 * @param layer
	 * @param key the annotation key to be used in order to narrow down the amount of annotations
	 * 			to be considered or {@code null} if the layer's default annotation should be used.
	 * @return
	 * @throws NullPointerException if the {@code layer} argument is {@code null}
	 * @throws IllegalArgumentException if the specified {@code key} does not represent a legal entry in the
	 * 			given {@code AnnotationLayer}
	 * @throws ModelException
	 * @throws InterruptedException
	 */
	AnnotationValueDistribution lookupDistribution(AnnotationLayerManifest layer, String key) throws InterruptedException;

	// Module stuff

	void forEachModule(Consumer<? super DriverModule> action);

	default Collection<DriverModule> getModules() {
		LazyCollection<DriverModule> result = LazyCollection.lazyList();

		forEachModule(result);

		return result.getAsList();
	}

	default Collection<DriverModule> getModules(Predicate<? super DriverModule> p) {
		LazyCollection<DriverModule> result = LazyCollection.lazyList();

		forEachModule(m -> {
			if(p.test(m)) result.add(m);
		});

		return result.getAsList();
	}

	default boolean isReady() {
		MutableBoolean result = new MutableBoolean(true);

		forEachModule(m -> {
			if(!m.isReady()) result.setBoolean(false);
		});

		return result.booleanValue();
	}

	default void prepareModules(ModuleMonitor monitor) throws InterruptedException {

		Collection<DriverModule> modules = getModules();

		for(DriverModule module : modules) {
			if(!module.isReady() && !module.isBusy()) {
				module.prepare(monitor);
			}
		}
	}

	// Connected (live) phase

	/**
	 * Returns a {@link LayerMemberFactory} implementation that creates {@link Item} and {@link Edge}
	 * objects suitable for this driver. If the driver does not impose any implementation dependent
	 * restrictions on the member objects it manages, than this method should return {@code null}.
	 * <p>
	 * Note that although the name suggests otherwise it is <b>not</b> mandatory that the driver
	 * instantiate and return a completely new factory instance. It's perfectly legal to return
	 * a shared instance every time this method is called.
	 *
	 * @return
	 */
	LayerMemberFactory newFactory();

	/**
	 * Inserts a new {@code item} into the specified layer.
	 *
	 * @param layer
	 * @param item
	 * @param index
	 *
	 * @throws ModelException if the {@code layer} is declared as being static or the surrounding corpus
	 * is not editable.
	 */
	void addItem(ItemLayer layer, Item item, long index);
	void removeItem(ItemLayer layer, Item item, long index);

	void moveItem(ItemLayer layer, Item item, long fromIndex, long toIndex);

	//FIXME experimental transaction stuff, needs review!!!

	boolean hasPendingChanges();

	void flush();

	// Notification stuff

	void addDriverListener(DriverListener listener);

	void removeDriverListener(DriverListener listener);

	/**
	 * Synchronously attempts to load the given set of indices referencing chunks in the given layer.
	 * The implementation is responsible for translating the given {@code indices} into indices
	 * of the respective layer group and load chunks of that group. Note that this method must automatically
	 * increment the reference counters of each of the affected primary layer members by exactly {@code 1}, no matter
	 * how many lower members are referenced via the original indices. For every chunk of resolved items covered by the
	 * given indices the supplied {@code action} implementation is used. However, the order in which data chunks appear
	 * when passed to that action is completely undefined and in no way mandatory to be related to the default order
	 * of items in the layer! Implementations are free to first gather already loaded items while gathering the index
	 * values of items that need to be loaded, before starting the actual loading of data.
	 * <p>
	 * Note furthermore that in case a context contains data that relies on foreign data hosted in another context,
	 * the driver is responsible for performing the required index lookups and load requests. The reason for
	 * delegating this responsibility to the driver implementation is twofold:<br>
	 * <ul>
	 * <li>The driver already contains the required knowledge to decide what external resources to load.</li>
	 * <li>It is perfectly legal for a driver to postpone the acquisition of external resources to the point where they
	 * are actually about to being accessed for the first time. Such lazy initialization of foreign dependencies can save
	 * resources and is essentially outside the control of the corpus framework itself!</li>
	 * </ul>
	 * <p>
	 * The returned value is meant to be a total count of data chunks that have been loaded and which are considered
	 * to be in a healthy state. Note that this count can be completely different from the number of index values
	 * contained in the {@code indices} parameter! Only in case the specified {@code layer} is actually the primary layer
	 * of its surrounding layer group can both counts be expected to be equal.
	 *
	 * @param indices
	 * @param layer
	 * @param action callback the manager should pass information to. Can be {@code null}
	 * @return
	 *
	 * @throws ModelException if the given {@link IndexSet} is empty or the specified {@code layer}
	 * 			is not managed by this driver.
	 * @throws InterruptedException
	 * @throws NullPointerException if either one of {@code indices} or {@code layer} is {@code null}
	 *
	 * @see Scope
	 */
	@Override
	long load(IndexSet[] indices, ItemLayer layer,
			Consumer<ChunkInfo> action) throws InterruptedException;
}
