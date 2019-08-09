/**
 *
 */
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValue;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationStorageTest<S extends AnnotationStorage>
		extends ApiGuardedTest<S> {

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);

		apiGuard.nullGuard(true);
	}

	/** The main value type for the storage under test */
//	ValueType getValueType();

	static final Set<ValueType> ALL_TYPES = Collections.unmodifiableSet(
			new ObjectOpenHashSet<>(ValueType.valueTypes()));

	/** Types supported for setting values on the storage */
	Set<ValueType> typesForSetters();

	/** Types supported when fetching values from the storage */
	Set<ValueType> typesForGetters();

	default String key() {
		return "test";
	}

	default Object noEntryValue(String key) {
		return null;
	}

	Object testValue(String key);

	default AnnotationLayerManifest createManifest(String key) {
		AnnotationLayerManifest manifest = mockTypedManifest(AnnotationLayerManifest.class);
		when(manifest.getAvailableKeys()).thenReturn(singleton(key));
		when(manifest.getDefaultKey()).thenReturn(Optional.of(key));

		AnnotationManifest annotationManifest = mock(AnnotationManifest.class);
		when(annotationManifest.getKey()).thenReturn(Optional.of(key));
		when(annotationManifest.getNoEntryValue()).thenReturn(Optional.ofNullable(noEntryValue(key)));
		when(manifest.getAnnotationManifest(key)).thenReturn(Optional.of(annotationManifest));

		when(manifest.getAnnotationManifests()).thenReturn(singleton(annotationManifest));

		return manifest;
	}

	default AnnotationLayer createLayer(AnnotationLayerManifest manifest) {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		when(layer.getManifest()).thenReturn(manifest);
		return layer;
	}

	S createForLayer(AnnotationLayer layer);

	static void assertUnsupportedType(Executable executable) {
		RuntimeException ex = assertThrows(RuntimeException.class, executable);

		if(ex instanceof ModelException) {
			assertEquals(GlobalErrorCode.UNSUPPORTED_OPERATION, ((ModelException)ex).getErrorCode());
		} else {
			assertEquals(ClassCastException.class, ex.getClass());
		}
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		return settings.process(createForLayer(createLayer(createManifest(key()))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@Test
	default void testCollectKeysEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));

		Consumer<String> collector = mock(Consumer.class);
		assertFalse(storage.collectKeys(mockItem(), collector));

		verify(collector, never()).accept(anyString());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@Test
	default void testCollectKeysSingular() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		Consumer<String> collector = mock(Consumer.class);

		assertTrue(storage.collectKeys(item, collector));

		verify(collector).accept(key);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetValueEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		assertEquals(noEntryValue(key), storage.getValue(item, key));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getString(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetStringEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.STRING)) {
			storage.getString(item, key);
		} else {
			assertUnsupportedType(() -> storage.getString(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetIntegerEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.INTEGER)) {
			storage.getInteger(item, key);
		} else {
			assertUnsupportedType(() -> storage.getInteger(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetFloatEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.FLOAT)) {
			storage.getFloat(item, key);
		} else {
			assertUnsupportedType(() -> storage.getFloat(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetDoubleEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.DOUBLE)) {
			storage.getDouble(item, key);
		} else {
			assertUnsupportedType(() -> storage.getDouble(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetLongEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.LONG)) {
			storage.getLong(item, key);
		} else {
			assertUnsupportedType(() -> storage.getLong(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetBooleanEmpty() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();

		if(typesForGetters().contains(ValueType.BOOLEAN)) {
			storage.getBoolean(item, key);
		} else {
			assertUnsupportedType(() -> storage.getBoolean(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)}.
	 */
	@Test
	default void testRemoveAllValuesSupplierOfQextendsItem() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item[] items = Stream.generate(ModelTestUtils::mockItem)
				.limit(random(12, 20))
				.toArray(Item[]::new);

		for (Item item : items) {
			storage.setValue(item, key, testValue(key));
		}

		int from = random(3, items.length/2);
		int to = random(items.length/2 + 1, items.length-3);

		MutableInteger pointer = new MutableInteger(from);
		Supplier<? extends Item> supplier = () -> {
			int index = pointer.getAndIncrement();
			return index<=to ? items[index] : null;
		};

		storage.removeAllValues(supplier);

		for (int i = 0; i < items.length; i++) {
			if(i<from || i>to) {
				assertTrue(storage.hasAnnotations(items[i]), "Missing annotation for item at index "+i);
			} else {
				assertFalse(storage.hasAnnotations(items[i]), "Annotation still present for item at index "+i);
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.Iterator)}.
	 */
	@Test
	default void testRemoveAllValuesIteratorOfQextendsItem() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item[] items = Stream.generate(ModelTestUtils::mockItem)
				.limit(random(12, 20))
				.toArray(Item[]::new);

		for (Item item : items) {
			storage.setValue(item, key, testValue(key));
		}

		int from = random(3, items.length/2);
		int to = random(items.length/2 + 1, items.length-3);

		Iterator<? extends Item> iterator =
				Arrays.asList(items).subList(from, to+1).iterator();

		storage.removeAllValues(iterator);

		for (int i = 0; i < items.length; i++) {
			if(i<from || i>to) {
				assertTrue(storage.hasAnnotations(items[i]), "Missing annotation for item at index "+i);
			} else {
				assertFalse(storage.hasAnnotations(items[i]), "Annotation still present for item at index "+i);
			}
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		assertEquals(value, storage.getValue(item, key));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testSetString() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		String value = (String) getTestValue(ValueType.STRING);

		if(typesForSetters().contains(ValueType.STRING)) {
			storage.setString(item, key, value);
			assertEquals(value, storage.getString(item, key));
		} else {
			assertUnsupportedType(() -> storage.setString(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@Test
	default void testSetInteger() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		int value = ((Integer) getTestValue(ValueType.INTEGER)).intValue();

		if(typesForSetters().contains(ValueType.INTEGER)) {
			storage.setInteger(item, key, value);
			assertEquals(value, storage.getInteger(item, key));
		} else {
			assertUnsupportedType(() -> storage.setInteger(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@Test
	default void testSetLong() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		long value = ((Long) getTestValue(ValueType.LONG)).longValue();

		if(typesForSetters().contains(ValueType.LONG)) {
			storage.setLong(item, key, value);
			assertEquals(value, storage.getLong(item, key));
		} else {
			assertUnsupportedType(() -> storage.setLong(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@Test
	default void testSetFloat() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		float value = ((Float) getTestValue(ValueType.FLOAT)).floatValue();

		if(typesForSetters().contains(ValueType.FLOAT)) {
			storage.setFloat(item, key, value);
			assertEquals(value, storage.getFloat(item, key));
		} else {
			assertUnsupportedType(() -> storage.setFloat(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@Test
	default void testSetDouble() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		double value = ((Double) getTestValue(ValueType.DOUBLE)).doubleValue();

		if(typesForSetters().contains(ValueType.DOUBLE)) {
			storage.setDouble(item, key, value);
			assertEquals(value, storage.getDouble(item, key));
		} else {
			assertUnsupportedType(() -> storage.setDouble(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testSetBoolean() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		boolean value = ((Boolean) getTestValue(ValueType.BOOLEAN)).booleanValue();

		if(typesForSetters().contains(ValueType.BOOLEAN)) {
			storage.setBoolean(item, key, value);
			assertEquals(value, storage.getBoolean(item, key));
		} else {
			assertUnsupportedType(() -> storage.setBoolean(item, key, value));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Test
	default void testHasAnnotationsEmpty() {
		S storage = createForLayer(createLayer(createManifest(key())));
		assertFalse(storage.hasAnnotations());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Test
	default void testHasAnnotations() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		assertTrue(storage.hasAnnotations());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testHasAnnotationsItemEmpty() {
		S storage = createForLayer(createLayer(createManifest(key())));
		assertFalse(storage.hasAnnotations(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testHasAnnotationsItem() {
		String key = key();
		S storage = createForLayer(createLayer(createManifest(key)));
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		assertTrue(storage.hasAnnotations(item));
		assertFalse(storage.hasAnnotations(mockItem()));
	}

}
