/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static org.mockito.Mockito.mock;

import javax.swing.Icon;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ModifiableIdentityTest<M extends ModifiableIdentity> extends IdentityTest<M> {

	@Override
	@Provider
	default M createFromIdentity(String id, String name, String description, Icon icon) {
		M identity = createEmpty();
		identity.setId(id);
		identity.setName(name);
		identity.setDescription(description);
		identity.setIcon(icon);
		return identity;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setId(java.lang.String)}.
	 */
	@Test
	default void testSetId() {
		M empty = createEmpty();
		if(empty!=null) {
			assertNPE(() -> empty.setId(null));

			empty.setId("myId");
			assertOptionalEquals("myId", empty.getId());
		}

		Icon icon = mock(Icon.class);
		M fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setId("myId");
			assertOptionalEquals("myId", fromIdentity.getId());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setName(java.lang.String)}.
	 */
	@Test
	default void testSetName() {
		M empty = createEmpty();
		if(empty!=null) {
			empty.setName("name");
			assertOptionalEquals("name", empty.getName());
		}

		Icon icon = mock(Icon.class);
		M fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setName("name");
			assertOptionalEquals("name", fromIdentity.getName());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setDescription(java.lang.String)}.
	 */
	@Test
	default void testSetDescription() {
		M empty = createEmpty();
		if(empty!=null) {
			empty.setDescription("description");
			assertOptionalEquals("description", empty.getDescription());
		}

		Icon icon = mock(Icon.class);
		M fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setDescription("description");
			assertOptionalEquals("description", fromIdentity.getDescription());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ModifiableIdentity#setIcon(javax.swing.Icon)}.
	 */
	@Test
	default void testSetIcon() {
		Icon icon = mock(Icon.class);

		M empty = createEmpty();
		if(empty!=null) {
			empty.setIcon(icon);
			assertOptionalEquals(icon, empty.getIcon());
		}

		Icon icon2 = mock(Icon.class);
		M fromIdentity = createFromIdentity("myId", "name", "description", icon);
		if(fromIdentity!=null) {
			fromIdentity.setIcon(icon2);
			assertOptionalEquals(icon2, fromIdentity.getIcon());
		}
	}

}
