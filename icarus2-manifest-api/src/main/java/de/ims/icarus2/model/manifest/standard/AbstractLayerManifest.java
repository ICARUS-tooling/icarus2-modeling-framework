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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest> extends AbstractMemberManifest<L> implements LayerManifest {

	private final LayerGroupManifest layerGroupManifest;
	private final List<TargetLayerManifest> baseLayerManifests = new ArrayList<>(3);
	private LayerTypeLink layerType;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	protected AbstractLayerManifest(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerGroupManifest, LayerGroupManifest.class);

		this.layerGroupManifest = layerGroupManifest;
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
	public ContextManifest getContextManifest() {
		return layerGroupManifest==null ? null : layerGroupManifest.getContextManifest();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.LayerManifest#getGroupManifest()
	 */
	@Override
	public LayerGroupManifest getGroupManifest() {
		return layerGroupManifest;
	}

	/**
	 * @param layerType the layerType to set
	 */
	@Override
	public void setLayerTypeId(String layerTypeId) {
		checkNotLocked();

		setLayerTypeId0(layerTypeId);
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
	public LayerType getLayerType() {
		LayerType result = layerType==null ? null : layerType.get();
		if(result==null && hasTemplate()) {
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
	 * If the check fails an exception is thrown.
	 *
	 * @throws ManifestException of type {@link ManifestErrorCode#MANIFEST_MISSING_ENVIRONMENT}
	 * if the host check fails.
	 */
	protected void checkAllowsTargetLayer() {
		if(layerGroupManifest==null || layerGroupManifest.getContextManifest()==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_ENVIRONMENT,
					"Cannot make links to other layers without enclosing layer group or context: "+getId()); //$NON-NLS-1$
	}

	@Override
	public TargetLayerManifest addBaseLayerId(String baseLayerId) {
		checkNotLocked();

		return addBaseLayerId0(baseLayerId);
	}

	protected TargetLayerManifest addBaseLayerId0(String baseLayerId) {
		requireNonNull(baseLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest targetLayerManifest = createTargetLayerManifest(baseLayerId);
		baseLayerManifests.add(targetLayerManifest);
		return targetLayerManifest;
	}

	@Override
	public void removeBaseLayerId(String baseLayerId) {
		checkNotLocked();

		removeBaseLayerId0(baseLayerId);
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

	protected Link<LayerManifest> createLayerLink(String id) {
		return new GlobalLayerLink(id);
	}

	protected Link<PrerequisiteManifest> createPrerequisiteLink(String id) {
		return new PrerequisiteLink(id);
	}

	protected TargetLayerManifest createTargetLayerManifest(String id) {
		return new TargetLayerManifestImpl(id);
	}

	protected class GlobalLayerLink extends Link<LayerManifest> {

		/**
		 * @param id
		 */
		public GlobalLayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected LayerManifest resolve() {
			// This takes care of layer id resolution in terms of prerequisite aliases
			return getContextManifest().getLayerManifest(getId());
		}

	}

	protected class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		/**
		 * @param id
		 */
		public PrerequisiteLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected PrerequisiteManifest resolve() {
			return getContextManifest().getPrerequisite(getId());
		}

	}

	public class TargetLayerManifestImpl implements TargetLayerManifest {

		private Link<LayerManifest> resolvedLayer;
		private Link<PrerequisiteManifest> prerequisite;

		public TargetLayerManifestImpl(String targetId) {
			resolvedLayer = createLayerLink(targetId);
			prerequisite = createPrerequisiteLink(targetId);
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
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getLayerManifest()
		 */
		@Override
		public LayerManifest getLayerManifest() {
			return AbstractLayerManifest.this;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getPrerequisite()
		 */
		@Override
		public PrerequisiteManifest getPrerequisite() {
			return prerequisite.get();
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest#getResolvedLayerManifest()
		 */
		@Override
		public LayerManifest getResolvedLayerManifest() {
			return resolvedLayer.get();
		}
	}
}
