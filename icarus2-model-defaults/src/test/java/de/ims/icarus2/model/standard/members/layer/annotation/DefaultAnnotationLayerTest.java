/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.layer.AnnotationLayerTest;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorage;
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
	 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.DefaultAnnotationLayer#DefaultAnnotationLayer(de.ims.icarus2.model.manifest.api.AnnotationLayerManifest)}.
	 */
	@Test
	void testDefaultAnnotationLayer() {
		AnnotationLayerManifest manifest = mock(AnnotationLayerManifest.class);
		DefaultAnnotationLayer layer = new DefaultAnnotationLayer(manifest);
		assertSame(manifest, layer.getManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.DefaultAnnotationLayer#setAnnotationStorage(de.ims.icarus2.model.api.layer.AnnotationLayer.AnnotationStorage)}.
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
