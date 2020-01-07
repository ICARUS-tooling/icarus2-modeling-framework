/**
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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.test.TestUtils.assertNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface PathEntryTest {

	@Provider
	PathEntry createInstance(PathType type, String value);

	@Test
	default void testCreation() {
		assertNPE(() -> createInstance(null, "value"));
		assertNPE(() -> createInstance(PathType.FILE, null));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry#getType()}.
	 */
	@Test
	default void testGetType() {
		for(PathType pathType : PathType.values()) {
			assertEquals(pathType, createInstance(pathType, "value").getType());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry#getValue()}.
	 */
	@Test
	default void testGetValue() {
		String[] values = {"", "value", TestUtils.LOREM_IPSUM_CHINESE, TestUtils.EMOJI};

		for(String value : values) {
			assertEquals(value, createInstance(PathType.CUSTOM, value).getValue());
		}
	}

}
