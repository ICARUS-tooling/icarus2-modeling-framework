/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * A {@code LayerManifest} describes a single {@link Layer} in a corpus and
 * defines an optional set of prerequisites that have to be met for the layer
 * to work properly.
 * <p>
 * Note:<br>
 * A layer declaring any kind of inter-layer relationship (like a base or
 * boundary layer) must be hosted within a valid layer group and context
 * environment! Otherwise it will not be possible to resolve the targets of
 * those relations. Not complying to this specification will result in
 * a {@link ModelException} of type {@value ModelError#MANIFEST_MISSING_ENVIRONMENT}
 * being thrown during parsing of the manifest or at verification time.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LayerManifest<L extends LayerManifest<L>> extends MemberManifest<L>, Embedded {

	@AccessRestriction(AccessMode.READ)
	Optional<ContextManifest> getContextManifest();

	/**
	 * Returns the group manifest this layer is a part of.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default <M extends LayerGroupManifest> Optional<M> getGroupManifest() {
		return getHost();
	}

	/**
	 * Returns the optional layer type that acts as another abstraction mechanism
	 * to unify layers that share a common content structure. Note that all
	 * layer type instances are globally unique within the scope of a single
	 * {@link ManifestRegistry registry} and are shared between all the
	 * layers of that type.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<LayerType> getLayerType();

	boolean isLocalLayerType();

	@AccessRestriction(AccessMode.READ)
	void forEachBaseLayerManifest(Consumer<? super TargetLayerManifest> action);

	@AccessRestriction(AccessMode.READ)
	void forEachLocalBaseLayerManifest(Consumer<? super TargetLayerManifest> action);

	/**
	 * Returns the list of (resolved) base layers for this layer manifest.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<TargetLayerManifest> getBaseLayerManifests() {
		LazyCollection<TargetLayerManifest> result = LazyCollection.lazyList();

		forEachBaseLayerManifest(result);

		return result.getAsList();
	}

	default boolean isBaseLayerManifest(ItemLayerManifestBase<?> layerManifest) {
		requireNonNull(layerManifest);
		for(TargetLayerManifest target : getBaseLayerManifests()) {
			if(target.getResolvedLayerManifest()
					.orElseThrow(ManifestException.error("Unresolvable base layer"))
					.equals(layerManifest)) {
				return true;
			}
		}

		return false;
	}

	@AccessRestriction(AccessMode.READ)
	default List<TargetLayerManifest> getLocalBaseLayerManifests() {
		LazyCollection<TargetLayerManifest> result = LazyCollection.lazyList();

		forEachLocalBaseLayerManifest(result);

		return result.getAsList();
	}

	// Modification methods

	L setLayerTypeId(String layerTypeId);

	default TargetLayerManifest addAndGetBaseLayer(String baseLayerId) {
		return IcarusUtils.extractSupplied(action -> addBaseLayerId(baseLayerId, action));
	}

	L addBaseLayerId(String baseLayerId, @Nullable Consumer<? super TargetLayerManifest> action);

	L removeBaseLayerId(String baseLayerId);

	/**
	 * Models a resolved dependency on the layer level. A target layer may either be
	 * a local layer, hosted within the same context, or a foreign layer that has been
	 * resolved by means of binding a prerequisite manifest declaration to a layer manifest.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface TargetLayerManifest extends Embedded {

		/**
		 * Returns the local {@code id} of the target layer.
		 * This can either be the actual {@link LayerManifest#getId() id} of a local layer
		 * or the {@link PrerequisiteManifest#getAlias() alias} of a {@link PrerequisiteManifest}
		 * if this manifest references a foreign layer.
		 *
		 * @return
		 */
		String getLayerId();

		/**
		 * Returns the source manifest for the dependency this manifest describes
		 * (that is the layer or context actually hosting a {@code TargetLayerManifest}).
		 *
		 * @return
		 */
		MemberManifest<?> getHostManifest();

		/**
		 * @see de.ims.icarus2.model.manifest.api.Embedded#getHost()
		 */
		@SuppressWarnings("unchecked")
		@Override
		default <M extends TypedManifest> Optional<M> getHost() {
			return (Optional<M>) Optional.of(getHostManifest());
		}

		/**
		 * When the target layer resides in a foreign context and was resolved using
		 * a prerequisite manifest, this method returns the used prerequisite. In the
		 * case of a local layer being targeted, the return value is {@code null}.
		 * @return
		 */
		Optional<ContextManifest.PrerequisiteManifest> getPrerequisite();

		/**
		 * Returns the actual target layer manifest this manifest refers to. Note that the
		 * return type is chosen to be the general {@link LayerManifest} class instead of the
		 * {@link ItemLayerManifestBase} usually used for base or boundary layer declarations.
		 * This is so that {@link FragmentLayerManifest}s or {@link HighlightLayerManifest}s
		 * do not have to declare another linking manifest to account for their respective layer
		 * dependencies. The actually required type of layer should be concluded from the context
		 * in which the target layer is to be resolved.
		 *
		 * @return
		 */
		Optional<LayerManifest<?>> getResolvedLayerManifest();
	}
}
