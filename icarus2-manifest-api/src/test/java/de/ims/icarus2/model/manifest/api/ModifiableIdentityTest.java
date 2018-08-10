/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface ModifiableIdentityTest {

	ModifiableIdentity createEmpty();

	default ModifiableIdentity createFromIdentity(String id, String name, String description, Icon icon) {
		ModifiableIdentity identity = createEmpty();
		identity.setId(id);
		identity.setName(name);
		identity.setDescription(description);
		identity.setIcon(icon);
		return identity;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getId()}.
	 */
	@Test
	default void testGetId() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getId());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("myId", fromIdentity.getId());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getName()}.
	 */
	@Test
	default void testGetName() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getName());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("name", fromIdentity.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getDescription());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertEquals("description", fromIdentity.getDescription());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#getIcon()}.
	 */
	@Test
	default void testGetIcon() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getIcon());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			assertSame(icon, fromIdentity.getIcon());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setId(java.lang.String)}.
	 */
	@Test
	default void testSetId() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			empty.setId("myId");
			assertEquals("myId", empty.getId());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setId("myId");
			assertEquals("myId", fromIdentity.getId());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setName(java.lang.String)}.
	 */
	@Test
	default void testSetName() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			empty.setName("name");
			assertEquals("name", empty.getName());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setName("name");
			assertEquals("name", fromIdentity.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setDescription(java.lang.String)}.
	 */
	@Test
	default void testSetDescription() {
		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			empty.setDescription("description");
			assertEquals("description", empty.getDescription());
		}

		Icon icon = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setDescription("description");
			assertEquals("description", fromIdentity.getDescription());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setIcon(javax.swing.Icon)}.
	 */
	@Test
	default void testSetIcon() {
		Icon icon = mock(Icon.class);

		ModifiableIdentity empty = createEmpty();
		if(empty!=null) {
			empty.setIcon(icon);
			assertEquals(icon, empty.getIcon());
		}

		Icon icon2 = mock(Icon.class);
		ModifiableIdentity fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setIcon(icon2);
			assertEquals(icon2, fromIdentity.getIcon());
		}
	}

}
