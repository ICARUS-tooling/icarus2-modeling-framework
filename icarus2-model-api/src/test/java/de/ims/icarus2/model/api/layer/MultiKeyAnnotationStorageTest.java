/**
 *
 */
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
public interface MultiKeyAnnotationStorageTest<S extends AnnotationStorage>
		extends AnnotationStorageTest<S> {

	/** Creates a list of distinct keys */
	List<String> keys();

	default AnnotationLayerManifest createManifest(List<String> keys) {
		AnnotationLayerManifest manifest = mockTypedManifest(AnnotationLayerManifest.class);
		when(manifest.getAvailableKeys()).thenReturn(new HashSet<>(keys));

		Set<AnnotationManifest> annotationManifests = new HashSet<>();

		for(String key : keys) {
			AnnotationManifest annotationManifest = mock(AnnotationManifest.class);
			when(annotationManifest.getKey()).thenReturn(Optional.of(key));
			when(annotationManifest.getNoEntryValue()).thenReturn(
					Optional.ofNullable(noEntryValue(key)));
			when(manifest.getAnnotationManifest(key)).thenReturn(
					Optional.of(annotationManifest));

			annotationManifests.add(annotationManifest);
		}

		when(manifest.getAnnotationManifests()).thenReturn(annotationManifests);

		return manifest;
	}

	@Override
	default AnnotationLayer createLayer(AnnotationLayerManifest manifest) {
		AnnotationLayer layer = mock(AnnotationLayer.class);
		when(layer.getManifest()).thenReturn(manifest);
		return layer;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default S createTestInstance(TestSettings settings) {
		return settings.process(createForLayer(createLayer(createManifest(keys()))));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#collectKeys(de.ims.icarus2.model.api.members.item.Item, java.util.function.Consumer)}.
	 */
	@Override
	@Test
	default void testCollectKeys() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getString(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#getBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Override
	@Test
	default void testGetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.function.Supplier)}.
	 */
	@Override
	@Test
	default void testRemoveAllValuesSupplierOfQextendsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#removeAllValues(java.util.Iterator)}.
	 */
	@Override
	@Test
	default void testRemoveAllValuesIteratorOfQextendsItem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Override
	@Test
	default void testSetValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setString(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.String)}.
	 */
	@Override
	@Test
	default void testSetString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setInteger(de.ims.icarus2.model.api.members.item.Item, java.lang.String, int)}.
	 */
	@Override
	@Test
	default void testSetIntegerValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setLong(de.ims.icarus2.model.api.members.item.Item, java.lang.String, long)}.
	 */
	@Override
	@Test
	default void testSetLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setFloat(de.ims.icarus2.model.api.members.item.Item, java.lang.String, float)}.
	 */
	@Override
	@Test
	default void testSetFloatValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setDouble(de.ims.icarus2.model.api.members.item.Item, java.lang.String, double)}.
	 */
	@Override
	@Test
	default void testSetDoubleValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#setBoolean(de.ims.icarus2.model.api.members.item.Item, java.lang.String, boolean)}.
	 */
	@Override
	@Test
	default void testSetBooleanValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations()}.
	 */
	@Override
	@Test
	default void testHasAnnotations() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage#hasAnnotations(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Override
	@Test
	default void testHasAnnotationsItem() {
		fail("Not yet implemented"); // TODO
	}

}
