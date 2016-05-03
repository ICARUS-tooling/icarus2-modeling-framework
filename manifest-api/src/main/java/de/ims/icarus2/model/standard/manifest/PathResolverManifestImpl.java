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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/PathResolverManifestImpl.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.manifest;

import de.ims.icarus2.model.api.manifest.ImplementationManifest;
import de.ims.icarus2.model.api.manifest.LocationManifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.ManifestRegistry;
import de.ims.icarus2.model.api.manifest.ManifestType;
import de.ims.icarus2.model.api.manifest.PathResolverManifest;

/**
 * @author Markus Gärtner
 * @version $Id: PathResolverManifestImpl.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public class PathResolverManifestImpl extends AbstractForeignImplementationManifest<PathResolverManifest> implements PathResolverManifest {

	private final LocationManifest locationManifest;

	public PathResolverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public PathResolverManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LocationManifest locationManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, locationManifest, LocationManifest.class);

		this.locationManifest = locationManifest;
	}

	public PathResolverManifestImpl(LocationManifest locationManifest) {
		this(locationManifest.getManifestLocation(), locationManifest.getRegistry(), locationManifest);
	}

	@Override
	public LocationManifest getLocationManifest() {
		return locationManifest;
	}

	/**
	 * @see de.ims.icarus2.model.standard.manifest.AbstractForeignImplementationManifest#getImplementationManifest()
	 */
	@Override
	public ImplementationManifest getImplementationManifest() {
		ImplementationManifest result = super.getImplementationManifest();
		if(result==null && hasTemplate()) {
			result = getTemplate().getImplementationManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.PATH_RESOLVER_MANIFEST;
	}
}
