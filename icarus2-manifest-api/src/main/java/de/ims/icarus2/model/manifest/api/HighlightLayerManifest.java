/*
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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;

/**
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface HighlightLayerManifest extends LayerManifest<HighlightLayerManifest> {

	//FIXME finish specification

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#getManifestType()
	 */
	@Override
	default ManifestType getManifestType() {
		return ManifestType.HIGHLIGHT_LAYER_MANIFEST;
	}

	Optional<TargetLayerManifest> getPrimaryLayerManifest();

	boolean isLocalPrimaryLayerManifest();

	default boolean isHighlightFlagSet(HighlightFlag flag) {
		MutableBoolean result = new MutableBoolean(false);

		forEachActiveHighlightFlag(f -> {
			if(f==flag) {
				result.setBoolean(true);
			}
		});

		return result.booleanValue();
	}

	default boolean isLocalHighlightFlagSet(HighlightFlag flag) {
		MutableBoolean result = new MutableBoolean(false);

		forEachActiveLocalHighlightFlag(f -> {
			if(f==flag) {
				result.setBoolean(true);
			}
		});

		return result.booleanValue();
	}

	@AccessRestriction(AccessMode.READ)
	void forEachActiveHighlightFlag(Consumer<? super HighlightFlag> action);

	@AccessRestriction(AccessMode.READ)
	void forEachActiveLocalHighlightFlag(Consumer<? super HighlightFlag> action);

	default Set<HighlightFlag> getActiveHighlightFlags() {
		EnumSet<HighlightFlag> result = EnumSet.noneOf(HighlightFlag.class);

		forEachActiveHighlightFlag(result::add);

		return result;
	}

	default Set<HighlightFlag> getActiveLocalHighlightFlags() {
		EnumSet<HighlightFlag> result = EnumSet.noneOf(HighlightFlag.class);

		forEachActiveLocalHighlightFlag(result::add);

		return result;
	}

	// Modification methods

	default TargetLayerManifest setAndGetPrimaryLayer(String primaryLayerId) {
		return IcarusUtils.extractSupplied(action -> setPrimaryLayerId(primaryLayerId, action));
	}

	HighlightLayerManifest setPrimaryLayerId(String primaryLayerId,
			@Nullable Consumer<? super TargetLayerManifest> action);

	HighlightLayerManifest setHighlightFlag(HighlightFlag flag, boolean active);
}
