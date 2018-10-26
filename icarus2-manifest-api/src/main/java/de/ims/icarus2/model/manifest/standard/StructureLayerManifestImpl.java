/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

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
		super(manifestLocation, registry, null);
	}

	public StructureLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		super(layerGroupManifest);
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
	public Optional<StructureManifest> getRootStructureManifest() {
		Hierarchy<ContainerManifest> hierarchy = getContainerHierarchy().orElse(null);

		// Bail early if there's not enough data to even host a structure manifest
		if(hierarchy==null || hierarchy.getDepth()<2) {
			//TODO this also returns null when a structure was illegally set on root position in the hierarchy
			return Optional.empty();
		}

		// Find and return first structure manifest
		for(int level=1; level<hierarchy.getDepth(); level++) {
			ContainerManifest manifest = hierarchy.atLevel(level);
			if(manifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
				return Optional.of((StructureManifest) manifest);
			}
		}

		throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_MEMBER,
				"No root structure manifest defined for "+ManifestUtils.getName(this));
	}
}
