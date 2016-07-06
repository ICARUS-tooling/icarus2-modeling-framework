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

import static de.ims.icarus2.util.Conditions.checkNotNull;

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
		checkNotNull(containerType);

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
		checkNotNull(flag);

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
