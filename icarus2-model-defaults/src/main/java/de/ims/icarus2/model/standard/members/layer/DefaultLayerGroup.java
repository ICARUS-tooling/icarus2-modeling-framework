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
package de.ims.icarus2.model.standard.members.layer;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Context;
import de.ims.icarus2.model.api.layer.Dependency;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.layer.Layer;
import de.ims.icarus2.model.api.layer.LayerGroup;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.annotations.TestableImplementation;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
@TestableImplementation(LayerGroup.class)
public class DefaultLayerGroup extends AbstractPart<Context> implements LayerGroup {

	private final Set<Layer> layers = new ReferenceOpenHashSet<>();
	private final Set<Dependency<LayerGroup>> dependencies = new ReferenceOpenHashSet<>();
	private final LayerGroupManifest manifest;
	private ItemLayer primaryLayer;

	public DefaultLayerGroup(LayerGroupManifest manifest) {
		requireNonNull(manifest);

		this.manifest = manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getContext()
	 */
	@Override
	public Context getContext() {
		checkAdded();
		return getOwner();
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getLayers()
	 */
	@Override
	public Set<Layer> getLayers() {
		return CollectionUtils.getSetProxy(layers);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getManifest()
	 */
	@Override
	public LayerGroupManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#getPrimaryLayer()
	 */
	@Override
	public ItemLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#forEachDependency(java.util.function.Consumer)
	 */
	@Override
	public void forEachDependency(Consumer<? super Dependency<LayerGroup>> action) {
		dependencies.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.LayerGroup#forEachLayer(java.util.function.Consumer)
	 */
	@Override
	public void forEachLayer(Consumer<? super Layer> action) {
		layers.forEach(action);
	}

	/**
	 * Changes the primary layer of this group. Note that the new primary layer must
	 * be added as regular layer to this group prior to making this method call!
	 *
	 * @param primaryLayer
	 */
	public void setPrimaryLayer(ItemLayer primaryLayer) {
		requireNonNull(primaryLayer);
		if(!layers.contains(primaryLayer))
			throw new ModelException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "Layer is unknown to this group: "+ModelUtils.getName(primaryLayer)); //$NON-NLS-1$

		this.primaryLayer = primaryLayer;
	}

	/**
	 * Adds the layer to the internal set of layers, discarding potential duplicates.
	 * @param layer
	 */
	public void addLayer(Layer layer) {
		requireNonNull(layer);
//		if(layers.contains(layer))
//			throw new IllegalArgumentException("Layer already added: "+ModelUtils.getName(layer)); //$NON-NLS-1$

		layer.addNotify(this);

		layers.add(layer);
	}

	/**
	 * Adds the specified dependency to the internal set of dependencies, discarding potential
	 * duplicates.
	 * @param dependency
	 */
	public void addDependency(Dependency<LayerGroup> dependency) {
		requireNonNull(dependency);
//		if(dependencies.contains(dependency))
//			throw new IllegalArgumentException("Dependency already added: "+dependency); //$NON-NLS-1$

		dependencies.add(dependency);
	}
}
