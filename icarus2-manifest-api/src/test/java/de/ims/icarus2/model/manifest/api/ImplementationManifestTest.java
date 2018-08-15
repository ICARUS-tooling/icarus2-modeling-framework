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

import static de.ims.icarus2.test.GenericTest.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.settings;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.OverrideTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ImplementationManifestTest<M extends ImplementationManifest> extends EmbeddedMemberManifestTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.MemberManifestTest#testMandatoryConstructors()
	 */
	@SuppressWarnings("unchecked")
	@OverrideTest
	@Override
	@Test
	default void testMandatoryConstructors() throws Exception {
		assertConstructorHost(MemberManifest.class);
		assertConstructorManifestLocationManifestRegistryHost(MemberManifest.class);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSourceType()}.
	 */
	@Test
	default void testGetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			assertDerivativeGetter(settings(), sourceType, TestUtils.other(sourceType),
					ImplementationManifest.DEFAULT_SOURCE_TYPE,
					ImplementationManifest::getSourceType, ImplementationManifest::setSourceType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()}.
	 */
	@Test
	default void testGetSource() {
		assertDerivativeGetter(settings(), "source", "source2", NO_DEFAULT(),
				ImplementationManifest::getSource, ImplementationManifest::setSource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()}.
	 */
	@Test
	default void testGetClassname() {
		assertDerivativeGetter(settings(), "classname", "classname2", NO_DEFAULT(),
				ImplementationManifest::getClassname, ImplementationManifest::setClassname);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#isUseFactory()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsUseFactory() {
		assertDerivativeGetter(settings(), Boolean.TRUE, Boolean.FALSE,
				ImplementationManifest.DEFAULT_USE_FACTORY_VALUE,
				ImplementationManifest::isUseFactory, ImplementationManifest::setUseFactory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSourceType(de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType)}.
	 */
	@Test
	default void testSetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			assertLockableSetter(settings(),ImplementationManifest::setSourceType, sourceType, true, TYPE_CAST_CHECK);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSource(java.lang.String)}.
	 */
	@Test
	default void testSetSource() {

		assertLockableSetter(settings(),ImplementationManifest::setSource, "source", true, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setClassname(java.lang.String)}.
	 */
	@Test
	default void testSetClassname() {
		assertLockableSetter(settings(),ImplementationManifest::setClassname, "classname", true, TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setUseFactory(boolean)}.
	 */
	@Test
	default void testSetUseFactory() {
		assertLockableSetter(settings(),ImplementationManifest::setUseFactory);
	}

}
