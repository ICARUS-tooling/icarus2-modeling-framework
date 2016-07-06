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

import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;

/**
 * @author Markus Gärtner
 *
 */
public class StructureLayerManifestImpl extends ItemLayerManifestImpl implements StructureLayerManifest {

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public StructureLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public StructureLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public StructureLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.AbstractManifest#getTemplate()
	 */
	@Override
	public synchronized StructureLayerManifest getTemplate() {
		return (StructureLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus2.model.api.ItemLayerManifestImpl.manifest.MarkableLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StructureLayerManifest#getRootStructureManifest()
	 */
	@Override
	public StructureManifest getRootStructureManifest() {
		return (StructureManifest) getContainerManifest(1);
	}

	@Override
	public void addStructureManifest(StructureManifest manifest, int level) {
		checkNotLocked();

		addStructureManifest0(manifest, level);
	}

	protected void addStructureManifest0(StructureManifest manifest, int level) {
		if(level==0 || (level==-1 && getContainerDepth()==0))
			throw new IllegalStateException("Cannot add structure manifest as root container manifest"); //$NON-NLS-1$
		addContainerManifest0(manifest, level);
	}

	@Override
	public void removeStructureManifest(StructureManifest manifest) {
		removeContainerManifest(manifest);
	}
}
