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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_consumeTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_createTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.mockItemLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_layerManifestId;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_targetLayerId;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.transform_genericOptValue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface HighlightLayerManifestTest extends LayerManifestTest<HighlightLayerManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.HIGHLIGHT_LAYER_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getPrimaryLayerManifest()}.
	 */
	@Test
	default void testGetPrimaryLayerManifest() {
		assertDerivativeOptGetter(settings(),
				"layer1",
				"layer2",
				NO_DEFAULT(),
				transform_genericOptValue(HighlightLayerManifest::getPrimaryLayerManifest, transform_targetLayerId()),
				inject_createTargetLayerManifest(HighlightLayerManifest::setAndGetPrimaryLayer));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isLocalPrimaryLayerManifest()}.
	 */
	@Test
	default void testIsLocalPrimaryLayerManifest() {
		assertDerivativeIsLocal(
				settings(),
				mockItemLayerManifest("layer1"),
				mockItemLayerManifest("layer2"),
				HighlightLayerManifest::isLocalPrimaryLayerManifest,
				TestUtils.inject_genericSetter(HighlightLayerManifest::setAndGetPrimaryLayer, transform_layerManifestId()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isHighlightFlagSet(de.ims.icarus2.model.manifest.api.HighlightFlag)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsHighlightFlagSet() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeFlagGetter(
							settings(),
							Boolean.FALSE,
							m -> m.isHighlightFlagSet(flag),
							(m, active) -> m.setHighlightFlag(flag, active.booleanValue()));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#isLocalHighlightFlagSet(de.ims.icarus2.model.manifest.api.HighlightFlag)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLocalHighlightFlagSet() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeLocalFlagGetter(
							settings(),
							Boolean.FALSE,
							m -> m.isLocalHighlightFlagSet(flag),
							(m, active) -> m.setHighlightFlag(flag, active.booleanValue()));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#forEachActiveHighlightFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveHighlightFlag() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					LayerManifestTest.super.<HighlightFlag>assertDerivativeForEach(
							settings(),
							flag, TestUtils.other(flag),
							HighlightLayerManifest::forEachActiveHighlightFlag,
							(m,f) -> m.setHighlightFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#forEachActiveLocalHighlightFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveLocalHighlightFlag() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					LayerManifestTest.super.<HighlightFlag>assertDerivativeForEachLocal(
							settings(),
							flag, TestUtils.other(flag),
							HighlightLayerManifest::forEachActiveLocalHighlightFlag,
							(m,f) -> m.setHighlightFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getActiveHighlightFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveHighlightFlags() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeGetter(
							settings(),
							flag, TestUtils.other(flag),
							HighlightLayerManifest::getActiveHighlightFlags,
							(m,f) -> m.setHighlightFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#getActiveLocalHighlightFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveLocalHighlightFlags() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeLocalGetter(
							settings(),
							flag, TestUtils.other(flag),
							HighlightLayerManifest::getActiveLocalHighlightFlags,
							(m,f) -> m.setHighlightFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#setAndGetPrimaryLayer(java.lang.String)}.
	 */
	@Test
	default void testSetAndGetPrimaryLayer() {
		assertLockableSetterBatch(settings(),
				HighlightLayerManifest::setAndGetPrimaryLayer,
				getLegalIdValues(), true,
				INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#setPrimaryLayerId(String, java.util.function.Consumer)}.
	 */
	@Test
	default void testSetPrimaryLayerId() {
		assertLockableSetterBatch(settings(),
				inject_consumeTargetLayerManifest(HighlightLayerManifest::setPrimaryLayerId),
				getLegalIdValues(), true,
				INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.HighlightLayerManifest#setHighlightFlag(de.ims.icarus2.model.manifest.api.HighlightFlag, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetHighlightFlag() {
		return Stream.of(HighlightFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertLockableSetter(settings(),
							(m, active) -> m.setHighlightFlag(flag, active.booleanValue()));
						}));
	}

}
