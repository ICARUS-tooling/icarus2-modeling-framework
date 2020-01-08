/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.id;

import static de.ims.icarus2.test.TestUtils.assertPresent;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface IdentityTest<I extends Identity> extends GenericTest<I> {

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getId()}.
	 */
	@Test
	default void testGetId() {
		I instance = create();
		assertNotNull(instance.getId());
		assertPresent(instance.getId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getName()}.
	 */
	@Test
	default void testGetName() {
		assertNotNull(create().getName());
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.id.Identity#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		assertNotNull(create().getDescription());
	}

}
