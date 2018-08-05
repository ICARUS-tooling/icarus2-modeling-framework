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

import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;

/**
 * @author Markus Gärtner
 *
 */
public class FragmentLayerManifestImpl extends ItemLayerManifestImpl implements FragmentLayerManifest {

	private TargetLayerManifest valueManifest;
	private String annotationKey;
	private RasterizerManifest rasterizerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public FragmentLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		this(layerGroupManifest.getContextManifest().getManifestLocation(),
				layerGroupManifest.getContextManifest().getRegistry(), layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && rasterizerManifest==null && valueManifest==null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#getTemplate()
	 */
	@Override
	public FragmentLayerManifest getTemplate() {
		return (FragmentLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getValueLayerManifest()
	 */
	@Override
	public TargetLayerManifest getValueLayerManifest() {
		TargetLayerManifest result = valueManifest;

		if(result==null && hasTemplate()) {
			result = getTemplate().getValueLayerManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalValueLayerManifest()
	 */
	@Override
	public boolean isLocalValueLayerManifest() {
		return valueManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getAnnotationKey()
	 */
	@Override
	public String getAnnotationKey() {
		String result = annotationKey;
		if(result==null && hasTemplate()) {
			result = getTemplate().getAnnotationKey();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalAnnotationKey()
	 */
	@Override
	public boolean isLocalAnnotationKey() {
		return annotationKey!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getRasterizerManifest()
	 */
	@Override
	public RasterizerManifest getRasterizerManifest() {
		RasterizerManifest result = rasterizerManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getRasterizerManifest();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalRasterizerManifest()
	 */
	@Override
	public boolean isLocalRasterizerManifest() {
		return rasterizerManifest!=null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setValueLayerManifest(de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest)
	 */
	@Override
	public TargetLayerManifest setValueLayerId(String valueLayerId) {
		checkNotLocked();

		return setValueLayerId0(valueLayerId);
	}

	protected TargetLayerManifest setValueLayerId0(String valueLayerId) {
		requireNonNull(valueLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest manifest = createTargetLayerManifest(valueLayerId);
		valueManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setAnnotationKey(java.lang.String)
	 */
	@Override
	public void setAnnotationKey(String key) {
		checkNotLocked();

		setAnnotationKey0(key);
	}

	protected void setAnnotationKey0(String key) {
		requireNonNull(key);

		annotationKey = key;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setRasterizerManifest(de.ims.icarus2.model.manifest.api.RasterizerManifest)
	 */
	@Override
	public void setRasterizerManifest(RasterizerManifest rasterizerManifest) {
		checkNotLocked();

		setRasterizerManifest0(rasterizerManifest);
	}

	protected void setRasterizerManifest0(RasterizerManifest rasterizerManifest) {
		requireNonNull(rasterizerManifest);

		this.rasterizerManifest = rasterizerManifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	public void lock() {
		super.lock();

		if(rasterizerManifest!=null) {
			rasterizerManifest.lock();
		}
	}

}
