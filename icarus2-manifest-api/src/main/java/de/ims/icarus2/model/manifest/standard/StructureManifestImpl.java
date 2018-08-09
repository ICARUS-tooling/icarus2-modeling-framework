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

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;

/**
 * @author Markus Gärtner
 *
 */
public class StructureManifestImpl extends ContainerManifestImpl implements StructureManifest {

	private StructureType structureType;

	private EnumSet<StructureFlag> structureFlags;

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, StructureLayerManifest layerManifest) {
		super(manifestLocation, registry, layerManifest);

		structureFlags = EnumSet.noneOf(StructureFlag.class);
	}

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public StructureManifestImpl(StructureLayerManifest layerManifest) {
		this(layerManifest.getManifestLocation(), layerManifest.getRegistry(), layerManifest);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && structureFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.AbstractManifest#getTemplate()
	 */
	@Override
	public synchronized StructureManifest getTemplate() {
		return (StructureManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus2.model.api.standard.manifest.ContainerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StructureManifest#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		StructureType result = structureType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getStructureType();
		}

		if(result==null) {
			result = DEFAULT_STRUCTURE_TYPE;
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StructureManifest#isLocalStructureType()
	 */
	@Override
	public boolean isLocalStructureType() {
		return structureType!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.ContainerManifestImpl#getHost()
	 */
	@Override
	public StructureLayerManifest getHost() {
		return (StructureLayerManifest) super.getHost();
	}

	@Override
	public void setStructureType(StructureType structureType) {
		checkNotLocked();

		setStructureType0(structureType);
	}

	protected void setStructureType0(StructureType structureType) {
		requireNonNull(structureType);

		this.structureType = structureType;
	}

	@Override
	public boolean isStructureFlagSet(StructureFlag flag) {
		return structureFlags.contains(flag) || (hasTemplate() && getTemplate().isStructureFlagSet(flag));
	}

	@Override
	public void setStructureFlag(StructureFlag flag, boolean active) {
		checkNotLocked();

		setStructureFlag0(flag, active);
	}

	protected void setStructureFlag0(StructureFlag flag, boolean active) {
		requireNonNull(flag);

		if(active) {
			structureFlags.add(flag);
		} else {
			structureFlags.remove(flag);
		}
	}

	@Override
	public void forEachActiveStructureFlag(
			Consumer<? super StructureFlag> action) {
		if(hasTemplate()) {
			getTemplate().forEachActiveStructureFlag(action);
		}
		structureFlags.forEach(action);
	}

	@Override
	public void forEachActiveLocalStructureFlag(
			Consumer<? super StructureFlag> action) {
		structureFlags.forEach(action);
	}
}
