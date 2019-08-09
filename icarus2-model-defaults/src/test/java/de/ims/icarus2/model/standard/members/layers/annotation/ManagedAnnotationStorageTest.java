/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.AnnotationStorageTest;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.PartTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManagedAnnotationStorageTest<S extends ManagedAnnotationStorage>
		extends AnnotationStorageTest<S>, PartTest<AnnotationLayer, S> {

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@Override
	default AnnotationLayer createEnvironment() {
		return createLayer(createManifest(key()));
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		AnnotationLayer layer = createEnvironment();
		S storage = createForLayer(layer);
		storage.addNotify(layer);
		return settings.process(storage);
	}

	@Override
	default S createForKey(String key) {
		AnnotationLayer layer = createLayer(createManifest(key));
		S storage = createForLayer(layer);
		storage.addNotify(layer);
		return storage;
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#createUnadded()
	 */
	@Override
	default S createUnadded() {
		return createForLayer(createEnvironment());
	}

	default boolean supportsItemManagement() {
		return true;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#containsItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testContainsItemEmpty() {
		assertFalse(create().containsItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#containsItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testContainsItemForeign() {
		S storage = create();
		storage.addItem(mockItem());
		assertFalse(storage.containsItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#addItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItem() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		S storage = create();
		Item item = mockItem();

		assertTrue(storage.addItem(item));
		assertTrue(storage.containsItem(item));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#addItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testAddItemRepeated() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		S storage = create();
		Item item = mockItem();

		assertTrue(storage.addItem(item));
		// Subsequently adding the same item should have no effect
		assertFalse(storage.addItem(item));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItem() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		S storage = create();
		Item item = mockItem();

		storage.addItem(item);
		assertTrue(storage.removeItem(item));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemForeign() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		S storage = create();

		storage.addItem(mockItem());
		assertFalse(storage.removeItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemEmpty() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		assertFalse(create().removeItem(mockItem()));
	}

}
