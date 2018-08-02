/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface CategorizableTest extends LockableTest {

	@Override
	Categorizable createUnlocked();

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
		Categorizable categorizable = createUnlocked();

		Category category = mockCategory("cat1");
		categorizable.addCategory(category);
		categorizable.addCategory(category);

		categorizable.addCategory(mockCategory("cat2"));

		assertThrows(NullPointerException.class, () -> categorizable.addCategory(null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#removeCategory(de.ims.icarus2.model.manifest.api.Category)}.
	 */
	@Test
	default void testRemoveCategory() {
		Categorizable categorizable = createUnlocked();

		Category category = mockCategory("cat1");
		categorizable.addCategory(category);

		assertTrue(categorizable.hasCategory(category));

		categorizable.removeCategory(category);
		assertFalse(categorizable.hasCategory(category));

		assertThrows(NullPointerException.class, () -> categorizable.removeCategory(null));

		categorizable.lock();
		LockableTest.assertLocked(() -> categorizable.addCategory(category));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Categorizable#forEachCategory(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachCategory() {
		Categorizable categorizable = createUnlocked();

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
		Categorizable categorizable = createUnlocked();

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
		Categorizable categorizable = createUnlocked();

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
