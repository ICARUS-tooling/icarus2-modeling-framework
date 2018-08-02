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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface ImplementationManifestTest extends MemberManifestTest<ImplementationManifest> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getHostManifest()}.
	 */
	@Test
	default void testGetHostManifest() {
		testGetHost();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSourceType()}.
	 */
	@Test
	default void testGetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			ImplementationManifest manifest = createUnlocked();

			assertEquals(ImplementationManifest.DEFAULT_SOURCE_TYPE, manifest.getSourceType());

			manifest.setSourceType(sourceType);
			assertEquals(sourceType, manifest.getSourceType());

			ImplementationManifest template = createTemplate();
			template.setSourceType(sourceType);
			ImplementationManifest derived = createDerived(template);

			assertEquals(sourceType, derived.getSourceType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getSource()}.
	 */
	@Test
	default void testGetSource() {
		ImplementationManifest manifest = createUnlocked();
		String source = "source";
		String soruce2 = "source2";

		assertNull(manifest.getSource());

		manifest.setSource(source);
		assertEquals(source, manifest.getSource());

		ImplementationManifest template = createTemplate();
		template.setSource(source);
		ImplementationManifest derived = createDerived(template);

		assertEquals(source, derived.getSource());

		derived.setSource(soruce2);

		assertEquals(soruce2, derived.getSource());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#getClassname()}.
	 */
	@Test
	default void testGetClassname() {
		ImplementationManifest manifest = createUnlocked();
		String classname = "classname";
		String classname2 = "classname2";

		assertNull(manifest.getClassname());

		manifest.setClassname(classname);
		assertEquals(classname, manifest.getClassname());

		ImplementationManifest template = createTemplate();
		template.setClassname(classname);
		ImplementationManifest derived = createDerived(template);

		assertEquals(classname, derived.getClassname());

		derived.setClassname(classname2);

		assertEquals(classname2, derived.getClassname());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#isUseFactory()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testIsUseFactory() {
		ImplementationManifest manifest = createUnlocked();

		assertEquals(ImplementationManifest.DEFAULT_USE_FACTORY_VALUE, manifest.isUseFactory());

		manifest.setUseFactory(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);
		assertEquals(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE, manifest.isUseFactory());

		ImplementationManifest template = createTemplate();
		template.setUseFactory(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);
		ImplementationManifest derived = createDerived(template);

		assertEquals(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE, derived.isUseFactory());

		derived.setUseFactory(ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);

		assertEquals(ImplementationManifest.DEFAULT_USE_FACTORY_VALUE, derived.isUseFactory());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSourceType(de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType)}.
	 */
	@Test
	default void testSetSourceType() {

		for(SourceType sourceType : SourceType.values()) {
			ImplementationManifest manifest = createUnlocked();

			assertEquals(ImplementationManifest.DEFAULT_SOURCE_TYPE, manifest.getSourceType());

			TestUtils.assertNPE(() -> manifest.setSourceType(null));

			manifest.setSourceType(sourceType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setSource(java.lang.String)}.
	 */
	@Test
	default void testSetSource() {
		ImplementationManifest manifest = createUnlocked();

		TestUtils.assertNPE(() -> manifest.setSource(null));

		manifest.setSource("someSource");

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setSource("anotherSource"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setClassname(java.lang.String)}.
	 */
	@Test
	default void testSetClassname() {
		ImplementationManifest manifest = createUnlocked();

		TestUtils.assertNPE(() -> manifest.setClassname(null));

		manifest.setClassname("classname");

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setClassname("classname2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ImplementationManifest#setUseFactory(boolean)}.
	 */
	@Test
	default void testSetUseFactory() {
		ImplementationManifest manifest = createUnlocked();

		manifest.setUseFactory(true);
		manifest.setUseFactory(false);

		manifest.lock();
		LockableTest.assertLocked(() -> manifest.setUseFactory(true));
	}

}
