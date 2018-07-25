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
public class ContainerManifestImpl extends AbstractMemberManifest<ContainerManifest> implements ContainerManifest {

//	private ContainerManifest parentManifest;
	private final ItemLayerManifest layerManifest;

//	private ContainerManifest elementManifest;
	private ContainerType containerType;

	private EnumSet<ContainerFlag> containerFlags;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContainerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, ItemLayerManifest layerManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerManifest, ItemLayerManifest.class);

		this.layerManifest = layerManifest;

		containerFlags = EnumSet.noneOf(ContainerFlag.class);
	}

	public ContainerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public ContainerManifestImpl(ItemLayerManifest layerManifest) {
		this(layerManifest.getManifestLocation(), layerManifest.getRegistry(), layerManifest);
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
	 * @see de.ims.icarus2.model.manifest.api.ContainerManifest#getLayerManifest()
	 */
	@Override
	public ItemLayerManifest getLayerManifest() {
		return layerManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		ContainerType result = containerType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getContainerType();
		}

		if(result==null) {
			result = ContainerType.LIST;
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ContainerManifest#isLocalContainerType()
	 */
	@Override
	public boolean isLocalContainerType() {
		return containerType!=null;
	}

	@Override
	public void setContainerType(ContainerType containerType) {
		checkNotLocked();

		setContainerType0(containerType);
	}

	protected void setContainerType0(ContainerType containerType) {
		requireNonNull(containerType);

		this.containerType = containerType;
	}

	@Override
	public boolean isContainerFlagSet(ContainerFlag flag) {
		return containerFlags.contains(flag) || (hasTemplate() && getTemplate().isContainerFlagSet(flag));
	}

	@Override
	public void setContainerFlag(ContainerFlag flag, boolean active) {
		checkNotLocked();

		setContainerFlag0(flag, active);
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
