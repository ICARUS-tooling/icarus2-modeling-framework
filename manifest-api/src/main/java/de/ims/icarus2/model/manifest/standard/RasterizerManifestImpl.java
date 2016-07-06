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
package de.ims.icarus2.model.manifest.standard;

import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;

/**
 * @author Markus Gärtner
 *
 */
public class RasterizerManifestImpl extends AbstractForeignImplementationManifest<RasterizerManifest> implements RasterizerManifest {

	private final FragmentLayerManifest layerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public RasterizerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);

		layerManifest = null;
	}

	public RasterizerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, FragmentLayerManifest layerManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerManifest, FragmentLayerManifest.class);

		this.layerManifest = layerManifest;
	}

	public RasterizerManifestImpl(FragmentLayerManifest layerManifest) {
		this(layerManifest.getManifestLocation(), layerManifest.getRegistry(), layerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.RasterizerManifest#getLayerManifest()
	 */
	@Override
	public FragmentLayerManifest getLayerManifest() {
		return layerManifest;
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
		return ManifestType.RASTERIZER_MANIFEST;
	}
}
