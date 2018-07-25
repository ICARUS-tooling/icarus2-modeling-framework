/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.api.corpus;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.api.members.NamedCorpusMember;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.util.Connectible;
import de.ims.icarus2.util.Part;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.id.UnknownIdentifierException;

/**
 * @author Markus Gärtner
 *
 */
public interface Context extends ManifestOwner<ContextManifest>, Connectible<Driver>, Part<Corpus>, NamedCorpusMember {

	/**
	 * If this context contains one or more item layers it has to define
	 * one primary layer among them.
	 * @return
	 */
	ItemLayer getPrimaryLayer();

	/**
	 * The foundation layer contains the atomic units of a context. Note however, that
	 * a context is not required to declare its own foundation layer, but can use that
	 * of another context it is depending on. Typically a context's foundation layer
	 * will be the foundation layer of its declared primary layer.
	 * <p>
	 * It is recommended (but not enforced) that the host context of any foundation layer
	 * be static in the sense that only constructive modifications are possible, i.e.
	 * only new data points can be appended, but the existing order of elements in the
	 * foundation layer is to remain unchanged!
	 *
	 * @return
	 */
	ItemLayer getFoundationLayer();

	/**
	 * Returns all the layer groups defined in this context
	 * @return
	 */
	default List<LayerGroup> getLayerGroups() {
		LazyCollection<LayerGroup> result = LazyCollection.lazyList();
		forEachLayerGroup(result);
		return result.getAsList();
	}

	void forEachLayerGroup(Consumer<? super LayerGroup> action);

	@Override
	ContextManifest getManifest();

	void forEachLayer(Consumer<? super Layer> action);

	/**
	 * Returns all the layers in this context.
	 *
	 * @return
	 */
	default Collection<Layer> getLayers() {
		return getLayers(l -> true);
	}

	default Collection<Layer> getLayers(Predicate<? super Layer> p) {
		LazyCollection<Layer> buffer = LazyCollection.lazyList();

		forEachLayer(l -> {if(p.test(l)) buffer.add(l);});

		return buffer.getAsList();
	}

	default Collection<Layer> getLayers(LayerType type) {
		return getLayers(l -> type.equals(l.getManifest().getLayerType()));
	}

	default Collection<Layer> getLayers(Class<? extends Layer> clazz) {
		return getLayers(clazz::isInstance);
	}

	/**
	 * Looks up a layer by its {@code id}. This method both looks for the desired layer in
	 * all the layers declared in this context <b>and</b> all layers accessible
	 * through resolved {@link PrerequisiteManifest prerequisites} in the specifying
	 * manifest.
	 * <p>
	 * This behavior allows for example driver implementations that create all the
	 * content lazily by invoking NLP tools to process the data provided by external
	 * layers.
	 *
	 * @param id
	 * @return
	 *
	 * @throws UnknownIdentifierException if the given id is not mappable to a layer
	 */
	<L extends Layer> L getLayer(String id);

	/**
	 * Returns the native layer mapped to the given id. A native layer is a layer defined
	 * by this context's own manifest.
	 *
	 * @param id
	 * @return
	 *
	 * @throws UnknownIdentifierException if the given id is not mappable to a native layer
	 */
	Layer getNativeLayer(String id);

	/**
	 * Returns the shared {@code Driver} instance that is used to access
	 * and manage the content represented by this context.
	 */
	Driver getDriver();

	/**
	 * Called by a corpus to signal a context that it has been added.
	 * <p>
	 * Note that this method will <b>not</b> be called when a context is
	 * assigned default context for a corpus!
	 *
	 * @param corpus The corpus this context has been added to
	 */
	@Override
	void addNotify(Corpus corpus);

	/**
	 * Called by a corpus to signal a context that it has been removed.
	 * <p>
	 * Note that this method will <b>not</b> be called for default contexts
	 * since they cannot be removed without completely invalidating their
	 * respective host corpus!
	 *
	 * @param corpus The corpus this context has been removed from
	 */
	@Override
	void removeNotify(Corpus corpus);

	/**
	 * Called by a {@code Driver} when it gets connected to a context instance.
	 *
	 * @param driver
	 */
	@Override
	void connectNotify(Driver driver);

	/**
	 * Called by a {@code Driver} when it gets disconnected from a context instance.
	 *
	 * @param driver
	 */
	@Override
	void disconnectNotify(Driver driver);

	/**
	 * A normal context cannot be structurally changed at runtime while being live.
	 * Virtual contexts (such as those created programmatically to host search results)
	 * however can get new (virtual) data added to them (and also have it removed again).
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface VirtualContext extends Context {
		void addLayer(Layer layer);
		void removeLayer(Layer layer);
	}
}
