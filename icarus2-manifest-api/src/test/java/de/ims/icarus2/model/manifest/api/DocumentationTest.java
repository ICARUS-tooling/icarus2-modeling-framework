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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertAccumulativeGetter;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertGetter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;

/**
 * @author Markus Gärtner
 *
 */
public interface DocumentationTest<D extends Documentation> extends LockableTest<D>, ModifiableIdentityTest {

	/**
	 * @see de.ims.icarus2.model.manifest.api.ModifiableIdentityTest#createEmpty()
	 */
	@Override
	default ModifiableIdentity createEmpty() {
		return createUnlocked();
	}

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
		assertGetter(createUnlocked(), "content1", "content2", null,
				Documentation::getContent, Documentation::setContent);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#getResources()}.
	 */
	@Test
	default void testGetResources() {
		assertAccumulativeGetter(createUnlocked(),
				mock(Resource.class), mock(Resource.class),
				Documentation::getResources, Documentation::addResource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#forEachResource(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachResource() {
		ManifestTestUtils.assertForEach(createUnlocked(),
				mock(Resource.class), mock(Resource.class),
				(Function<D, Consumer<Consumer<? super Resource>>>)d -> d::forEachResource, Documentation::addResource);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#setContent(java.lang.String)}.
	 */
	@Test
	default void testSetContent() {
		assertLockableSetter(Documentation::setContent, "content", false);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#addResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testAddResource() {
		assertLockableAccumulativeAdd(Documentation::addResource,
				null, true, true,
				mock(Resource.class), mock(Resource.class));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Documentation#removeResource(de.ims.icarus2.model.manifest.api.Documentation.Resource)}.
	 */
	@Test
	default void testRemoveResource() {
		assertLockableAccumulativeRemove(Documentation::addResource,
				Documentation::removeResource, Documentation::getResources,
				true, true, mock(Resource.class), mock(Resource.class));
	}

}
