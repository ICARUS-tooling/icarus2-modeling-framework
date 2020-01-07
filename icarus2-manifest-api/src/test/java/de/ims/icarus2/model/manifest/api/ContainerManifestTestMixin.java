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

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertPresent;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.collections.CollectionUtils.set;

import java.util.Set;
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
interface ContainerManifestTestMixin<M extends ContainerManifestBase<?>> extends EmbeddedMemberManifestTest<M> {


	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return set(ManifestType.ITEM_LAYER_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertPresent(createUnlocked().getLayerManifest());
		assertNotPresent(createTemplate(settings()).getLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getContainerType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetContainerType() {
		return Stream.of(ContainerType.values())
				.map(containerType -> DynamicTest.dynamicTest(containerType.getStringValue(), () -> {
					assertDerivativeGetter(settings(), containerType, TestUtils.other(containerType),
							DEFAULT(ContainerManifestBase.DEFAULT_CONTAINER_TYPE),
							ContainerManifestBase::getContainerType,
							ContainerManifestBase::setContainerType);
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#isLocalContainerType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLocalContainerType() {
		return Stream.of(ContainerType.values())
				.map(containerType -> DynamicTest.dynamicTest(containerType.getStringValue(), () -> {
					assertDerivativeIsLocal(settings(), containerType, TestUtils.other(containerType),
							ContainerManifestBase::isLocalContainerType,
							ContainerManifestBase::setContainerType);
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#isContainerFlagSet(de.ims.icarus2.model.manifest.api.ContainerFlag)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsContainerFlagSet() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeFlagGetter(settings(), Boolean.FALSE,
							m -> m.isContainerFlagSet(flag),
							(m, active) -> m.setContainerFlag(flag, active.booleanValue()));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#forEachActiveContainerFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveContainerFlag() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					this.<ContainerFlag>assertDerivativeForEach(
							settings(),
							flag, TestUtils.other(flag),
							ContainerManifestBase::forEachActiveContainerFlag,
							(m,f) -> m.setContainerFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#forEachActiveLocalContainerFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveLocalContainerFlag() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					this.<ContainerFlag>assertDerivativeForEachLocal(
							settings(),
							flag, TestUtils.other(flag),
							ContainerManifestBase::forEachActiveLocalContainerFlag,
							(m,f) -> m.setContainerFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getActiveContainerFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveContainerFlags() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeGetter(settings(), flag, TestUtils.other(flag),
							ContainerManifestBase::getActiveContainerFlags,
							(m,f) -> m.setContainerFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getActiveLocalContainerFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveLocalContainerFlags() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeLocalGetter(settings(), flag, TestUtils.other(flag),
							ContainerManifestBase::getActiveLocalContainerFlags,
							(m,f) -> m.setContainerFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getParentManifest()}.
	 */
	@Test
	default void testGetParentManifest() {
		assertNotPresent(createUnlocked().getParentManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#getElementManifest()}.
	 */
	@Test
	default void testGetElementManifest() {
		assertNotPresent(createUnlocked().getElementManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#setContainerType(de.ims.icarus2.model.manifest.api.ContainerType)}.
	 */
	@Test
	default void testSetContainerType() {
		for(ContainerType containerType : ContainerType.values()) {
			assertLockableSetter(settings(), ContainerManifestBase::setContainerType,
					containerType, true, ManifestTestUtils.TYPE_CAST_CHECK);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifestBase#setContainerFlag(de.ims.icarus2.model.manifest.api.ContainerFlag, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetContainerFlag() {
		return Stream.of(ContainerFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertLockableSetter(settings(),
							(m, active) -> m.setContainerFlag(flag, active.booleanValue()));
						}));
	}

}
