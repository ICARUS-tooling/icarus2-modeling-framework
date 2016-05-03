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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/LayerGroupManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.List;
import java.util.function.Consumer;

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
 * @version $Id: LayerGroupManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LayerGroupManifest extends ModifiableIdentity, ManifestFragment {

	public static final boolean DEFAULT_INDEPENDENT_VALUE = false;

	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest();

	@AccessRestriction(AccessMode.READ)
	int layerCount();

	@AccessRestriction(AccessMode.READ)
	void forEachLayerManifest(Consumer<? super LayerManifest> action);

	/**
	 * Returns the list of manifests that describe the layers in this group.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<LayerManifest> getLayerManifests() {
		LazyCollection<LayerManifest> result = LazyCollection.lazyList();

		forEachLayerManifest(result);

		return result.getAsList();
	}

	@AccessRestriction(AccessMode.READ)
	ItemLayerManifest getPrimaryLayerManifest();

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
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	LayerManifest getLayerManifest(String id);

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

	void addLayerManifest(LayerManifest layerManifest);

	void removeLayerManifest(LayerManifest layerManifest);

	void setPrimaryLayerId(String primaryLayerId);

	void setIndependent(Boolean isIndependent);

	@Override
	void setName(String name);
}
