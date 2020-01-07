/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;


/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface FragmentLayerManifest extends ItemLayerManifestBase<FragmentLayerManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl#getManifestType()
	 */
	@Override
	default ManifestType getManifestType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * Links to the annotation layer that is used to fetch the annotation
	 * values to be rasterized.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<TargetLayerManifest> getValueLayerManifest();

	boolean isLocalValueLayerManifest();

	/**
	 * Returns the key to be used when fetching values for fragmentation.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getAnnotationKey();

	boolean isLocalAnnotationKey();

	@AccessRestriction(AccessMode.READ)
	Optional<RasterizerManifest> getRasterizerManifest();

	boolean isLocalRasterizerManifest();

	// Modification methods

	default TargetLayerManifest setAndGetValueLayer(String valueLayerId) {
		return IcarusUtils.extractSupplied(action -> setValueLayerId(valueLayerId, action));
	}

	FragmentLayerManifest setValueLayerId(String valueLayerId,
			@Nullable Consumer<? super TargetLayerManifest> action);

	FragmentLayerManifest setAnnotationKey(String key);

	FragmentLayerManifest setRasterizerManifest(RasterizerManifest rasterizerManifest);
}
