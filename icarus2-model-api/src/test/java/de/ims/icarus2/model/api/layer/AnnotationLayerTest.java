/**
 *
 */
package de.ims.icarus2.model.api.layer;

import static de.ims.icarus2.SharedTestUtils.mockSet;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.util.collections.set.DataSet;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationLayerTest<L extends AnnotationLayer>
		extends LayerTest<L, AnnotationLayerManifest> {


	@Override
	default AnnotationLayerManifest createManifest(String name) {
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);
		when(manifest.getName()).thenReturn(Optional.of(name));
		when(manifest.getUniqueId()).thenReturn(name);
		return manifest;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#getReferenceLayers()}.
	 */
	@Test
	default void testGetReferenceLayers() {
		assertTrue(create().getReferenceLayers().isEmpty());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#getAnnotationStorage()}.
	 */
	@Test
	default void testGetAnnotationStorage() {
		assertNull(create().getAnnotationStorage());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#setAnnotationStorage(de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage)}.
	 */
	@Test
	default void testSetAnnotationStorage() {
		AnnotationStorage storage = mock(AnnotationStorage.class);
		L layer = create();

		layer.setAnnotationStorage(storage);

		assertSame(storage, layer.getAnnotationStorage());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#setReferenceLayers(de.ims.icarus2.util.collections.set.DataSet)}.
	 */
	@Test
	default void testSetReferenceLayers() {
		L layer = create();
		DataSet<AnnotationLayer> referenceLayers = mockSet(mock(AnnotationLayer.class));

		layer.setReferenceLayers(referenceLayers);
		assertSame(referenceLayers, layer.getReferenceLayers());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#getValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testGetValueItemString() {
		L layer = create();
		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		Item item = mockItem();
		String key = randomString(5);

		layer.getValue(item, key);

		verify(storage).getValue(item, key);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#getValue(de.ims.icarus2.model.api.members.item.Item)}.
	 */
	@Test
	default void testGetValueItem() {
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);

		Item item = mockItem();
		String key = randomString(5);
		when(manifest.getDefaultKey()).thenReturn(Optional.of(key));

		L layer = createForManifest(manifest);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		layer.getValue(item);

		verify(storage).getValue(item, key);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#setValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String, java.lang.Object)}.
	 */
	@Test
	default void testSetValue() {
		L layer = create();
		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		Item item = mockItem();
		String key = randomString(5);
		Object value = new Object();

		layer.setValue(item, key, value);
		verify(storage).setValue(item, key, value);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#clearValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testClearValue() {
		Object noEntryValue = new Object();
		Item item = mockItem();
		String key = randomString(5);
		AnnotationManifest annotationManifest = mock(AnnotationManifest.class);
		when(annotationManifest.getNoEntryValue()).thenReturn(Optional.of(noEntryValue));
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);
		when(manifest.getAnnotationManifest(key)).thenReturn(Optional.of(annotationManifest));

		L layer = createForManifest(manifest);
		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		layer.clearValue(item, key);

		verify(storage).setValue(item, key, noEntryValue);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.api.layer.AnnotationLayer#clearValue(de.ims.icarus2.model.api.members.item.Item, java.lang.String)}.
	 */
	@Test
	default void testClearValueNoDefault() {
		L layer = create();
		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		Item item = mockItem();
		String key = randomString(5);

		layer.clearValue(item, key);

		verify(storage, never()).setValue(eq(item), eq(key), any());
	}

}
