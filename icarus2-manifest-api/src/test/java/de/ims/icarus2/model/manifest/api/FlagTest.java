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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.util.Flag;

/**
 * Test case for evaluating enums implementing {@link Flag}.
 *
 * @author Markus Gärtner
 *
 */
public interface FlagTest<F extends Flag> {

	F[] createFlags();

	@Test
	default void testName() {
		F[] flags = createFlags();
		for(F flag : flags)
			assertNotNull(flag.name());
	}

	@Test
	default void testUniqueNames() {
		F[] flags = createFlags();
		Set<String> names = new HashSet<>();
		for(F flag : flags) {
			String name = flag.name();
			assertTrue(names.add(name), "Duplicate name: "+name);
		}
	}
}
