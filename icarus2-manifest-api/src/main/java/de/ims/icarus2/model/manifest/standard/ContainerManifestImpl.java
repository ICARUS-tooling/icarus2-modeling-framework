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

import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerManifestImpl extends AbstractMemberManifest<ContainerManifest, ItemLayerManifest>
		implements ContainerManifest {

	private Optional<ContainerType> containerType = Optional.empty();

	private final EnumSet<ContainerFlag> containerFlags = EnumSet.noneOf(ContainerFlag.class);

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContainerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, ItemLayerManifest layerManifest) {
		super(manifestLocation, registry, layerManifest, ItemLayerManifest.class);
	}

	public ContainerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	public ContainerManifestImpl(ItemLayerManifest layerManifest) {
		super(layerManifest, hostIdentity(), ItemLayerManifest.class);
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && containerFlags.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return getWrappedDerivable(containerType, ContainerManifest::getContainerType)
				.orElse(DEFAULT_CONTAINER_TYPE);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContainerManifest#isLocalContainerType()
	 */
	@Override
	public boolean isLocalContainerType() {
		return containerType.isPresent();
	}

	@Override
	public ContainerManifest setContainerType(ContainerType containerType) {
		checkNotLocked();

		setContainerType0(containerType);

		return this;
	}

	protected void setContainerType0(ContainerType containerType) {
		this.containerType = Optional.of(containerType);
	}

	@Override
	public boolean isContainerFlagSet(ContainerFlag flag) {
		return containerFlags.contains(flag) || (hasTemplate() && getTemplate().isContainerFlagSet(flag));
	}

	@Override
	public ContainerManifest setContainerFlag(ContainerFlag flag, boolean active) {
		checkNotLocked();

		setContainerFlag0(flag, active);

		return this;
	}

	protected void setContainerFlag0(ContainerFlag flag, boolean active) {
		requireNonNull(flag);

		if(active) {
			containerFlags.add(flag);
		} else {
			containerFlags.remove(flag);
		}
	}

	@Override
	public void forEachActiveContainerFlag(
			Consumer<? super ContainerFlag> action) {
		if(hasTemplate()) {
			getTemplate().forEachActiveContainerFlag(action);
		}
		containerFlags.forEach(action);
	}

	@Override
	public void forEachActiveLocalContainerFlag(
			Consumer<? super ContainerFlag> action) {
		containerFlags.forEach(action);
	}
}
