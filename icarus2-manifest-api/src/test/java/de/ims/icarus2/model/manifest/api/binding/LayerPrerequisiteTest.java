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
package de.ims.icarus2.model.manifest.api.binding;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerPrerequisiteTest<P extends LayerPrerequisite> extends GenericTest<P> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getLayerId()}.
	 */
	@Test
	default void testGetLayerId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getContextId()}.
	 */
	@Test
	default void testGetContextId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getTypeId()}.
	 */
	@Test
	default void testGetTypeId() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getAlias()}.
	 */
	@Test
	default void testGetAlias() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getMultiplicity()}.
	 */
	@Test
	default void testGetMultiplicity() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#defaultEquals(de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite, de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite)}.
	 */
	@Test
	default void testDefaultEquals() {
		fail("Not yet implemented"); // TODO
	}

}
