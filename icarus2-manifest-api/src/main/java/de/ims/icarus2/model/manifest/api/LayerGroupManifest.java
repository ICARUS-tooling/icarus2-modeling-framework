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
package de.ims.icarus2.model.manifest.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;


/**
 * Layer groups describe logical compounds of layers within a single context.
 *
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LayerGroupManifest extends ModifiableIdentity, ManifestFragment, Embedded {

	public static final boolean DEFAULT_INDEPENDENT_VALUE = false;

	@Override
	default ManifestType getManifestType() {
		return ManifestType.LAYER_GROUP_MANIFEST;
	}

	@AccessRestriction(AccessMode.READ)
	default <M extends ContextManifest> Optional<M> getContextManifest() {
		return getHost();
	}

	@AccessRestriction(AccessMode.READ)
	int layerCount();

	@AccessRestriction(AccessMode.READ)
	void forEachLayerManifest(Consumer<? super LayerManifest<?>> action);

	/**
	 * Returns the list of manifests that describe the layers in this group.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<LayerManifest<?>> getLayerManifests() {
		LazyCollection<LayerManifest<?>> result = LazyCollection.lazyList();

		forEachLayerManifest(result);

		return result.getAsList();
	}

	/**
	 * Returns the primary layer of this group.
	 * Note that unlike many other methods in this framework that link to
	 * other layers (such as {@link ItemLayerManifestBase#getBaseLayerManifests()}
	 * this one does <b>not</b> return an optional of {@link TargetLayerManifest}
	 * since the returned layer <b>must</b> be a member of this group and as such
	 * is not obtained by resolving a (potentially foreign) id or alias.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	<L extends ItemLayerManifestBase<?>> Optional<L> getPrimaryLayerManifest();

	/**
	 * Signals that the layers in this group do not depend on external data hosted in other
	 * groups within the same context. Note that this does <b>not</b> mean the layers are totally
	 * independent of content that resides in another context! Full independence is given when
	 * both this method and {@link ContextManifest#isIndependentContext()} of the describing
	 * manifest of the surrounding context return {@code true}.
	 * <p>
	 * Default is {@code false}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isIndependent();

	/**
	 * Performs a group local lookup for the given layer id. This method does <b>not</b>
	 * resolve layer ids on the context level!
	 *
	 * @param id
	 * @return the layer mapped to {@code id} if one could be found or an empty {@link Optional} otherwise
	 */
	@AccessRestriction(AccessMode.READ)
	<L extends LayerManifest<?>> Optional<L> getLayerManifest(String id);

	/**
	 * Tests whether this {@code LayerGroupManifest} equals the given {@code Object} {@code o}.
	 * Two {@code LayerGroupManifest} instances are considered equal if they have the same name
	 * attribute as returned by {@link #getName()}.
	 *
	 * @param obj
	 * @return
	 */
	@Override
	boolean equals(Object o);

	// Modification methods

	LayerGroupManifest addLayerManifest(LayerManifest<?> layerManifest);

	LayerGroupManifest removeLayerManifest(LayerManifest<?> layerManifest);

	LayerGroupManifest setPrimaryLayerId(String primaryLayerId);

	LayerGroupManifest setIndependent(boolean isIndependent);

	@Override
	LayerGroupManifest setDescription(String description);
	@Override
	LayerGroupManifest setName(String name);
	@Override
	LayerGroupManifest setId(String id);
}
