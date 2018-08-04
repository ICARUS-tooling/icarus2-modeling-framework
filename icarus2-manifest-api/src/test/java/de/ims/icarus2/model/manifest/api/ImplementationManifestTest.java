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

import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ImplementationManifestTest<M extends ImplementationManifest> extends MemberManifestTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSourceType()}.
	 */
	@Test
	default void testGetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			assertDerivativeGetter(sourceType, TestUtils.other(sourceType),
					ImplementationManifest.DEFAULT_SOURCE_TYPE,
					ImplementationManifest::getSourceType, ImplementationManifest::setSourceType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()}.
	 */
	@Test
	default void testGetSource() {
		assertDerivativeGetter("source", "source2", null,
				ImplementationManifest::getSource, ImplementationManifest::setSource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()}.
	 */
	@Test
	default void testGetClassname() {
		assertDerivativeGetter("classname", "classname2", null,
				ImplementationManifest::getClassname, ImplementationManifest::setClassname);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#isUseFactory()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsUseFactory() {
		assertDerivativeGetter(Boolean.TRUE, Boolean.FALSE,
				ImplementationManifest.DEFAULT_USE_FACTORY_VALUE,
				ImplementationManifest::isUseFactory, ImplementationManifest::setUseFactory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSourceType(de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType)}.
	 */
	@Test
	default void testSetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			assertLockableSetter(ImplementationManifest::setSourceType, sourceType, true);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSource(java.lang.String)}.
	 */
	@Test
	default void testSetSource() {

		assertLockableSetter(ImplementationManifest::setSource, "source", true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setClassname(java.lang.String)}.
	 */
	@Test
	default void testSetClassname() {
		assertLockableSetter(ImplementationManifest::setClassname, "classname", true);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setUseFactory(boolean)}.
	 */
	@Test
	default void testSetUseFactory() {
		assertLockableSetter(ImplementationManifest::setUseFactory);
	}

}
