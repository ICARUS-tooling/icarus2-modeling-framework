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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface CategorizableTest<C extends Categorizable> extends LockableTest<C> {

	public static Category mockCategory(String id) {
		Category category = mock(Category.class);
		when(category.getId()).thenReturn(id);
		return category;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#addCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testAddCategory() {
		assertLockableAccumulativeAdd(Categorizable::addCategory, null, true, false,
				mockCategory("cat1"), mockCategory("cat2"), mockCategory("cat3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#removeCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testRemoveCategory() {
		assertLockableAccumulativeRemove(Categorizable::addCategory, Categorizable::removeCategory,
				Categorizable::getCategories, true, false,
				mockCategory("cat1"), mockCategory("cat2"), mockCategory("cat3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#forEachCategory(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCategory() {
		ManifestTestUtils.assertForEach(createUnlocked(),
				mockCategory("cat1"), mockCategory("cat2"),
				(Function<C, Consumer<Consumer<? super Category>>>)m -> m::forEachCategory, Categorizable::addCategory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#getCategories()}.
	 */
	@Test
	default void testGetCategories() {
		ManifestTestUtils.assertAccumulativeGetter(createUnlocked(),
				mockCategory("cat1"), mockCategory("cat2"),
				Categorizable::getCategories, Categorizable::addCategory);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#hasCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testHasCategory() {
		ManifestTestUtils.assertAccumulativeFlagGetter(createUnlocked(),
				Categorizable::hasCategory,
				Categorizable::addCategory, Categorizable::removeCategory,
				mockCategory("cat1"), mockCategory("cat2"));
	}

}
