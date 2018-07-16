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

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface HighlightLayerManifest extends LayerManifest {

	//FIXME finish specification

	ItemLayerManifest getPrimaryLayerManifest();

	boolean isLocalPrimaryLayerManifest();

	default boolean isHighlightFlagSet(HighlightFlag flag) {
		MutableBoolean result = new MutableBoolean(false);

		forEachActiveHighlightFlag(f -> {
			if(f==flag) {
				result.setBoolean(true);
			}
		});

		return result.booleanValue();
	}

	default boolean isLocalHighlightFlagSet(HighlightFlag flag) {
		MutableBoolean result = new MutableBoolean(false);

		forEachActiveLocalHighlightFlag(f -> {
			if(f==flag) {
				result.setBoolean(true);
			}
		});

		return result.booleanValue();
	}

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