/*
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
package de.ims.icarus2.model.api.layer.annotation;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.PartTest;

/**
 * @author Markus Gärtner
 *
 */
public interface ManagedAnnotationStorageTest<S extends ManagedAnnotationStorage>
		extends AnnotationStorageTest<S>, PartTest<AnnotationLayer, S> {

	@Override
	default AnnotationLayer createEnvironment() {
		return createLayer(createManifest(key()));
	}

	@Provider
	@Override
	default S createForKey(String key) {
		AnnotationLayer layer = createLayer(createManifest(key));
		S storage = createForLayer(layer);
		storage.addNotify(layer);
		return storage;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		AnnotationLayer layer = createLayer(createManifest());
		S storage = createForLayer(layer);
		storage.addNotify(layer);
		return settings.process(storage);
	}

	@Provider
	@Override
	default S createUnadded() {
		return createForLayer(createEnvironment());
	}

	/**
	 * Signals whether or not the implementation under test is expected
	 * to honor the contracts for {@link ManagedAnnotationStorage#addItem(Item)}
	 * and {@link ManagedAnnotationStorage#removeItem(Item)}.
	 */
	default boolean supportsItemManagement() {
		return true;
	}

	/**
	 * Signals if the implementation under test is able to automatically remove
	 * any entries that do not differ from the designated {@code noEntryValue}.
	 * @return
	 */
	default boolean supportsAutoRemoval() {
		return true;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#containsItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testContainsItemEmpty() {
		assertFalse(create().containsItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#containsItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testContainsItemForeign() {
		S storage = create();
		storage.addItem(mockItem());
		assertFalse(storage.containsItem(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#addItem(de.ims.icarus2.model.api.members.item.Item)}.
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
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#addItem(de.ims.icarus2.model.api.members.item.Item)}.
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
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
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
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveByNoEntryValue() {
		if(!supportsItemManagement() || !supportsAutoRemoval())
			throw new TestAbortedException("Implementation does not support automatic removal");

		String key = key();
		S storage = createForKey(key);
		Item item = mockItem();

		storage.setValue(item, key, testValue(key));
		assertTrue(storage.containsItem(item));

		storage.setValue(item, key, noEntryValue(key));
		assertFalse(storage.containsItem(item));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
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
	 * Test method for {@link de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage#removeItem(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testRemoveItemEmpty() {
		if(!supportsItemManagement())
			throw new TestAbortedException("Implementation does not support item management");

		assertFalse(create().removeItem(mockItem()));
	}

}
