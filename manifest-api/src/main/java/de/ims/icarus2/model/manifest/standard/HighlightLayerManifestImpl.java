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

import de.ims.icarus2.model.manifest.api.HighlightFlag;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class HighlightLayerManifestImpl extends AbstractLayerManifest<HighlightLayerManifest> implements HighlightLayerManifest {

	private EnumSet<HighlightFlag> highlightFlags;
	private GlobalLayerLink primaryLayer;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public HighlightLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);

		highlightFlags = EnumSet.noneOf(HighlightFlag.class);
	}

	public HighlightLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public HighlightLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	@Override
	public boolean isHighlightFlagSet(HighlightFlag flag) {
		return highlightFlags.contains(flag) || (hasTemplate() && getTemplate().isHighlightFlagSet(flag));
	}

	@Override
	public boolean isLocalHighlightFlagSet(HighlightFlag flag) {
		return highlightFlags.contains(flag);
	}

	@Override
	public void setHighlightFlag(HighlightFlag flag, boolean active) {
		checkNotLocked();

		setHighlightFlag0(flag, active);
	}

	protected void setHighlightFlag0(HighlightFlag flag, boolean active) {
		checkNotNull(flag);

		if(active) {
			highlightFlags.add(flag);
		} else {
			highlightFlags.remove(flag);
		}
	}

	@Override
	public void forEachActiveHighlightFlag(Consumer<? super HighlightFlag> action) {
		if(hasTemplate()) {
			getTemplate().forEachActiveHighlightFlag(action);
		}

		forEachActiveLocalHighlightFlag(action);
	}

	@Override
	public void forEachActiveLocalHighlightFlag(Consumer<? super HighlightFlag> action) {
		highlightFlags.forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.HIGHLIGHT_LAYER_MANIFEST;
	}

	@Override
	public ItemLayerManifest getPrimaryLayerManifest() {
		LayerManifest result = null;

		if(primaryLayer!=null) {
			result = primaryLayer.get();
		}

		if(result==null && hasTemplate()) {
			result = getTemplate().getPrimaryLayerManifest();
		}

		return (ItemLayerManifest) result;
	}

	@Override
	public boolean isLocalPrimaryLayerManifest() {
		return primaryLayer!=null;
	}

	@Override
	public void setPrimaryLayerId(String primaryLayerId) {
		checkNotLocked();

		setPrimaryLayerId0(primaryLayerId);
	}

	protected void setPrimaryLayerId0(String primaryLayerId) {
		checkAllowsTargetLayer();
		checkNotNull(primaryLayerId);

		primaryLayer = new GlobalLayerLink(primaryLayerId);
	}
}
