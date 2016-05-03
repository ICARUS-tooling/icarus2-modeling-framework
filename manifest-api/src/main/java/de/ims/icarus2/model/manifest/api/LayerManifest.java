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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/LayerManifest.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;
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
 * being thrown during parsing or at verification time.
 *
 * @author Markus Gärtner
 * @version $Id: LayerManifest.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LayerManifest extends MemberManifest {

	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest();

	/**
	 * Returns the group manifest this layer is a part of.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	LayerGroupManifest getGroupManifest();


	@Override
	default ManifestFragment getHost() {
		return getGroupManifest();
	}

	/**
	 * Returns the optional layer type that acts as another abstraction layer
	 * to unify layers that share a common content structure. Note that all
	 * layer type instances are globally unique and are shared between all the
	 * layers of that type.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	LayerType getLayerType();

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

	@AccessRestriction(AccessMode.READ)
	default List<TargetLayerManifest> getLocalBaseLayerManifests() {
		LazyCollection<TargetLayerManifest> result = LazyCollection.lazyList();

		forEachLocalBaseLayerManifest(result);

		return result.getAsList();
	}

	// Modification methods

	void setLayerTypeId(String layerTypeId);

	TargetLayerManifest addBaseLayerId(String baseLayerId);

	void removeBaseLayerId(String baseLayerId);

	/**
	 * Models a resolved dependency on the layer level. A target layer may either be
	 * a local layer, hosted within the same context, or a foreign layer that has been
	 * resolved by means of binding a prerequisite manifest declaration to a layer manifest.
	 *
	 * @author Markus Gärtner
	 * @version $Id: LayerManifest.java 447 2016-01-14 10:34:47Z mcgaerty $
	 *
	 */
	public interface TargetLayerManifest extends Embedded {

		/**
		 * Returns the local {@code id} of the target layer.
		 * @return
		 */
		String getLayerId();

		/**
		 * Returns the source layer manifest for the dependency this manifest describes
		 * (that is the layer actually hosting a {@code TargetLayerManifest}).
		 *
		 * @return
		 */
		LayerManifest getLayerManifest();

		@Override
		default public ManifestFragment getHost() {
			return getLayerManifest();
		};

		/**
		 * When the target layer resides in a foreign context and was resolved using
		 * a prerequisite manifest, this method returns the used prerequisite. In the
		 * case of a local layer being targeted, the return value is {@code null}.
		 * @return
		 */
		ContextManifest.PrerequisiteManifest getPrerequisite();

		/**
		 * Returns the actual target layer manifest this manifest refers to. Note that the
		 * return type is chosen to be the general {@link LayerManifest} class instead of the
		 * {@link ItemLayerManifest} usually used for base or boundary layer declarations.
		 * This is so that {@link FragmentLayerManifest}s or {@link HighlightLayerManifest}s
		 * do not have to declare another linking manifest to account for their respective layer
		 * dependencies. The actually required type of layer should be concluded from the context
		 * in which the target layer is to be resolved.
		 *
		 * @return
		 */
		LayerManifest getResolvedLayerManifest();
	}
}
