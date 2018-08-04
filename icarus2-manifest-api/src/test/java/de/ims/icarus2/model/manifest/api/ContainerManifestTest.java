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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ContainerManifestTest<M extends ContainerManifest> extends MemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getLayerManifest()}.
	 */
	@Test
	default void testGetLayerManifest() {
		assertNotNull(createUnlocked().getLayerManifest());
		assertNull(createTemplate().getLayerManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getContainerType()}.
	 */
	@Test
	default void testGetContainerType() {
		for(ContainerType containerType : ContainerType.values()) {
			assertDerivativeGetter(containerType, TestUtils.other(containerType),
					ContainerManifest.DEFAULT_CONTAINER_TYPE,
					ContainerManifest::getContainerType, ContainerManifest::setContainerType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#isLocalContainerType()}.
	 */
	@Test
	default void testIsLocalContainerType() {
		for(ContainerType containerType : ContainerType.values()) {
			assertDerivativeIsLocal(containerType, TestUtils.other(containerType),
					ContainerManifest::isLocalContainerType, ContainerManifest::setContainerType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#isContainerFlagSet(de.ims.icarus2.model.manifest.api.ContainerFlag)}.
	 */
	@Test
	default void testIsContainerFlagSet() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertDerivativeFlagGetter(Boolean.FALSE,
					m -> m.isContainerFlagSet(flag),
					(m, active) -> m.setContainerFlag(flag, active));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#forEachActiveContainerFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveContainerFlag() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertDerivativeForEach(flag, TestUtils.other(flag),
					m -> m::forEachActiveContainerFlag,
					(m,f) -> m.setContainerFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#forEachActiveLocalContainerFlag(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachActiveLocalContainerFlag() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertDerivativeForEachLocal(flag, TestUtils.other(flag),
					m -> m::forEachActiveLocalContainerFlag,
					(m,f) -> m.setContainerFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getActiveContainerFlags()}.
	 */
	@Test
	default void testGetActiveContainerFlags() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertDerivativeAccumulativeGetter(flag, TestUtils.other(flag),
					ContainerManifest::getActiveContainerFlags,
					(m,f) -> m.setContainerFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getActiveLocalContainerFlags()}.
	 */
	@Test
	default void testGetActiveLocalContainerFlags() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertDerivativeAccumulativeLocalGetter(flag, TestUtils.other(flag),
					ContainerManifest::getActiveLocalContainerFlags,
					(m,f) -> m.setContainerFlag(f, true));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getParentManifest()}.
	 */
	@Test
	default void testGetParentManifest() {
		assertNull(createUnlocked().getParentManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#getElementManifest()}.
	 */
	@Test
	default void testGetElementManifest() {
		assertNull(createUnlocked().getElementManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#setContainerType(de.ims.icarus2.model.manifest.api.ContainerType)}.
	 */
	@Test
	default void testSetContainerType() {
		for(ContainerType containerType : ContainerType.values()) {
			assertSetter(ContainerManifest::setContainerType, containerType, true);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerManifest#setContainerFlag(de.ims.icarus2.model.manifest.api.ContainerFlag, boolean)}.
	 */
	@Test
	default void testSetContainerFlag() {
		for(ContainerFlag flag : ContainerFlag.values()) {
			assertSetter((m, active) -> m.setContainerFlag(flag, active));
		}
	}

}
