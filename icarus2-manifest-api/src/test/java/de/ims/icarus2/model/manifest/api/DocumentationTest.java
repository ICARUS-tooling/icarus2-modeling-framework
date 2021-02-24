/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface DocumentationTest<D extends Documentation> extends LockableTest<D> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getManifestType()}.
	 */
	@Test
	default void testGetManifestType() {
		assertEquals(ManifestType.DOCUMENTATION, createUnlocked().getManifestType());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getContent()}.
	 */
	@Test
	default void testGetContent() {
		TestUtils.assertOptGetter(createUnlocked(),
				"content1", "content2", TestUtils.NO_DEFAULT(),
				Documentation::getContent, Documentation::setContent);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getResources()}.
	 */
	@Test
	default void testGetResources() {
		TestUtils.assertAccumulativeGetter(createUnlocked(),
				mock(Resource.class), mock(Resource.class),
				Documentation::getResources, Documentation::addResource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#forEachResource(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachResource() {
		TestUtils.<D, Resource>assertForEach(createUnlocked(),
				mock(Resource.class), mock(Resource.class),
				Documentation::forEachResource,
				Documentation::addResource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#setContent(java.lang.String)}.
	 */
	@Test
	default void testSetContent() {
		assertLockableSetter(settings(),Documentation::setContent, "content", false, ManifestTestUtils.TYPE_CAST_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#addResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testAddResource() {
		assertLockableAccumulativeAdd(
				settings(),Documentation::addResource,
				TestUtils.NO_ILLEGAL(), TestUtils.NO_CHECK, true, ManifestTestUtils.INVALID_INPUT_CHECK,
				mock(Resource.class), mock(Resource.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#removeResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testRemoveResource() {
		assertLockableAccumulativeRemove(
				settings(),Documentation::addResource,
				Documentation::removeResource, Documentation::getResources,
				true, ManifestTestUtils.INVALID_INPUT_CHECK, mock(Resource.class), mock(Resource.class));
	}

}
