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

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureManifestTest extends ContainerManifestTestMixin<StructureManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return Collections.singleton(ManifestType.STRUCTURE_LAYER_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getStructureType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetStructureType() {
		return Stream.of(StructureType.values())
				.map(structureType -> DynamicTest.dynamicTest(structureType.getStringValue(), () -> {
					assertDerivativeGetter(settings(), structureType, TestUtils.other(structureType),
							DEFAULT(StructureManifest.DEFAULT_STRUCTURE_TYPE),
							StructureManifest::getStructureType, StructureManifest::setStructureType);
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isLocalStructureType()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsLocalStructureType() {
		return Stream.of(StructureType.values())
				.map(structureType -> DynamicTest.dynamicTest(structureType.getStringValue(), () -> {
					assertDerivativeIsLocal(settings(), structureType, TestUtils.other(structureType),
							StructureManifest::isLocalStructureType, StructureManifest::setStructureType);
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isStructureFlagSet(de.ims.icarus2.model.manifest.api.StructureFlag)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testIsStructureFlagSet() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeFlagGetter(settings(), Boolean.FALSE,
							m -> m.isStructureFlagSet(flag),
							(m, active) -> m.setStructureFlag(flag, active.booleanValue()));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveStructureFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveStructureFlag() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					this.<StructureFlag>assertDerivativeForEach(
							settings(),
							flag, TestUtils.other(flag),
							StructureManifest::forEachActiveStructureFlag,
							(m,f) -> m.setStructureFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveLocalStructureFlag(java.util.function.Consumer)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testForEachActiveLocalStructureFlag() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					this.<StructureFlag>assertDerivativeForEachLocal(
							settings(),
							flag, TestUtils.other(flag),
							StructureManifest::forEachActiveLocalStructureFlag,
							(m,f) -> m.setStructureFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getActiveStructureFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveStructureFlags() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeGetter(settings(),
							flag, TestUtils.other(flag),
							StructureManifest::getActiveStructureFlags,
							(m,f) -> m.setStructureFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getActiveLocalStructureFlags()}.
	 */
	@TestFactory
	default Stream<DynamicTest> testGetActiveLocalStructureFlags() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertDerivativeAccumulativeLocalGetter(settings(),
							flag, TestUtils.other(flag),
							StructureManifest::getActiveLocalStructureFlags,
							(m,f) -> m.setStructureFlag(f, true));
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#setStructureType(de.ims.icarus2.model.manifest.api.StructureType)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetStructureType() {
		return Stream.of(StructureType.values())
				.map(structureType -> DynamicTest.dynamicTest(structureType.getStringValue(), () -> {
					assertLockableSetter(settings(), StructureManifest::setStructureType,
							structureType, true, ManifestTestUtils.TYPE_CAST_CHECK);
						}));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#setStructureFlag(de.ims.icarus2.model.manifest.api.StructureFlag, boolean)}.
	 */
	@TestFactory
	default Stream<DynamicTest> testSetStructureFlag() {
		return Stream.of(StructureFlag.values())
				.map(flag -> DynamicTest.dynamicTest(flag.getStringValue(), () -> {
					assertLockableSetter(settings(),
							(m, active) -> m.setStructureFlag(flag, active.booleanValue()));
						}));
	}

}
