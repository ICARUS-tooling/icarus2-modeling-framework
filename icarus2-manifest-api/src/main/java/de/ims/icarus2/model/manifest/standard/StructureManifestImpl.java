/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;

/**
 * @author Markus Gärtner
 *
 */
public class StructureManifestImpl extends AbstractContainerManifestBase<StructureManifest, StructureLayerManifest>
		implements StructureManifest {

	private Optional<StructureType> structureType = Optional.empty();

	private final EnumSet<StructureFlag> structureFlags = EnumSet.noneOf(StructureFlag.class);

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, @Nullable StructureLayerManifest layerManifest) {
		super(manifestLocation, registry, layerManifest, StructureLayerManifest.class);
	}

	public StructureManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public StructureManifestImpl(StructureLayerManifest layerManifest) {
		super(layerManifest, StructureLayerManifest.class);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && structureFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StructureManifest#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return getWrappedDerivable(structureType, StructureManifest::getStructureType)
				.orElse(DEFAULT_STRUCTURE_TYPE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.StructureManifest#isLocalStructureType()
	 */
	@Override
	public boolean isLocalStructureType() {
		return structureType.isPresent();
	}

	@Override
	public StructureManifest setStructureType(StructureType structureType) {
		checkNotLocked();

		setStructureType0(structureType);

		return this;
	}

	protected void setStructureType0(StructureType structureType) {
		this.structureType = Optional.of(structureType);
	}

	@Override
	public boolean isStructureFlagSet(StructureFlag flag) {
		requireNonNull(flag);
		return structureFlags.contains(flag) || (hasTemplate() && getTemplate().isStructureFlagSet(flag));
	}

	@Override
	public StructureManifest setStructureFlag(StructureFlag flag, boolean active) {
		checkNotLocked();

		setStructureFlag0(flag, active);

		return this;
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
