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
package de.ims.icarus2.model.manifest.xml.delegates;

import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.manifest.standard.PathResolverManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PathResolverManifestXmlDelegate extends AbstractForeignImplementationManifestXmlDelegate<PathResolverManifest> {

	public PathResolverManifestXmlDelegate() {
		// no-op
	}

	public PathResolverManifestXmlDelegate(PathResolverManifest manifest) {
		setInstance(manifest);
	}

	public PathResolverManifestXmlDelegate(LocationManifest locationManifest) {
		setInstance(new PathResolverManifestImpl(locationManifest));
	}

	public PathResolverManifestXmlDelegate reset(LocationManifest locationManifest) {
		reset();
		setInstance(new PathResolverManifestImpl(locationManifest));

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_PATH_RESOLVER;
	}
}
