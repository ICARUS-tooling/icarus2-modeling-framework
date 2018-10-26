/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.mockito.Mockito.mock;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public interface IdentityTest<I extends Identity> extends GenericTest<I> {

	default I createEmpty() {
		return createTestInstance(settings());
	}

	@Provider
	I createFromIdentity(String id, String name, String description, Icon icon);

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getId()}.
	 */
	@Test
	default void testGetId() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNotPresent(empty.getId());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertOptionalEquals("myId", fromIdentity.getId());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getName()}.
	 */
	@Test
	default void testGetName() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNotPresent(empty.getName());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertOptionalEquals("name", fromIdentity.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNotPresent(empty.getDescription());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertOptionalEquals("description", fromIdentity.getDescription());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getIcon()}.
	 */
	@Test
	default void testGetIcon() {
		I empty = createEmpty();
		if(empty!=null) {
			assertNotPresent(empty.getIcon());
		}

		Icon icon = mock(Icon.class);
		I fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertOptionalEquals(icon, fromIdentity.getIcon());
		}
	}

}
