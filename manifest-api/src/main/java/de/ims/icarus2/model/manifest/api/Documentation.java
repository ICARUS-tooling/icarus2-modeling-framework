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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/Documentation.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * @author Markus Gärtner
 * @version $Id: Documentation.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Documentation extends ModifiableIdentity, Lockable, TypedManifest {

	@AccessRestriction(AccessMode.READ)
	String getContent();

	@AccessRestriction(AccessMode.READ)
	default List<Resource> getResources() {
		LazyCollection<Resource> result = LazyCollection.lazyList();

		forEachResource(result);

		return result.getAsList();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifest#getManifestType()
	 */
	@Override
	default public ManifestType getManifestType() {
		return ManifestType.DOCUMENTATION;
	}

	void forEachResource(Consumer<? super Resource> action);

	// Modification methods

	void setContent(String content);

	void addResource(Resource resource);

	void removeResource(Resource resource);

	@AccessControl(AccessPolicy.DENY)
	public interface Resource extends ModifiableIdentity, Lockable {

		@AccessRestriction(AccessMode.READ)
		URI getUri();

		void setUri(URI uri);
	}
}
