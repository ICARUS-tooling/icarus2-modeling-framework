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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface DriverManifestTest<M extends DriverManifest> extends ForeignImplementationManifestTest<M>, EmbeddedTest<M> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 *
	 * @see MemberManifestTest#createTestInstance(TestSettings)
	 */
	@Provider
	@Override
	default M createTestInstance(TestSettings settings) {
		return ForeignImplementationManifestTest.super.createTestInstance(settings);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachMappingManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachMappingManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalMappingManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalMappingManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getMappingManifests()}.
	 */
	@Test
	default void testGetMappingManifests() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalMappingManifests()}.
	 */
	@Test
	default void testGetLocalMappingManifests() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getMappingManifest(java.lang.String)}.
	 */
	@Test
	default void testGetMappingManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocationType()}.
	 */
	@Test
	default void testGetLocationType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#isLocalLocationType()}.
	 */
	@Test
	default void testIsLocalLocationType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachModuleManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachModuleManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalModuleManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests()}.
	 */
	@Test
	default void testGetModuleManifests() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalModuleManifests()}.
	 */
	@Test
	default void testGetLocalModuleManifests() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachModuleSpec(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleSpec(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpecs()}.
	 */
	@Test
	default void testGetModuleSpecs() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalModuleSpecs()}.
	 */
	@Test
	default void testGetLocalModuleSpecs() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpec(java.lang.String)}.
	 */
	@Test
	default void testGetModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests(java.lang.String)}.
	 */
	@Test
	default void testGetModuleManifestsString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testGetModuleManifestsModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)}.
	 */
	@Test
	default void testAddMappingManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)}.
	 */
	@Test
	default void testRemoveMappingManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest)}.
	 */
	@Test
	default void testAddModuleManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest)}.
	 */
	@Test
	default void testRemoveModuleManifest() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addModuleSpec(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testAddModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleSpec(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testRemoveModuleSpec() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#setLocationType(de.ims.icarus2.model.manifest.api.LocationType)}.
	 */
	@Test
	default void testSetLocationType() {
		fail("Not yet implemented");
	}

}
