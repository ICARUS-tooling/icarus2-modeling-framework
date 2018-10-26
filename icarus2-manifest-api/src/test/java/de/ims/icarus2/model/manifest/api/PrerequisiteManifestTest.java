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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.binding.LayerPrerequisiteTest;

/**
 * @author Markus Gärtner
 *
 */
public interface PrerequisiteManifestTest<M extends PrerequisiteManifest> extends
		LayerPrerequisiteTest<M>, LockableTest<M> {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getMultiplicity()}.
	 */
	@Override
	@Test
	default void testGetMultiplicity() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getUnresolvedForm()}.
	 */
	@Test
	default void testGetUnresolvedForm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setDescription(java.lang.String)}.
	 */
	@Test
	default void testSetDescription() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetLayerId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setTypeId(java.lang.String)}.
	 */
	@Test
	default void testSetTypeId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setContextId(java.lang.String)}.
	 */
	@Test
	default void testSetContextId() {
		fail("Not yet implemented");
	}

}
