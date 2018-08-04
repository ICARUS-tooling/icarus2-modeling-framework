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

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureManifestTest<M extends StructureManifest> extends ContainerManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getStructureType()}.
	 */
	@Test
	default void testGetStructureType() {
		for(StructureType structureType : StructureType.values()) {
			assertDerivativeGetter(structureType, TestUtils.other(structureType),
					StructureManifest.DEFAULT_STRUCTURE_TYPE,
					StructureManifest::getStructureType, StructureManifest::setStructureType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isLocalStructureType()}.
	 */
	@Test
	default void testIsLocalStructureType() {
		for(StructureType structureType : StructureType.values()) {
			assertDerivativeIsLocal(structureType, TestUtils.other(structureType),
					StructureManifest::isLocalStructureType, StructureManifest::setStructureType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#isStructureFlagSet(de.ims.icarus2.model.manifest.api.StructureFlag)}.
	 */
	@Test
	default void testIsStructureFlagSet() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeFlagGetter(Boolean.FALSE,
					m -> m.isStructureFlagSet(flag),
					(m, active) -> m.setStructureFlag(flag, active));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveStructureFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeForEach(flag, TestUtils.other(flag),
					m -> m::forEachActiveStructureFlag,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#forEachActiveLocalStructureFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveLocalStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeForEachLocal(flag, TestUtils.other(flag),
					m -> m::forEachActiveLocalStructureFlag,
					(m,f) -> m.setStructureFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#getActiveStructureFlags()}.
	 */
	@Test
	default void testGetActiveStructureFlags() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertDerivativeAccumulativeGetter(flag, TestUtils.other(flag),
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
			assertDerivativeAccumulativeLocalGetter(flag, TestUtils.other(flag),
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
			assertSetter(StructureManifest::setStructureType, structureType, true);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.StructureManifest#setStructureFlag(de.ims.icarus2.model.manifest.api.StructureFlag, boolean)}.
	 */
	@Test
	default void testSetStructureFlag() {
		for(StructureFlag flag : StructureFlag.values()) {
			assertSetter((m, active) -> m.setStructureFlag(flag, active));
		}
	}

}
