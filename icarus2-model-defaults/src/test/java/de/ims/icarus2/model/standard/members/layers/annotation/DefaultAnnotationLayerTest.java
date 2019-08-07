/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage;
import de.ims.icarus2.model.api.layer.AnnotationLayerTest;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;

/**
 * @author Markus Gärtner
 *
 */
class DefaultAnnotationLayerTest implements AnnotationLayerTest<DefaultAnnotationLayer> {

	@Override
	public Class<? extends DefaultAnnotationLayer> getTestTargetClass() {
		return DefaultAnnotationLayer.class;
	}

	@Override
	public DefaultAnnotationLayer createForManifest(AnnotationLayerManifest manifest) {
		return new DefaultAnnotationLayer(manifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.DefaultAnnotationLayer#DefaultAnnotationLayer(de.ims.icarus2.model.manifest.api.AnnotationLayerManifest)}.
	 */
	@Test
	void testDefaultAnnotationLayer() {
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);
		DefaultAnnotationLayer layer = new DefaultAnnotationLayer(manifest);
		assertSame(manifest, layer.getManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layers.annotation.DefaultAnnotationLayer#setAnnotationStorage(de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage)}.
	 */
	@Test
	void testSetAnnotationStorageManaged() {
		DefaultAnnotationLayer layer = create();

		AnnotationStorage storage = mock(AnnotationStorage.class);
		layer.setAnnotationStorage(storage);

		ManagedAnnotationStorage managedStorage = mock(ManagedAnnotationStorage.class);
		layer.setAnnotationStorage(managedStorage);
		verify(managedStorage).addNotify(layer);
		assertSame(managedStorage, layer.getAnnotationStorage());

		layer.setAnnotationStorage(storage);
		assertSame(storage, layer.getAnnotationStorage());

		verify(managedStorage).removeNotify(layer);
	}

}
