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

import static de.ims.icarus2.model.manifest.ManifestTestUtils.getIllegalIdValues;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.getLegalIdValues;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;

/**
 * @author Markus Gärtner
 *
 */
public interface MappingManifestTest<M extends MappingManifest> extends TypedManifestTest<M>, LockableTest<M> {

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
	@Test
	default void testGetId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getSourceLayerId()}.
	 */
	@Test
	default void testGetSourceLayerId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getTargetLayerId()}.
	 */
	@Test
	default void testGetTargetLayerId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getRelation()}.
	 */
	@Test
	default void testGetRelation() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getCoverage()}.
	 */
	@Test
	default void testGetCoverage() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#getInverse()}.
	 */
	@Test
	default void testGetInverse() {
		assertg
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setSourceLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetSourceLayerId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setSourceLayerId,
				getLegalIdValues(),
				true, INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setTargetLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetTargetLayerId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setTargetLayerId,
				getLegalIdValues(),
				true, INVALID_ID_CHECK, getIllegalIdValues());
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
				true, INVALID_ID_CHECK, getIllegalIdValues());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.MappingManifest#setId(java.lang.String)}.
	 */
	@Test
	default void testSetId() {
		assertLockableSetterBatch(settings(),
				MappingManifest::setId,
				getLegalIdValues(),
				true, INVALID_ID_CHECK, getIllegalIdValues());
	}

}
