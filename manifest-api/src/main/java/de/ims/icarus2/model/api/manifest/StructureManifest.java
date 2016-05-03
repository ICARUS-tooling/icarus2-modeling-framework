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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/StructureManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.manifest;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;

/**
 * @author Markus Gärtner
 * @version $Id: StructureManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface StructureManifest extends ContainerManifest {

	/**
	 * Returns the type of this structure
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	StructureType getStructureType();

	boolean isLocalStructureType();

	/**
	 * Checks whether the given {@code flag} is set for this manifest.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isStructureFlagSet(StructureFlag flag);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveStructureFlag(Consumer<? super StructureFlag> action);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveLocalStructureFlag(Consumer<? super StructureFlag> action);

	default Set<StructureFlag> getActiveStructureFlags() {
		EnumSet<StructureFlag> result = EnumSet.noneOf(StructureFlag.class);

		forEachActiveStructureFlag(result::add);

		return result;
	}

	default Set<StructureFlag> getActiveLocalStructureFlags() {
		EnumSet<StructureFlag> result = EnumSet.noneOf(StructureFlag.class);

		forEachActiveLocalStructureFlag(result::add);

		return result;
	}

	// Modification methods

	void setStructureType(StructureType structureType);

	void setStructureFlag(StructureFlag flag, boolean active);
}
