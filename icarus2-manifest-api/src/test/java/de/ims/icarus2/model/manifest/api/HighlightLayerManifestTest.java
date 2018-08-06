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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_genericSetter;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.mockItemLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_layerManifestId;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface HighlightLayerManifestTest<M extends HighlightLayerManifest> extends LayerManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getPrimaryLayerManifest()}.
	 */
	@Test
	default void testGetPrimaryLayerManifest() {
		assertDerivativeGetter(
				mockItemLayerManifest("layer1"),
				mockItemLayerManifest("layer2"), null,
				HighlightLayerManifest::getPrimaryLayerManifest,
				inject_genericSetter(HighlightLayerManifest::setPrimaryLayerId, transform_layerManifestId()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isLocalPrimaryLayerManifest()}.
	 */
	@Test
	default void testIsLocalPrimaryLayerManifest() {
		assertDerivativeIsLocal(
				mockItemLayerManifest("layer1"),
				mockItemLayerManifest("layer2"),
				HighlightLayerManifest::isLocalPrimaryLayerManifest,
				inject_genericSetter(HighlightLayerManifest::setPrimaryLayerId, transform_layerManifestId()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isHighlightFlagSet(de.ims.icarus2.model.manifest.api.HighlightFlag)}.
	 */
	@Test
	default void testIsHighlightFlagSet() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeFlagGetter(Boolean.FALSE,
					m -> m.isHighlightFlagSet(flag),
					(m, active) -> m.setHighlightFlag(flag, active));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isLocalHighlightFlagSet(de.ims.icarus2.model.manifest.api.HighlightFlag)}.
	 */
	@Test
	default void testIsLocalHighlightFlagSet() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeLocalFlagGetter(Boolean.FALSE,
					m -> m.isLocalHighlightFlagSet(flag),
					(m, active) -> m.setHighlightFlag(flag, active));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#forEachActiveHighlightFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveHighlightFlag() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeForEach(flag, TestUtils.other(flag),
					m -> m::forEachActiveHighlightFlag,
					(m,f) -> m.setHighlightFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#forEachActiveLocalHighlightFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveLocalHighlightFlag() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeForEachLocal(flag, TestUtils.other(flag),
					m -> m::forEachActiveLocalHighlightFlag,
					(m,f) -> m.setHighlightFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getActiveHighlightFlags()}.
	 */
	@Test
	default void testGetActiveHighlightFlags() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeAccumulativeGetter(flag, TestUtils.other(flag),
					HighlightLayerManifest::getActiveHighlightFlags,
					(m,f) -> m.setHighlightFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getActiveLocalHighlightFlags()}.
	 */
	@Test
	default void testGetActiveLocalHighlightFlags() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeAccumulativeLocalGetter(flag, TestUtils.other(flag),
					HighlightLayerManifest::getActiveLocalHighlightFlags,
					(m,f) -> m.setHighlightFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#setPrimaryLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetPrimaryLayerId() {
		assertLockableSetter(
				HighlightLayerManifest::setPrimaryLayerId,
				"layer1", true, ManifestTestUtils.getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#setHighlightFlag(de.ims.icarus2.model.manifest.api.HighlightFlag, boolean)}.
	 */
	@Test
	default void testSetHighlightFlag() {
		for(HighlightFlag flag : HighlightFlag.values()) {
			assertDerivativeAccumulativeGetter(flag, TestUtils.other(flag),
					HighlightLayerManifest::getActiveHighlightFlags,
					(m,f) -> m.setHighlightFlag(f, true));
		}
	}

}
