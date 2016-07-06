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
public interface StructureLayerManifest extends ItemLayerManifest {

	/**
	 * Returns the manifest for the members (structures) of the top-level
	 * container in this layer. This is effectively the same as calling
	 * {@link #getContainerManifest(int)} with a {@code level} value
	 * of {@code 1} and casting the result to {@code StructureManifest}.
	 * Note that a structure layer always contains a regular container
	 * as root of its container hierarchy. Only on the subsequent levels
	 * the structures themselves are hosted!
	 *
	 * @return
	 * @see #getRootContainerManifest()
	 * @see #getContextManifest()
	 */
	@AccessRestriction(AccessMode.READ)
	StructureManifest getRootStructureManifest();

	void addStructureManifest(StructureManifest manifest, int level);

	default void addStructureManifest(StructureManifest manifest) {
		addStructureManifest(manifest, -1);
	}

	void removeStructureManifest(StructureManifest manifest);
}
