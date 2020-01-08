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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.assertMock;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.inject_genericSetter;
import static de.ims.icarus2.test.TestUtils.other;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.ManifestTestUtils;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;

/**
 * @author Markus Gärtner
 *
 */
public interface MappingManifestTest
	extends ManifestFragmentTest<MappingManifest>, EmbeddedTest<MappingManifest> {

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
		return ManifestType.MAPPING_MANIFEST;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return set(ManifestType.DRIVER_MANIFEST);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getDriverManifest()}.
	 */
	@Test
	default void testGetDriverManifest() {
		assertNotNull(createUnlocked().getDriverManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getId()}.
	 */
	@Override
	@Test
	default void testGetId() {
		assertOptGetter(createUnlocked(),
				"id1", "id2", null,
				MappingManifest::getId,
				MappingManifest::setId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getSourceLayerId()}.
	 */
	@Test
	default void testGetSourceLayerId() {
		assertOptGetter(createUnlocked(),
				"layer1", "layer2", null,
				MappingManifest::getSourceLayerId,
				MappingManifest::setSourceLayerId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getTargetLayerId()}.
	 */
	@Test
	default void testGetTargetLayerId() {
		assertOptGetter(createUnlocked(),
				"layer1", "layer2", null,
				MappingManifest::getTargetLayerId,
				MappingManifest::setTargetLayerId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getRelation()}.
	 */
	@Test
	default void testGetRelation() {
		for(Relation relation : Relation.values()) {
			assertOptGetter(createUnlocked(),
					relation, other(relation), null,
					MappingManifest::getRelation, MappingManifest::setRelation);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getCoverage()}.
	 */
	@Test
	default void testGetCoverage() {
		for(Coverage coverage : Coverage.values()) {
			assertOptGetter(createUnlocked(),
					coverage, other(coverage), null,
					MappingManifest::getCoverage, MappingManifest::setCoverage);
		}
	}

	public static <M extends MappingManifest, L extends MappingManifest> BiConsumer<M, L> inject_mappingLookup(
			BiConsumer<M, L> setter) {
		return (m, mappingManifest) -> {

			String id = mappingManifest.getId().orElseThrow(AssertionError::new);

			DriverManifest driverManifest = assertMock(m.getDriverManifest());
			when(driverManifest.getMappingManifest(id)).thenReturn(Optional.of(mappingManifest));

			setter.accept(m, mappingManifest);
		};
	}

	/**
	 * Helper function to be used for consistency.
	 * Transforms a {@link MappingManifest} into a {@link String} by using
	 * its {@link MappingManifest#getId() id}.
	 */
	public static <I extends MappingManifest> Function<I, String> transform_id(){
		return i -> i.getId().orElseThrow(AssertionError::new);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getInverse()}.
	 */
	@Test
	default void testGetInverse() {
		assertOptGetter(createUnlocked(),
				mockMappingManifest("mapping1"),
				mockMappingManifest("mapping2"),
				null,
				MappingManifest::getInverse,
				inject_mappingLookup(inject_genericSetter(
						MappingManifest::setInverseId, transform_id())));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setSourceLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetSourceLayerId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setSourceLayerId,
				getLegalIdValues(),
				true, ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setTargetLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetTargetLayerId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setTargetLayerId,
				getLegalIdValues(),
				true, ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setRelation(de.ims.icarus2.model.manifest.api.MappingManifest.Relation)}.
	 */
	@Test
	default void testSetRelation() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setRelation,
				Relation.values(), true, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setCoverage(de.ims.icarus2.model.manifest.api.MappingManifest.Coverage)}.
	 */
	@Test
	default void testSetCoverage() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setCoverage,
				Coverage.values(), true, NO_CHECK);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setInverseId(java.lang.String)}.
	 */
	@Test
	default void testSetInverseId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setInverseId,
				getLegalIdValues(),
				true, ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setId(java.lang.String)}.
	 */
	@Test
	default void testSetId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setId,
				getLegalIdValues(),
				true, ManifestTestUtils.INVALID_ID_CHECK, getIllegalIdValues());
	}

}
