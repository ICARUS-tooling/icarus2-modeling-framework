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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

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
				mockCategory("cat1"), mockCategory("cat2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#removeCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testRemoveCategory() {
		assertLockableAccumulativeRemove(Categorizable::addCategory, Categorizable::removeCategory,
				Categorizable::getCategories, true, false, mockCategory("cat1"), mockCategory("cat2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#forEachCategory(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCategory() {


		C categorizable = createUnlocked();

		TestUtils.assertForEachEmpty(categorizable::forEachCategory);

		Category category = mockCategory("cat1");
		Category category2 = mockCategory("cat2");

		categorizable.addCategory(category);
		TestUtils.assertForEachUnsorted(categorizable::forEachCategory, category);

		categorizable.addCategory(category2);
		TestUtils.assertForEachUnsorted(categorizable::forEachCategory, category, category2);

		categorizable.removeCategory(category);
		TestUtils.assertForEachUnsorted(categorizable::forEachCategory, category2);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#getCategories()}.
	 */
	@Test
	default void testGetCategories() {
		C categorizable = createUnlocked();

		assertTrue(categorizable.getCategories().isEmpty());

		Category category = mockCategory("cat1");
		Category category2 = mockCategory("cat2");

		categorizable.addCategory(category);
		assertTrue(categorizable.getCategories().contains(category));

		categorizable.addCategory(category2);
		assertTrue(categorizable.getCategories().contains(category2));

		categorizable.removeCategory(category);
		assertTrue(categorizable.getCategories().contains(category2));
		assertFalse(categorizable.getCategories().contains(category));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#hasCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testHasCategory() {
		C categorizable = createUnlocked();

		TestUtils.assertNPE(() -> categorizable.hasCategory(null));

		Category category = mockCategory("cat1");
		Category category2 = mockCategory("cat2");

		categorizable.addCategory(category);
		assertTrue(categorizable.hasCategory(category));
		assertFalse(categorizable.hasCategory(category2));

		categorizable.addCategory(category2);
		assertTrue(categorizable.hasCategory(category2));

		categorizable.removeCategory(category);
		assertFalse(categorizable.hasCategory(category));
		assertTrue(categorizable.hasCategory(category2));
	}

}