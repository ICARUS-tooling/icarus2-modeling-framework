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
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;


/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface FragmentLayerManifest extends ItemLayerManifest {

	/**
	 * Links to the annotation layer that is used to fetch the annotation
	 * values to be rasterized.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	TargetLayerManifest getValueLayerManifest();

	boolean isLocalValueLayerManifest();

	/**
	 * Returns the key to be used when fetching values for fragmentation.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getAnnotationKey();

	boolean isLocalAnnotationKey();

	@AccessRestriction(AccessMode.READ)
	RasterizerManifest getRasterizerManifest();

	boolean isLocalRasterizerManifest();

	// Modification methods

	TargetLayerManifest setValueLayerId(String valueLayerId);

	void setAnnotationKey(String key);

	void setRasterizerManifest(RasterizerManifest rasterizerManifest);
}
