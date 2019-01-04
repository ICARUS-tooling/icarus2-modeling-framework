/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_consumeTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_createTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.inject_forEachTargetLayerManifest;
import static de.ims.icarus2.model.manifest.api.LayerManifestTest.transform_targetLayerId;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationLayerManifestTest<M extends AnnotationLayerManifest> extends LayerManifestTest<M> {

	public static AnnotationManifest mockAnnotationManifest(String key) {
		assertNotNull(key);
		AnnotationManifest manifest = mock(AnnotationManifest.class);
		when(manifest.getKey()).thenReturn(Optional.of(key));
		return manifest;
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link AnnotationManifest} into a {@link String} by using
	 * its {@link AnnotationManifest#getKey() key}.
	 */
	public static <I extends AnnotationManifest> Function<I, String> transform_key(){
		return i -> i.getKey().orElseThrow(AssertionError::new);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachAnnotationManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachAnnotationManifest() {
		LayerManifestTest.super.<AnnotationManifest>assertDerivativeForEach(settings(),
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"),
				AnnotationLayerManifest::forEachAnnotationManifest,
				AnnotationLayerManifest::addAnnotationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachLocalAnnotationManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalAnnotationManifest() {
		LayerManifestTest.super.<AnnotationManifest>assertDerivativeForEachLocal(settings(),
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"),
				AnnotationLayerManifest::forEachLocalAnnotationManifest,
				AnnotationLayerManifest::addAnnotationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getAvailableKeys()}.
	 */
	@Test
	default void testGetAvailableKeys() {
		assertDerivativeAccumulativeGetter(settings(),
				"key1", "key2",
				AnnotationLayerManifest::getAvailableKeys,
				TestUtils.inject_genericSetter(AnnotationLayerManifest::addAnnotationManifest,
						AnnotationLayerManifestTest::mockAnnotationManifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getLocalAvailableKeys()}.
	 */
	@Test
	default void testGetLocalAvailableKeys() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				"key1", "key2",
				AnnotationLayerManifest::getLocalAvailableKeys,
				TestUtils.inject_genericSetter(AnnotationLayerManifest::addAnnotationManifest,
						AnnotationLayerManifestTest::mockAnnotationManifest));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)}.
	 */
	@Test
	default void testGetAnnotationManifest() {
		assertDerivativeAccumulativeOptLookup(
				settings(),
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"),
				AnnotationLayerManifest::getAnnotationManifest,
				true,
				AnnotationLayerManifest::addAnnotationManifest,
				transform_key(),
				"unknownkey1");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getAnnotationManifests()}.
	 */
	@Test
	default void testGetAnnotationManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"),
				AnnotationLayerManifest::getAnnotationManifests,
				AnnotationLayerManifest::addAnnotationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getLocalAnnotationManifests()}.
	 */
	@Test
	default void testGetLocalAnnotationManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"),
				AnnotationLayerManifest::getLocalAnnotationManifests,
				AnnotationLayerManifest::addAnnotationManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getDefaultKey()}.
	 */
	@Test
	default void testGetDefaultKey() {
		assertDerivativeOptGetter(settings(), "key1", "key2", null,
				AnnotationLayerManifest::getDefaultKey,
				AnnotationLayerManifest::setDefaultKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#isLocalDefaultKey()}.
	 */
	@Test
	default void testIsLocalDefaultKey() {
		assertDerivativeIsLocal(settings(), "key1", "key2",
				AnnotationLayerManifest::isLocalDefaultKey,
				AnnotationLayerManifest::setDefaultKey);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#isAnnotationFlagSet(de.ims.icarus2.model.manifest.api.AnnotationFlag)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testIsAnnotationFlagSet() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeFlagGetter(
							settings(),
							Boolean.FALSE,
							m -> m.isAnnotationFlagSet(flag),
							(m, active) -> m.setAnnotationFlag(flag, active));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#isLocalAnnotationFlagSet(de.ims.icarus2.model.manifest.api.AnnotationFlag)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testIsLocalAnnotationFlagSet() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeLocalFlagGetter(
							settings(),
							Boolean.FALSE,
							m -> m.isLocalAnnotationFlagSet(flag),
							(m, active) -> m.setAnnotationFlag(flag, active));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachActiveAnnotationFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveAnnotationFlag() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					LayerManifestTest.super.<AnnotationFlag>assertDerivativeForEach(
							settings(),
							flag, TestUtils.other(flag),
							AnnotationLayerManifest::forEachActiveAnnotationFlag,
							(m,f) -> m.setAnnotationFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachActiveLocalAnnotationFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveLocalAnnotationFlag() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					LayerManifestTest.super.<AnnotationFlag>assertDerivativeForEachLocal(
							settings(),
							flag, TestUtils.other(flag),
							AnnotationLayerManifest::forEachActiveLocalAnnotationFlag,
							(m,f) -> m.setAnnotationFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getActiveAnnotationFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveAnnotationFlags() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeGetter(
							settings(),
							flag, TestUtils.other(flag),
							AnnotationLayerManifest::getActiveAnnotationFlags,
							(m,f) -> m.setAnnotationFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getLocalActiveAnnotationFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetLocalActiveAnnotationFlags() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeLocalGetter(
							settings(),
							flag, TestUtils.other(flag),
							AnnotationLayerManifest::getLocalActiveAnnotationFlags,
							(m,f) -> m.setAnnotationFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachReferenceLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachReferenceLayerManifest() {
		assertDerivativeForEach(settings(),
				"layer1", "layer2",
				inject_forEachTargetLayerManifest(AnnotationLayerManifest::forEachReferenceLayerManifest),
				inject_createTargetLayerManifest(AnnotationLayerManifest::addAndGetReferenceLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#forEachLocalReferenceLayerManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalReferenceLayerManifest() {
		assertDerivativeForEachLocal(settings(),
				"layer1", "layer2",
				inject_forEachTargetLayerManifest(AnnotationLayerManifest::forEachLocalReferenceLayerManifest),
				inject_createTargetLayerManifest(AnnotationLayerManifest::addAndGetReferenceLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getReferenceLayerManifests()}.
	 */
	@Test
	default void testGetReferenceLayerManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				"layer1", "layer2",
				TestUtils.transform_genericCollectionGetter(AnnotationLayerManifest::getReferenceLayerManifests, transform_targetLayerId()),
				inject_createTargetLayerManifest(AnnotationLayerManifest::addAndGetReferenceLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#getLocalReferenceLayerManifests()}.
	 */
	@Test
	default void testGetLocalReferenceLayerManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				"layer1", "layer2",
				TestUtils.transform_genericCollectionGetter(
						AnnotationLayerManifest::getLocalReferenceLayerManifests,
						transform_targetLayerId()),
				inject_createTargetLayerManifest(
						AnnotationLayerManifest::addAndGetReferenceLayerId));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#setDefaultKey(java.lang.String)}.
	 */
	@Test
	default void testSetDefaultKey() {
		assertLockableSetter(settings(),AnnotationLayerManifest::setDefaultKey, "key1", true, TestUtils.NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#addAnnotationManifest(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
	 */
	@Test
	default void testAddAnnotationManifest() {
		assertLockableAccumulativeAdd(settings(),
				AnnotationLayerManifest::addAnnotationManifest, TestUtils.NO_ILLEGAL(),
				TestUtils.NO_CHECK, true, DUPLICATE_ID_CHECK,
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"), mockAnnotationManifest("key3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#removeAnnotationManifest(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
	 */
	@Test
	default void testRemoveAnnotationManifest() {
		assertLockableAccumulativeRemove(settings(),
				AnnotationLayerManifest::addAnnotationManifest,
				AnnotationLayerManifest::removeAnnotationManifest,
				AnnotationLayerManifest::getAnnotationManifests,
				true, UNKNOWN_ID_CHECK,
				mockAnnotationManifest("key1"), mockAnnotationManifest("key2"), mockAnnotationManifest("key3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#setAnnotationFlag(de.ims.icarus2.model.manifest.api.AnnotationFlag, boolean)}.
	 */
	@SuppressWarnings("boxing")
	@TestFactory
	default Stream<DynamicTest> testSetAnnotationFlag() {
		return Stream.of(AnnotationFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertLockableSetter(settings(),(m, active) -> m.setAnnotationFlag(flag, active));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#addReferenceLayerId(java.lang.String)}.
	 */
	@Test
	default void testAddReferenceLayerId() {
		assertLockableAccumulativeAdd(settings(),
				inject_createTargetLayerManifest(AnnotationLayerManifest::addAndGetReferenceLayerId),
				ManifestTestUtils.getIllegalIdValues(), INVALID_ID_CHECK,
				true, DUPLICATE_ID_CHECK, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#addReferenceLayerId(String, java.util.function.Consumer)}.
	 */
	@Test
	default void testAddReferenceLayerIdStringConsumer() {
		assertLockableAccumulativeAdd(settings(),
				inject_consumeTargetLayerManifest(AnnotationLayerManifest::addReferenceLayerId),
				ManifestTestUtils.getIllegalIdValues(), INVALID_ID_CHECK,
				true, DUPLICATE_ID_CHECK, ManifestTestUtils.getLegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.AnnotationLayerManifest#removeReferenceLayerId(java.lang.String)}.
	 */
	@Test
	default void testRemoveReferenceLayerId() {
		assertLockableAccumulativeRemove(settings(),
				AnnotationLayerManifest::addAndGetReferenceLayerId,
				AnnotationLayerManifest::removeReferenceLayerId,
				TestUtils.transform_genericCollectionGetter(AnnotationLayerManifest::getReferenceLayerManifests, transform_targetLayerId()),
				true, UNKNOWN_ID_CHECK,
				"layer1", "layer2", "layer3");
	}

}
