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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest<L>>
		extends AbstractMemberManifest<L, LayerGroupManifest> implements LayerManifest<L> {

	private final List<TargetLayerManifest> baseLayerManifests = new ArrayList<>(3);
	private LayerTypeLink layerType;

	private static final Function<LayerGroupManifest, Manifest> group2Context =
			group -> group.getContextManifest().orElse(null);

	protected AbstractLayerManifest(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest, LayerGroupManifest.class);
	}

	protected AbstractLayerManifest(LayerGroupManifest layerGroupManifest) {
		super(layerGroupManifest, group2Context, LayerGroupManifest.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && baseLayerManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerManifest#getContextManifest()
	 */
	@Override
	public Optional<ContextManifest> getContextManifest() {
		return getGroupManifest().flatMap(LayerGroupManifest::getContextManifest);
	}

	/**
	 * @param layerType the layerType to set
	 */
	@Override
	public L setLayerTypeId(String layerTypeId) {
		checkNotLocked();

		setLayerTypeId0(layerTypeId);

		return thisAsCast();
	}

	protected void setLayerTypeId0(String layerTypeId) {
		requireNonNull(layerTypeId);

		layerType = this.new LayerTypeLink(layerTypeId);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerManifest#isLocalLayerType()
	 */
	@Override
	public boolean isLocalLayerType() {
		return layerType!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerManifest#getLayerType()
	 */
	@Override
	public Optional<LayerType> getLayerType() {
		Optional<LayerType> result = Optional.ofNullable(layerType==null ? null : layerType.get());
		if(!result.isPresent() && hasTemplate()) {
			result = getTemplate().getLayerType();
		}
		return result;
	}

	@Override
	public void forEachBaseLayerManifest(Consumer<? super TargetLayerManifest> action) {
		if(hasTemplate()) {
			getTemplate().forEachBaseLayerManifest(action);
		}
		baseLayerManifests.forEach(action);
	}

	@Override
	public void forEachLocalBaseLayerManifest(Consumer<? super TargetLayerManifest> action) {
		baseLayerManifests.forEach(action);
	}

	/**
	 * Verifies that this layer is properly hosted within an enclosing
	 * {@link LayerGroupManifest} and {@link ContextManifest}.
	 * If the check fails an exception is thrown, unless the layer is
	 * declared to be a {@link #isTemplate() template}.
	 *
	 * @throws ManifestException of type {@link ManifestErrorCode#MANIFEST_MISSING_ENVIRONMENT}
	 * if the host check fails.
	 */
	protected void checkAllowsTargetLayer() {
		if(!isTemplate() && (!getGroupManifest().flatMap(LayerGroupManifest::getContextManifest).isPresent()))
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_ENVIRONMENT,
					"Cannot make links to other layers without enclosing layer group or context: "+getId()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerManifest#addBaseLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public L addBaseLayerId(String baseLayerId,
			@Nullable Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();

		IcarusUtils.consumeIfAble(addBaseLayerId0(baseLayerId), action);

		return thisAsCast();
	}

	protected TargetLayerManifest addBaseLayerId0(String baseLayerId) {
		requireNonNull(baseLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest targetLayerManifest = createTargetLayerManifest(baseLayerId, "base layer");

		if(baseLayerManifests.contains(targetLayerManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Duplicate base layer id: "+baseLayerId);

		baseLayerManifests.add(targetLayerManifest);

		return targetLayerManifest;
	}

	@Override
	public L removeBaseLayerId(String baseLayerId) {
		checkNotLocked();

		removeBaseLayerId0(baseLayerId);

		return thisAsCast();
	}

	protected void removeBaseLayerId0(String baseLayerId) {
		requireNonNull(baseLayerId);

		checkAllowsTargetLayer();

		for(Iterator<TargetLayerManifest> it = baseLayerManifests.iterator(); it.hasNext();) {
			if(baseLayerId.equals(it.next().getLayerId())) {
				it.remove();
				return;
			}
		}

		throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No base layer manifest defined for id: "+baseLayerId);
	}

	protected Link<LayerManifest<?>> createLayerLink(String id, String linkType) {
		return new GlobalLayerLink(id, linkType);
	}

	protected Link<PrerequisiteManifest> createPrerequisiteLink(String id) {
		return new PrerequisiteLink(id);
	}

	protected TargetLayerManifest createTargetLayerManifest(String id, String linkType) {
		return new TargetLayerManifestImpl(this,
				createLayerLink(id, linkType),
				createPrerequisiteLink(id));
	}

	protected class LayerTypeLink extends Link<LayerType> {

		/**
		 * @param id
		 */
		public LayerTypeLink(String id) {
			super(id, true);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#getMissingLinkDescription()
		 */
		@Override
		protected String getMissingLinkDescription() {
			return "No layer type for id: "+getId();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected Optional<LayerType> resolve() {
			return getRegistry().getLayerType(getId());
		}

	}

	protected class GlobalLayerLink extends Link<LayerManifest<?>> {

		private final String linkType;

		public GlobalLayerLink(String id, String linkType) {
			super(id, true);

			this.linkType = requireNonNull(linkType);
		}

		@Override
		protected String getMissingLinkDescription() {
			return "No layer of type '"+linkType+"' for id: "+getId();
		}

		@Override
		protected Optional<LayerManifest<?>> resolve() {
			// This takes care of layer id resolution in terms of prerequisite aliases
			return getContextManifest()
					.flatMap(c -> c.getLayerManifest(getId()).map(l -> (LayerManifest<?>)l));
		}

	}

	protected class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		/**
		 * @param id
		 */
		public PrerequisiteLink(String id) {
			super(id, true);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#getMissingLinkDescription()
		 */
		@Override
		protected String getMissingLinkDescription() {
			return "No prerequisite for id: "+getId();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected Optional<PrerequisiteManifest> resolve() {
			return getContextManifest()
					.flatMap(c -> c.getPrerequisite(getId()));
		}

	}

	public static class TargetLayerManifestImpl implements TargetLayerManifest {

		private Link<LayerManifest<?>> resolvedLayer;
		private Link<PrerequisiteManifest> prerequisite;
		private final MemberManifest<?> host;

		public TargetLayerManifestImpl(MemberManifest<?> host,
				Link<LayerManifest<?>> resolvedLayer,
				Link<PrerequisiteManifest> prerequisite) {
			this.host = requireNonNull(host);
			this.resolvedLayer = requireNonNull(resolvedLayer);
			this.prerequisite = requireNonNull(prerequisite);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getHostManifest()
		 */
		@Override
		public MemberManifest<?> getHostManifest() {
			return host;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getLayerId().hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} if(obj instanceof TargetLayerManifest) {
				TargetLayerManifest other = (TargetLayerManifest) obj;
				return getLayerId().equals(other.getLayerId());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TargetLayerManifest@"+getLayerId(); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return resolvedLayer.getId();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getPrerequisite()
		 */
		@Override
		public Optional<PrerequisiteManifest> getPrerequisite() {
			return Optional.ofNullable(prerequisite.get());
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getResolvedLayerManifest()
		 */
		@Override
		public Optional<LayerManifest<?>> getResolvedLayerManifest() {
			return Optional.ofNullable(resolvedLayer.get());
		}
	}
}
