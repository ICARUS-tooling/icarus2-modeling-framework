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

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
public interface CategoryTest<C extends Category> extends IdentityTest<C> {

	C createWithNamespace(String namespace);

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.Category#getNamespace()}.
	 */
	@Test
	default void testGetNamespace() {
		C empty = createEmpty();
		if(empty!=null) {
			assertNull(empty.getNamespace());
		}

		C withNamespace = createWithNamespace("namespace");
		if(withNamespace!=null) {
			assertEquals("namespace", withNamespace.getNamespace());
		}
	}

}
