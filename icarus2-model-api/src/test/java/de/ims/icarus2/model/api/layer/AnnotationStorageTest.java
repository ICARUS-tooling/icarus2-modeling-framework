/**
 *
 */
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
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

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.ModelTestUtils;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
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

	static final Set<ValueType> ALL_TYPES = Collections.unmodifiableSet(
			new ObjectOpenHashSet<>(ValueType.valueTypes()));

	static final Set<ValueType> NUMBER_TYPES = Collections.unmodifiableSet(set(
			ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE));

	/** Types supported for setting values on the storage when using given key */
	Set<ValueType> typesForSetters(String key);

	/** Types supported when fetching values from the storage when using given key */
	Set<ValueType> typesForGetters(String key);

	default String key() {
		return "test";
	}

	default String keyForType(ValueType type) {
		return key();
	}

	default Object noEntryValue(String key) {
		return null;
	}

	Object testValue(String key);

	ValueType valueType(String key);

	default AnnotationManifest createAnnotationManifest(String key) {
		AnnotationManifest annotationManifest = mock(AnnotationManifest.class);
		when(annotationManifest.getValueType()).thenReturn(valueType(key));
		when(annotationManifest.getKey()).thenReturn(Optional.of(key));
		when(annotationManifest.getNoEntryValue()).thenReturn(Optional.ofNullable(noEntryValue(key)));
		return annotationManifest;
	}

	default AnnotationLayerManifest createManifest(String key) {
		AnnotationLayerManifest manifest = mockTypedManifest(AnnotationLayerManifest.class);
		when(manifest.getAvailableKeys()).thenReturn(singleton(key));
		when(manifest.getDefaultKey()).thenReturn(Optional.of(key));

		AnnotationManifest annotationManifest = createAnnotationManifest(key);
		when(manifest.getAnnotationManifest(key)).thenReturn(Optional.of(annotationManifest));

		when(manifest.getAnnotationManifests()).thenReturn(singleton(annotationManifest));

		return manifest;
	}

	default AnnotationLayerManifest createManifest() {
		return createManifest(key());
	}

	default AnnotationLayer createLayer(AnnotationLayerManifest manifest) {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		when(layer.getManifest()).thenReturn(manifest);
		return layer;
	}

	@Provider
	default S createForKey(String key) {
		AnnotationLayer layer = createLayer(createManifest(key));
		S storage = createForLayer(layer);
		if(storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)storage).addNotify(layer);
		}
		return storage;
	}

	@Provider
	S createForLayer(AnnotationLayer layer);

	static void assertUnsupportedType(Executable executable) {
		assertUnsupportedType(null, executable);
	}

	/**
	 * Expects a {@link ClassCastException} or {@link ModelException}
	 * of type {@link GlobalErrorCode#UNSUPPORTED_OPERATION} when executing.
	 */
	static void assertUnsupportedType(String msg, Executable executable) {
		RuntimeException ex = assertThrows(RuntimeException.class, executable, msg);

		if(ex instanceof IcarusRuntimeException) {
			ErrorCode errorCode = ((IcarusRuntimeException)ex).getErrorCode();

			assertTrue(errorCode == GlobalErrorCode.UNSUPPORTED_OPERATION
					|| errorCode == ManifestErrorCode.MANIFEST_TYPE_CAST);
		} else {
			assertEquals(ClassCastException.class, ex.getClass());
		}
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		AnnotationLayer layer = createLayer(createManifest());
		S storage = createForLayer(layer);
		if(storage instanceof ManagedAnnotationStorage) {
			((ManagedAnnotationStorage)storage).addNotify(layer);
		}
		return settings.process(storage);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@Test
	default void testCollectKeysEmpty() {
		String key = key();
		S storage = createForKey(key);

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
		S storage = createForKey(key);
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
		S storage = createForKey(key);
		Item item = mockItem();

		assertEquals(noEntryValue(key), storage.getValue(item, key));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getString(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetStringEmpty() {
		String key = keyForType(ValueType.STRING);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.STRING)) {
			assertEquals(noEntryValue(key), storage.getString(item, key));
		} else {
			assertUnsupportedType(() -> storage.getString(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetIntegerEmpty() {
		String key = keyForType(ValueType.INTEGER);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.INTEGER)) {
			assertEquals(((Number)noEntryValue(key)).intValue(), storage.getInteger(item, key));
		} else {
			assertUnsupportedType(() -> storage.getInteger(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetFloatEmpty() {
		String key = keyForType(ValueType.FLOAT);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.FLOAT)) {
			assertEquals(((Number)noEntryValue(key)).floatValue(), storage.getFloat(item, key));
		} else {
			assertUnsupportedType(() -> storage.getFloat(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetDoubleEmpty() {
		String key = keyForType(ValueType.DOUBLE);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.DOUBLE)) {
			assertEquals(((Number)noEntryValue(key)).doubleValue(), storage.getDouble(item, key));
		} else {
			assertUnsupportedType(() -> storage.getDouble(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetLongEmpty() {
		String key = keyForType(ValueType.LONG);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.LONG)) {
			assertEquals(((Number)noEntryValue(key)).longValue(), storage.getLong(item, key));
		} else {
			assertUnsupportedType(() -> storage.getLong(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetBooleanEmpty() {
		String key = keyForType(ValueType.BOOLEAN);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForGetters(key).contains(ValueType.BOOLEAN)) {
			assertEquals(((Boolean)noEntryValue(key)).booleanValue(), storage.getBoolean(item, key));
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
		S storage = createForKey(key);
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
		S storage = createForKey(key);
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
		S storage = createForKey(key);
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		assertEquals(value, storage.getValue(item, key));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Test
	default void testSetValueNull() {
		String key = key();
		S storage = createForKey(key);
		Item item = mockItem();

		if(!valueType(key).isPrimitiveType()) {
			storage.setValue(item, key, null);

			assertEquals(noEntryValue(key), storage.getValue(item, key));
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testSetString() {
		String key = keyForType(ValueType.STRING);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.STRING)) {
			String value = (String) testValue(key);
			storage.setString(item, key, value);
			assertEquals(value, storage.getString(item, key));
		} else {
			assertUnsupportedType(() -> storage.setString(item, key, randomString(10)));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testSetStringNull() {
		String key = keyForType(ValueType.STRING);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.STRING)) {
			storage.setString(item, key, null);
			assertEquals(noEntryValue(key), storage.getString(item, key));
		} else {
			assertUnsupportedType(() -> storage.setString(item, key, null));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@Test
	default void testSetInteger() {
		String key = keyForType(ValueType.INTEGER);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.INTEGER)) {
			int value = ((Number) testValue(key)).intValue();
			storage.setInteger(item, key, value);
			assertEquals(value, storage.getInteger(item, key));
		} else {
			assertUnsupportedType(() -> storage.setInteger(item, key, random().nextInt()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@Test
	default void testSetLong() {
		String key = keyForType(ValueType.LONG);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.LONG)) {
			long value = ((Number) testValue(key)).longValue();
			storage.setLong(item, key, value);
			assertEquals(value, storage.getLong(item, key));
		} else {
			assertUnsupportedType(() -> storage.setLong(item, key, random().nextLong()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@Test
	default void testSetFloat() {
		String key = keyForType(ValueType.FLOAT);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.FLOAT)) {
			float value = ((Number) testValue(key)).floatValue();
			storage.setFloat(item, key, value);
			assertEquals(value, storage.getFloat(item, key));
		} else {
			assertUnsupportedType(() -> storage.setFloat(item, key,  random().nextFloat()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@Test
	default void testSetDouble() {
		String key = keyForType(ValueType.DOUBLE);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.DOUBLE)) {
			double value = ((Number) testValue(key)).doubleValue();
			storage.setDouble(item, key, value);
			assertEquals(value, storage.getDouble(item, key));
		} else {
			assertUnsupportedType(() -> storage.setDouble(item, key,  random().nextDouble()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testSetBoolean() {
		String key = keyForType(ValueType.BOOLEAN);
		S storage = createForKey(key);
		Item item = mockItem();

		if(typesForSetters(key).contains(ValueType.BOOLEAN)) {
			boolean value = ((Boolean) testValue(key)).booleanValue();
			storage.setBoolean(item, key, value);
			assertEquals(value, storage.getBoolean(item, key));
		} else {
			assertUnsupportedType(() -> storage.setBoolean(item, key,  random().nextBoolean()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Test
	default void testHasAnnotationsEmpty() {
		S storage = create();
		assertFalse(storage.hasAnnotations());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Test
	default void testHasAnnotations() {
		String key = key();
		S storage = createForKey(key);
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
		S storage = create();
		assertFalse(storage.hasAnnotations(mockItem()));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testHasAnnotationsItem() {
		String key = key();
		S storage = createForKey(key);
		Item item = mockItem();
		Object value = testValue(key);

		storage.setValue(item, key, value);

		assertTrue(storage.hasAnnotations(item));
		assertFalse(storage.hasAnnotations(mockItem()));
	}

}
