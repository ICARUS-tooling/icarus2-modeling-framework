/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 447 $
 * $Date: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/AbstractLayerManifest.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.standard.Links.Link;
import de.ims.icarus2.model.manifest.standard.Links.MemoryLink;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractLayerManifest.java 447 2016-01-14 10:34:47Z mcgaerty $
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
		checkNotNull(layerTypeId);

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
		checkNotNull(baseLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest targetLayerManifest = new TargetLayerManifestImpl(baseLayerId);
		baseLayerManifests.add(targetLayerManifest);
		return targetLayerManifest;
	}

	@Override
	public void removeBaseLayerId(String baseLayerId) {
		checkNotLocked();

		removeBaseLayerId0(baseLayerId);
	}

	protected void removeBaseLayerId0(String baseLayerId) {
		checkNotNull(baseLayerId);

		checkAllowsTargetLayer();

		for(Iterator<TargetLayerManifest> it = baseLayerManifests.iterator(); it.hasNext();) {
			if(baseLayerId.equals(it.next().getLayerId())) {
				it.remove();
				return;
			}
		}

		throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "No base layer manifest defined for id: "+baseLayerId);
	}

	protected TargetLayerManifest lookupLocalBaseLayer(String id) {
		TargetLayerManifest result = null;

		for(TargetLayerManifest targetLayerManifest : baseLayerManifests) {
			if(id.equals(targetLayerManifest.getLayerId())) {
				result = targetLayerManifest;
				break;
			}
		}

		return result;
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

	protected class LocalLayerLink extends Link<LayerManifest> {

		/**
		 * @param lazyResolver
		 * @param id
		 */
		public LocalLayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.standard.Links.Link#resolve()
		 */
		@Override
		protected LayerManifest resolve() {
			TargetLayerManifest target = lookupLocalBaseLayer(getId());
			return target==null ? null : target.getResolvedLayerManifest();
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

		private GlobalLayerLink resolvedLayer;
		private PrerequisiteLink prerequisite;

		public TargetLayerManifestImpl(String targetId) {
			resolvedLayer = new GlobalLayerLink(targetId);
			prerequisite = new PrerequisiteLink(targetId);
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
