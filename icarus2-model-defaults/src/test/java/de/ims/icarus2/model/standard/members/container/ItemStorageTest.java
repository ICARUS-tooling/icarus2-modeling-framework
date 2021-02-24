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
package de.ims.icarus2.model.standard.members.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.container.ContainerEditVerifier;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemStorageTest<S extends ItemStorage> extends ApiGuardedTest<S> {

	/**
	 * Return expected container type or {@code null} if the implementation
	 * isn't bound to a single specific container type.
	 */
	@Provider
	ContainerType getExpectedContainerType();

	/**
	 * Mocks a {@link Container} usable for testing the current {@link ItemStorage}.
	 * The default implementation creates a mock with access to the underlying
	 * {@link ContainerManifest} and the associated {@link ItemLayerManifest}.
	 * The latter is configured to return {@link Optional#empty() empty} {@link Optional}
	 * objects for every method that uses {@link Optional} as a return type.
	 * <p>
	 * Subclasses should override this method to either adjust the returned container
	 * or provide an alternative construction.
	 * @return
	 */
	default Container createContainer() {
		ItemLayerManifest layerManifest = mock(ItemLayerManifest.class, inv -> {
			if(inv.getMethod().getReturnType()==Optional.class) {
				return Optional.empty();
			}
			return null;
		});
		ContainerManifest manifest = mock(ContainerManifest.class);
		when(manifest.getLayerManifest()).thenReturn(Optional.of(layerManifest));
		Container container = mock(Container.class);
		when(container.getManifest()).then(inv -> manifest);

		return container;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#addNotify(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testAddNotify() {
		S storage = create();
		Container container = createContainer();
		storage.addNotify(container);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#removeNotify(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testRemoveNotify() {
		S storage = create();
		Container container = createContainer();
		storage.addNotify(container);
		storage.removeNotify(container);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#getContainerType()}.
	 */
	@Test
	default void testGetContainerType() {
		ContainerType expectedType = getExpectedContainerType();
		if(expectedType!=null) {
			assertEquals(expectedType, create().getContainerType());
		} else {
			assertNotNull(create().getContainerType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#createEditVerifier(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testCreateEditVerifier() {
		S storage = create();
		Container container = createContainer();
		storage.addNotify(container);

		try(ContainerEditVerifier verifier = storage.createEditVerifier(container)) {
			assertNotNull(verifier);
			assertSame(container, verifier.getSource());
		}

	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#recycle()}.
	 */
	@Test
	default void testRecycle() {
		create().recycle();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.container.ItemStorage#isDirty(de.ims.icarus2.model.api.members.container.Container)}.
	 */
	@Test
	default void testIsDirty() {
		S storage = create();
		Container container = createContainer();
		storage.addNotify(container);

		assertFalse(storage.isDirty(container));
	}

}
