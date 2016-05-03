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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.manifest;

import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessRestriction;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TypedManifest {

	/**
	 * Returns the {@code type} of this manifest, i.e. that is
	 * what kind of member in a corpus it describes. If type-specific
	 * behavior is modeled, one should always use this method rather than
	 * doing multiple {@code instanceof} checks.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ManifestType getManifestType();
}
