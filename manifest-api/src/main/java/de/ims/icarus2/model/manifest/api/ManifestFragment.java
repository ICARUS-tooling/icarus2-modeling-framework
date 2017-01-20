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

import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * As the most simple variation of a manifest, a {@code ManifestFragment} only
 * has to declare a namespace wide unique {@link #getId() id}, a valid {@link #getManifestType() type}
 * and provide an implementation of the locking mechanism defined in {@link Lockable}.
 * <p>
 * Note that "namespace" usually refers to the entity a manifest is hosted in. This can either be
 * another manifest or the entirety of a {@link ManifestRegistry registry}.
 *
 * @author Markus Gärtner
 *
 */
public interface ManifestFragment extends Lockable, TypedManifest {

	/**
	 * Returns the namespace wide unique id of this manifest. Must not return {@code null}.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getId();

	/**
	 * Returns a globally unique identifier that is comprised of the locally unique id and
	 * the ids of the designated host environment if such exists. In the most simple case
	 * the globally unique id is equal to {@link #getId()}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default String getUniqueId() {
		return ManifestUtils.getUniqueId(this);
	}
}
