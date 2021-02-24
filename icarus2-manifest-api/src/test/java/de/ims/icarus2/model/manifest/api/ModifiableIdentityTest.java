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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ModifiableIdentityTest<M extends ModifiableIdentity> extends IdentityTest<M> {

	@Override
	@Provider
	default M createFromIdentity(String id, String name, String description) {
		M identity = createEmpty();
		identity.setId(id);
		identity.setName(name);
		identity.setDescription(description);
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

		M fromIdentity = createFromIdentity("myId", "name", "description");
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

		M fromIdentity = createFromIdentity("myId", "name", "description");
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

		M fromIdentity = createFromIdentity("myId", "name", "description");
		if(fromIdentity!=null) {
			fromIdentity.setDescription("description");
			assertOptionalEquals("description", fromIdentity.getDescription());
		}
	}

}
