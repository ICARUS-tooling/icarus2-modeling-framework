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
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.other;
import static de.ims.icarus2.test.TestUtils.settings;

import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.OverrideTest;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface ImplementationManifestTest extends EmbeddedMemberManifestTest<ImplementationManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return ManifestType.getMemberTypes();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.ManifestApiTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<ImplementationManifest> apiGuard) {
		EmbeddedMemberManifestTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("useFactory",
				Boolean.valueOf(ImplementationManifest.DEFAULT_USE_FACTORY_VALUE));
	}

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
			assertDerivativeOptGetter(settings(), sourceType, TestUtils.other(sourceType),
					NO_DEFAULT(),
					ImplementationManifest::getSourceType, ImplementationManifest::setSourceType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()}.
	 */
	@Test
	default void testGetSource() {
		assertDerivativeOptGetter(settings(), "source", "source2", TestUtils.NO_DEFAULT(),
				ImplementationManifest::getSource, ImplementationManifest::setSource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()}.
	 */
	@Test
	default void testGetClassname() {
		assertDerivativeOptGetter(settings(), "classname", "classname2", TestUtils.NO_DEFAULT(),
				ImplementationManifest::getClassname, ImplementationManifest::setClassname);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#isUseFactory()}.
	 */
	@Test
	default void testIsUseFactory() {
		assertDerivativeGetter(settings(), Boolean.TRUE, Boolean.FALSE,
				DEFAULT(ImplementationManifest.DEFAULT_USE_FACTORY_VALUE),
				ImplementationManifest::isUseFactory, ImplementationManifest::setUseFactory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSourceType(de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType)}.
	 */
	@Test
	default void testSetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			assertLockableSetter(settings(),ImplementationManifest::setSourceType, sourceType, true, ManifestTestUtils.TYPE_CAST_CHECK);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSource(java.lang.String)}.
	 */
	@Test
	default void testSetSource() {

		assertLockableSetter(settings(),ImplementationManifest::setSource, "source", true, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setClassname(java.lang.String)}.
	 */
	@Test
	default void testSetClassname() {
		assertLockableSetter(settings(),ImplementationManifest::setClassname, "classname", true, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setUseFactory(boolean)}.
	 */
	@Test
	default void testSetUseFactory() {
		assertLockableSetter(settings(),ImplementationManifest::setUseFactory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ImplementationManifest#isLocalSourceType()}.
	 */
	@Test
	default void testIsLocalSourceType() {
		for(SourceType sourceType : SourceType.values()) {
			assertDerivativeIsLocal(settings(),
					sourceType, other(sourceType),
					ImplementationManifest::isLocalSourceType,
					ImplementationManifest::setSourceType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ImplementationManifest#isLocalSource()}.
	 */
	@Test
	default void testIsLocalSource() {
		assertDerivativeIsLocal(settings(),
				"source1", "source2",
				ImplementationManifest::isLocalSource,
				ImplementationManifest::setSource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ImplementationManifest#isLocalClassname()}.
	 */
	@Test
	default void testIsLocalClassname() {
		assertDerivativeIsLocal(settings(),
				"classname1", "classname2",
				ImplementationManifest::isLocalClassname,
				ImplementationManifest::setClassname);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.standard.ImplementationManifest#isLocalUseFactory()}.
	 */
	@Test
	default void testIsLocalUseFactory() {
		assertDerivativeIsLocal(settings(),
				Boolean.TRUE, Boolean.FALSE,
				ImplementationManifest::isLocalUseFactory,
				ImplementationManifest::setUseFactory);
	}
}
