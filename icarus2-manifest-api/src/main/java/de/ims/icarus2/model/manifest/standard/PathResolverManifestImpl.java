/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;

/**
 * @author Markus Gärtner
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
	 * @see de.ims.icarus2.model.manifest.standard.AbstractForeignImplementationManifest#getImplementationManifest()
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
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.PATH_RESOLVER_MANIFEST;
	}
}
