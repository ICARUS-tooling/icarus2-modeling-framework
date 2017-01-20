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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ItemLayerManifestImpl extends AbstractLayerManifest<ItemLayerManifest> implements ItemLayerManifest {

	private final List<ContainerManifest> containerManifests = new ArrayList<>();

	private TargetLayerManifest boundaryLayerManifest;
	private TargetLayerManifest foundationLayerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public ItemLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public ItemLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public ItemLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && boundaryLayerManifest==null && containerManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ITEM_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getContainerDepth()
	 */
	@Override
	public int getContainerDepth() {
		int depth = containerManifests.size();
		if(depth==0 && hasTemplate()) {
			depth = getTemplate().getContainerDepth();
		}
		return depth;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#hasLocalContainers()
	 */
	@Override
	public boolean hasLocalContainers() {
		return !containerManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getContainerManifest(int)
	 */
	@Override
	public ContainerManifest getContainerManifest(int level) {
		ContainerManifest result = null;

		if(!containerManifests.isEmpty()) {
			result = containerManifests.get(level);
		} else if(hasTemplate()) {
			result = getTemplate().getContainerManifest(level);
		}

		if(result==null)
			throw new IndexOutOfBoundsException("No container manifest available for level: "+level); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#indexOfContainerManifest(de.ims.icarus2.model.manifest.api.ContainerManifest)
	 */
	@Override
	public int indexOfContainerManifest(ContainerManifest containerManifest) {
		requireNonNull(containerManifest);

		int index = containerManifests.indexOf(containerManifest);

		if(index==-1 && containerManifests.isEmpty() && hasTemplate()) {
			index = getTemplate().indexOfContainerManifest(containerManifest);
		}

		return index;
	}

	@Override
	public void addContainerManifest(ContainerManifest containerManifest, int level) {
		checkNotLocked();

		addContainerManifest0(containerManifest, level);
	}

	protected void addContainerManifest0(ContainerManifest containerManifest, int level) {
		requireNonNull(containerManifest);

		if(level==-1) {
			level = containerManifests.size();
		}

		containerManifests.add(level, containerManifest);
	}

	@Override
	public void removeContainerManifest(ContainerManifest containerManifest) {
		checkNotLocked();

		removeContainerManifest0(containerManifest);
	}

	protected void removeContainerManifest0(ContainerManifest containerManifest) {
		requireNonNull(containerManifest);

		if(!containerManifests.remove(containerManifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Unknown container manifest: "+ManifestUtils.getName(containerManifest));
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public TargetLayerManifest getBoundaryLayerManifest() {
		TargetLayerManifest result = boundaryLayerManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getBoundaryLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalBoundaryLayerManifest()
	 */
	@Override
	public boolean isLocalBoundaryLayerManifest() {
		return boundaryLayerManifest!=null;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	@Override
	public TargetLayerManifest setBoundaryLayerId(String boundaryLayerId) {
		checkNotLocked();

		return setBoundaryLayerId0(boundaryLayerId);
	}

	protected TargetLayerManifest setBoundaryLayerId0(String boundaryLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(boundaryLayerId);

		TargetLayerManifest manifest = new TargetLayerManifestImpl(boundaryLayerId);
		boundaryLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#getFoundationLayerManifest()
	 */
	@Override
	public TargetLayerManifest getFoundationLayerManifest() {
		TargetLayerManifest result = foundationLayerManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getFoundationLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ItemLayerManifest#isLocalFoundationLayerManifest()
	 */
	@Override
	public boolean isLocalFoundationLayerManifest() {
		return foundationLayerManifest!=null;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	@Override
	public TargetLayerManifest setFoundationLayerId(String foundationLayerId) {
		checkNotLocked();

		return setFoundationLayerId0(foundationLayerId);
	}

	protected TargetLayerManifest setFoundationLayerId0(String foundationLayerId) {
		checkAllowsTargetLayer();
		requireNonNull(foundationLayerId);

		TargetLayerManifest manifest = new TargetLayerManifestImpl(foundationLayerId);
		foundationLayerManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		for(ContainerManifest containerManifest : containerManifests) {
			containerManifest.lock();
		}
	}
}
