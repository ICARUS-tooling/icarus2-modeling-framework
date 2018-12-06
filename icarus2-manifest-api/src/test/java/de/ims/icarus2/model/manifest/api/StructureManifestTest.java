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

import static de.ims.icarus2.test.TestUtils.DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureManifestTest<M extends StructureManifest> extends ContainerManifestTest<M> {

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
	@Test
	default void testGetStructureType() {
		for(StructureType structureType : StructureType.values()) {
			assertDerivativeGetter(settings(), structureType, TestUtils.other(structureType),
					DEFAULT(StructureManifest.DEFAULT_STRUCTURE_TYPE),
					StructureManifest::getStructureType, StructureManifest::setStructureType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isLocalStructureType()}.
	 */
	@Test
	default void testIsLocalStructureType() {
		for(StructureType structureType : StructureType.values()) {
			assertDerivativeIsLocal(settings(), structureType, TestUtils.other(structureType),
					StructureManifest::isLocalStructureType, StructureManifest::setStructureType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isStructureFlagSet(de.ims.icarus2.model.manifest.api.StructureFlag)}.
	 */
	@Test
	default void testIsStructureFlagSet() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeFlagGetter(settings(), Boolean.FALSE,
					m -> m.isStructureFlagSet(flag),
					(m, active) -> m.setStructureFlag(flag, active.booleanValue()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveStructureFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeForEach(settings(), flag, TestUtils.other(flag),
					StructureManifest::forEachActiveStructureFlag,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveLocalStructureFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveLocalStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeForEachLocal(settings(), flag, TestUtils.other(flag),
					StructureManifest::forEachActiveLocalStructureFlag,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getActiveStructureFlags()}.
	 */
	@Test
	default void testGetActiveStructureFlags() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeAccumulativeGetter(settings(), flag, TestUtils.other(flag),
					StructureManifest::getActiveStructureFlags,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getActiveLocalStructureFlags()}.
	 */
	@Test
	default void testGetActiveLocalStructureFlags() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeAccumulativeLocalGetter(settings(), flag, TestUtils.other(flag),
					StructureManifest::getActiveLocalStructureFlags,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#setStructureType(de.ims.icarus2.model.manifest.api.StructureType)}.
	 */
	@Test
	default void testSetStructureType() {
		for(StructureType structureType : StructureType.values()) {
			assertLockableSetter(settings(), StructureManifest::setStructureType,
					structureType, true, TYPE_CAST_CHECK);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#setStructureFlag(de.ims.icarus2.model.manifest.api.StructureFlag, boolean)}.
	 */
	@Test
	default void testSetStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertLockableSetter(settings(),
					(m, active) -> m.setStructureFlag(flag, active.booleanValue()));
		}
	}

}
