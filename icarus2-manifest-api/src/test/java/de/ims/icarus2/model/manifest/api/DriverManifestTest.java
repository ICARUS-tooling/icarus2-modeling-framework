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
package de.ims.icarus2.model.manifest.api;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubIdentity;
import static de.ims.icarus2.test.TestUtils.IGNORE_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_ILLEGAL;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.other;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.test.TestUtils.unwrapGetter;
import static de.ims.icarus2.test.TestUtils.wrap_batchConsumer;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;

/**
 * @author Markus Gärtner
 *
 */
public interface DriverManifestTest
		extends ForeignImplementationManifestTest<DriverManifest>, EmbeddedMemberManifestTest<DriverManifest> {

	public static ModuleManifest mockModuleManifest(String id) {
		return (ModuleManifest)stubId(mockTypedManifest(ManifestType.MODULE_MANIFEST), id);
	}

	public static ModuleSpec mockModuleSpec(String id) {
		return stubIdentity(mockTypedManifest(ManifestType.MODULE_SPEC), id);
	}

	public static MappingManifest mockMappingManifest(String id) {
		MappingManifest manifest = mockTypedManifest(ManifestType.MAPPING_MANIFEST);
		when(manifest.getId()).thenReturn(Optional.of(id));
		return manifest;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.TypedManifestTest#getExpectedType()
	 */
	@Override
	default ManifestType getExpectedType() {
		return ManifestType.DRIVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return singleton(ManifestType.CONTEXT_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachMappingManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachMappingManifest() {
		EmbeddedMemberManifestTest.super.<MappingManifest>assertDerivativeForEach(settings(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				DriverManifest::forEachMappingManifest,
				DriverManifest::addMappingManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalMappingManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalMappingManifest() {
		EmbeddedMemberManifestTest.super.<MappingManifest>assertDerivativeForEachLocal(settings(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				DriverManifest::forEachLocalMappingManifest,
				DriverManifest::addMappingManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getMappingManifests()}.
	 */
	@Test
	default void testGetMappingManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				DriverManifest::getMappingManifests,
				DriverManifest::addMappingManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalMappingManifests()}.
	 */
	@Test
	default void testGetLocalMappingManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				DriverManifest::getLocalMappingManifests,
				DriverManifest::addMappingManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getMappingManifest(java.lang.String)}.
	 */
	@Test
	default void testGetMappingManifest() {
		assertDerivativeAccumulativeOptLookup(settings(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				DriverManifest::getMappingManifest,
				NPE_CHECK,
				DriverManifest::addMappingManifest,
				unwrapGetter(MappingManifest::getId),
				"mapping3");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocationType()}.
	 */
	@Test
	default void testGetLocationType() {
		for(LocationType locationType : LocationType.values()) {
			assertDerivativeOptGetter(settings(),
					locationType, other(locationType),
					IGNORE_DEFAULT(),
					DriverManifest::getLocationType,
					DriverManifest::setLocationType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#isLocalLocationType()}.
	 */
	@Test
	default void testIsLocalLocationType() {
		for(LocationType locationType : LocationType.values()) {
			assertDerivativeIsLocal(settings(),
					locationType, other(locationType),
					DriverManifest::isLocalLocationType,
					DriverManifest::setLocationType);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		assertHostGetter(DriverManifest::getContextManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachModuleManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachModuleManifest() {
		EmbeddedMemberManifestTest.super.<ModuleManifest>assertDerivativeForEach(settings(),
				mockModuleManifest("module1"),
				mockModuleManifest("module2"),
				DriverManifest::forEachModuleManifest,
				DriverManifest::addModuleManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleManifest(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalModuleManifest() {
		EmbeddedMemberManifestTest.super.<ModuleManifest>assertDerivativeForEachLocal(settings(),
				mockModuleManifest("module1"),
				mockModuleManifest("module2"),
				DriverManifest::forEachLocalModuleManifest,
				DriverManifest::addModuleManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests()}.
	 */
	@Test
	default void testGetModuleManifests() {
		assertDerivativeAccumulativeGetter(settings(),
				mockModuleManifest("module1"),
				mockModuleManifest("module2"),
				DriverManifest::getModuleManifests,
				DriverManifest::addModuleManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalModuleManifests()}.
	 */
	@Test
	default void testGetLocalModuleManifests() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockModuleManifest("module1"),
				mockModuleManifest("module2"),
				DriverManifest::getLocalModuleManifests,
				DriverManifest::addModuleManifest);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachModuleSpec(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachModuleSpec() {
		EmbeddedMemberManifestTest.super.<ModuleSpec>assertDerivativeForEach(settings(),
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"),
				DriverManifest::forEachModuleSpec,
				DriverManifest::addModuleSpec);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#forEachLocalModuleSpec(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachLocalModuleSpec() {
		EmbeddedMemberManifestTest.super.<ModuleSpec>assertDerivativeForEachLocal(settings(),
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"),
				DriverManifest::forEachLocalModuleSpec,
				DriverManifest::addModuleSpec);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpecs()}.
	 */
	@Test
	default void testGetModuleSpecs() {
		assertDerivativeAccumulativeGetter(settings(),
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"),
				DriverManifest::getModuleSpecs,
				DriverManifest::addModuleSpec);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getLocalModuleSpecs()}.
	 */
	@Test
	default void testGetLocalModuleSpecs() {
		assertDerivativeAccumulativeLocalGetter(settings(),
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"),
				DriverManifest::getLocalModuleSpecs,
				DriverManifest::addModuleSpec);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleSpec(java.lang.String)}.
	 */
	@Test
	default void testGetModuleSpec() {
		assertDerivativeAccumulativeOptLookup(settings(),
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"),
				DriverManifest::getModuleSpec,
				NPE_CHECK,
				DriverManifest::addModuleSpec,
				unwrapGetter(ModuleSpec::getId),
				"spec3");
	}

	public static Collection<ModuleManifest> mockModuleManifests(String id, int count) {
		assertTrue(count>1);

		Set<ModuleManifest> result = new HashSet<>();
		while(count-->0) {
			ModuleManifest module = mockModuleManifest(id);
			result.add(module);
		}

		return result;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests(java.lang.String)}.
	 */
	@Test
	default void testGetModuleManifestsString() {

		Function<Collection<ModuleManifest>, String> keyGen =
				items -> items.iterator().next().getId().get();

		assertDerivativeAccumulativeLookup(settings(),
				mockModuleManifests("group1", 3),
				mockModuleManifests("group2", 4),
				DriverManifest::getModuleManifests,
				NPE_CHECK, NO_CHECK,
				wrap_batchConsumer(DriverManifest::addModuleManifest),
				keyGen);
	}

	public static Collection<ModuleManifest> mockModuleManifests(ModuleSpec spec, int count) {
		assertTrue(count>1);

		Collection<ModuleManifest> result = new HashSet<>();
		while(count-->0) {
			ModuleManifest module = mockModuleManifest(spec.getId()+"_module"+count);
			when(module.getModuleSpec()).thenReturn(Optional.of(spec));
			result.add(module);
		}

		return result;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#getModuleManifests(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testGetModuleManifestsModuleSpec() {
		ModuleSpec spec1 = mockModuleSpec("spec1");
		ModuleSpec spec2 = mockModuleSpec("spec2");

		Function<Collection<ModuleManifest>, ModuleSpec> keyGen =
				items -> items.iterator().next().getModuleSpec().orElseThrow(AssertionError::new);

		assertDerivativeAccumulativeLookup(settings(),
				mockModuleManifests(spec1, 3),
				mockModuleManifests(spec2, 4),
				DriverManifest::getModuleManifests,
				NPE_CHECK, NO_CHECK,
				wrap_batchConsumer(DriverManifest::addModuleManifest),
				keyGen);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)}.
	 */
	@Test
	default void testAddMappingManifest() {
		assertLockableAccumulativeAdd(settings(),
				DriverManifest::addMappingManifest,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeMappingManifest(de.ims.icarus2.model.manifest.api.MappingManifest)}.
	 */
	@Test
	default void testRemoveMappingManifest() {
		assertLockableAccumulativeRemove(settings(),
				DriverManifest::addMappingManifest,
				DriverManifest::removeMappingManifest,
				DriverManifest::getMappingManifests,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addModuleManifest(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest)}.
	 */
	@Test
	default void testAddModuleManifest() {
		assertLockableAccumulativeAdd(settings(),
				DriverManifest::addModuleManifest,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockModuleManifest("module1"),
				mockModuleManifest("module1"),
				mockModuleManifest("module2"),
				mockModuleManifest("module3"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleManifest(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest)}.
	 */
	@Test
	default void testRemoveModuleManifest() {
		assertLockableAccumulativeRemove(settings(),
				DriverManifest::addModuleManifest,
				DriverManifest::removeModuleManifest,
				DriverManifest::getModuleManifests,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockModuleManifest("module1"),
				mockModuleManifest("module2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#addModuleSpec(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testAddModuleSpec() {
		assertLockableAccumulativeAdd(settings(),
				DriverManifest::addModuleSpec,
				NO_ILLEGAL(), NO_CHECK, NPE_CHECK, ManifestTestUtils.DUPLICATE_ID_CHECK,
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#removeModuleSpec(de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec)}.
	 */
	@Test
	default void testRemoveModuleSpec() {
		assertLockableAccumulativeRemove(settings(),
				DriverManifest::addModuleSpec,
				DriverManifest::removeModuleSpec,
				DriverManifest::getModuleSpecs,
				NPE_CHECK, ManifestTestUtils.UNKNOWN_ID_CHECK,
				mockModuleSpec("spec1"),
				mockModuleSpec("spec2"));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.DriverManifest#setLocationType(de.ims.icarus2.model.manifest.api.LocationType)}.
	 */
	@Test
	default void testSetLocationType() {
		for(LocationType locationType : LocationType.values()) {
			assertLockableSetter(settings(),
					DriverManifest::setLocationType,
					locationType, NPE_CHECK, NO_CHECK);
		}
	}

}
