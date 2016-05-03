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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/HighlightLayerManifest.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;

/**
 * @author Markus Gärtner
 * @version $Id: HighlightLayerManifest.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface HighlightLayerManifest extends LayerManifest {

	//FIXME finish specification

	ItemLayerManifest getPrimaryLayerManifest();

	boolean isLocalPrimaryLayerManifest();

	boolean isHighlightFlagSet(HighlightFlag flag);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveHighlightFlag(Consumer<? super HighlightFlag> action);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveLocalHighlightFlag(Consumer<? super HighlightFlag> action);

	default Set<HighlightFlag> getActiveHighlightFlags() {
		EnumSet<HighlightFlag> result = EnumSet.noneOf(HighlightFlag.class);

		forEachActiveHighlightFlag(result::add);

		return result;
	}

	default Set<HighlightFlag> getActiveLocalHighlightFlags() {
		EnumSet<HighlightFlag> result = EnumSet.noneOf(HighlightFlag.class);

		forEachActiveLocalHighlightFlag(result::add);

		return result;
	}

	// Modification methods

	void setPrimaryLayerId(String primaryLayerId);

	void setHighlightFlag(HighlightFlag flag, boolean active);
}
