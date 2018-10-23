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
import java.util.Optional;
import java.util.function.Consumer;

import de.ims.icarus2.model.manifest.api.HighlightFlag;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.standard.Links.Link;

/**
 * @author Markus Gärtner
 *
 */
public class HighlightLayerManifestImpl extends AbstractLayerManifest<HighlightLayerManifest> implements HighlightLayerManifest {

	private EnumSet<HighlightFlag> highlightFlags;
	private Link<LayerManifest> primaryLayer;

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
		super(manifestLocation, registry, null);
	}

	public HighlightLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		super(layerGroupManifest);
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
		requireNonNull(flag);

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
	public Optional<ItemLayerManifest> getPrimaryLayerManifest() {
		return getDerivable(
				Optional.ofNullable(primaryLayer).map(
						link -> (ItemLayerManifest) link.get()),
				HighlightLayerManifest::getPrimaryLayerManifest);
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
		requireNonNull(primaryLayerId);

		primaryLayer = createLayerLink(primaryLayerId, "primary layer");
	}
}
