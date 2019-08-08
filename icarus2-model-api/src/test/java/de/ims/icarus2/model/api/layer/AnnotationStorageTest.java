/**
 *
 */
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getTestValue;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationStorageTest<S extends AnnotationStorage>
		extends ApiGuardedTest<S> {

	/** The main value type for the storage under test */
	ValueType getValueType();

	/** Types supported for setting values on the storage */
	Set<ValueType> typesForSetters();

	/** Types supported when fetching values from the storage */
	Set<ValueType> typesForGetters();

	/** Creates a list of distinct keys */
	List<String> keys();

	default String key() {
		return "test";
	}

	default Object noEntryValue(String key) {
		return null;
	}

	default AnnotationLayerManifest createManifest(String key) {
		AnnotationLayerManifest manifest = mockTypedManifest(AnnotationLayerManifest.class);
		when(manifest.getAvailableKeys()).thenReturn(singleton(key));

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
		Object value = getTestValue(getValueType());

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

		} else {
			assertUnsupportedType(() -> storage.getString(item, key));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)}.
	 */
	@Test
	default void testRemoveAllValuesSupplierOfQextendsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.Iterator)}.
	 */
	@Test
	default void testRemoveAllValuesIteratorOfQextendsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)}.
	 */
	@Test
	default void testSetString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setIntegerValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@Test
	default void testSetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLongValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@Test
	default void testSetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloatValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@Test
	default void testSetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDoubleValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@Test
	default void testSetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBooleanValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@Test
	default void testSetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Test
	default void testHasAnnotations() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testHasAnnotationsItem() {
		fail("Not yet implemented"); // TODO
	}

}
