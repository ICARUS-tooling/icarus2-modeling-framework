/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class FragmentLayerManifestImpl extends AbstractItemLayerManifestBase<FragmentLayerManifest>
		implements FragmentLayerManifest {

	private TargetLayerManifest valueManifest;
	private Optional<String> annotationKey = Optional.empty();
	private Optional<RasterizerManifest> rasterizerManifest = Optional.empty();

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry, @Nullable LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			ManifestRegistry registry) {
		super(manifestLocation, registry, null);
	}

	public FragmentLayerManifestImpl(LayerGroupManifest layerGroupManifest) {
		super(layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && !rasterizerManifest.isPresent() && valueManifest==null;
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
	public Optional<TargetLayerManifest> getValueLayerManifest() {
		return getDerivable(
				Optional.ofNullable(valueManifest),
				FragmentLayerManifest::getValueLayerManifest);
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
	public Optional<String> getAnnotationKey() {
		return getDerivable(annotationKey, FragmentLayerManifest::getAnnotationKey);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalAnnotationKey()
	 */
	@Override
	public boolean isLocalAnnotationKey() {
		return annotationKey.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#getRasterizerManifest()
	 */
	@Override
	public Optional<RasterizerManifest> getRasterizerManifest() {
		return getDerivable(rasterizerManifest, FragmentLayerManifest::getRasterizerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#isLocalRasterizerManifest()
	 */
	@Override
	public boolean isLocalRasterizerManifest() {
		return rasterizerManifest.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setValueLayerId(java.lang.String, java.util.function.Consumer)
	 */
	@Override
	public FragmentLayerManifest setValueLayerId(String valueLayerId, Consumer<? super TargetLayerManifest> action) {
		checkNotLocked();
		IcarusUtils.consumeIfAble(setValueLayerId0(valueLayerId), action);
		return this;
	}

	protected TargetLayerManifest setValueLayerId0(String valueLayerId) {
		requireNonNull(valueLayerId);

		checkAllowsTargetLayer();
		TargetLayerManifest manifest = createTargetLayerManifest(valueLayerId, "value layer");
		valueManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setAnnotationKey(java.lang.String)
	 */
	@Override
	public FragmentLayerManifest setAnnotationKey(String key) {
		checkNotLocked();

		setAnnotationKey0(key);

		return this;
	}

	protected void setAnnotationKey0(String key) {
		requireNonNull(key);

		if(key.isEmpty())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Annotation key must not be empty");

		annotationKey = Optional.of(key);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.FragmentLayerManifest#setRasterizerManifest(de.ims.icarus2.model.manifest.api.RasterizerManifest)
	 */
	@Override
	public FragmentLayerManifest setRasterizerManifest(RasterizerManifest rasterizerManifest) {
		checkNotLocked();

		setRasterizerManifest0(rasterizerManifest);

		return this;
	}

	protected void setRasterizerManifest0(RasterizerManifest rasterizerManifest) {
		this.rasterizerManifest = Optional.of(rasterizerManifest);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#lock()
	 */
	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(rasterizerManifest);
	}

}
