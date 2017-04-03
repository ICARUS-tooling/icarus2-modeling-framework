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
 */
package de.ims.icarus2.model.manifest.api.binding;

import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.classes.ClassUtils;

/**
 * Models a requirement an entity can publish to let others know what kinds of layers
 * it is able to consume or requires to work properly.
 *
 * @author Markus Gärtner
 *
 */
public interface LayerPrerequisite {

	/**
	 * Returns the id of the target layer or {@code null} if an exact id match
	 * is not required or the prerequisite has not yet been fully resolved.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getLayerId();

	/**
	 * Returns the id of the context which should be used to resolve the required
	 * layer (specified by the {@link #getLayerId() method}) or {@code null} if no
	 * exact match is required or the prerequisite has not yet been fully resolved.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getContextId();

	/**
	 * If this layer only requires <i>some</i> layer of a certain type to be present
	 * this method provides the mechanics to tell this. When the returned value is
	 * {@code non-null} it is considered to be the exact name of a previously
	 * defined layer type.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getTypeId();

	/**
	 * Returns the id the required layer should be assigned once resolved. This links
	 * the result of an abstract prerequisite declaration to a boundary or base definition
	 * in a template. In addition a prerequisite's alias serves as its identifier. Therefore
	 * an alias must be unique within the same context!
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getAlias();

	/**
	 * Returns a brief description of this prerequisite, usable as a hint for user interfaces
	 * when asking the user to resolve ambiguous references.
	 * <p>
	 * This is an optional method.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getDescription();

	/**
	 * Returns the number of allowed bindings assigned to this prerequisite.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Multiplicity getMultiplicity();


	public static boolean defaultEquals(LayerPrerequisite p1, LayerPrerequisite p2) {
		return ClassUtils.equals(p1.getAlias(), p2.getAlias())
				&& ClassUtils.equals(p1.getContextId(), p2.getContextId())
				&& ClassUtils.equals(p1.getLayerId(), p2.getLayerId())
				&& ClassUtils.equals(p1.getTypeId(), p2.getTypeId())
				&& ClassUtils.equals(p1.getDescription(), p2.getDescription());

	}
}
