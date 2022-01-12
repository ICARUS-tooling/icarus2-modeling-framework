/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api.binding;

import static de.ims.icarus2.test.TestUtils.assertNotPresent;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.assertPairwiseNotEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.Multiplicity;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerPrerequisiteTest<P extends LayerPrerequisite> extends GenericTest<P> {

	@Provider
	P createTestInstance(TestSettings settings,
			String alias, @Nullable String layerId,
			@Nullable String contextId, @Nullable String typeId,
			@Nullable String description, Multiplicity multiplicity);

	public static LayerPrerequisite mockLayerPrerequisite(
			String alias, String layerId, String contextId,
			String typeId, String description, Multiplicity multiplicity) {
		LayerPrerequisite p = mock(LayerPrerequisite.class);
		when(p.getAlias()).thenReturn(alias);
		when(p.getLayerId()).thenReturn(Optional.ofNullable(layerId));
		when(p.getContextId()).thenReturn(Optional.ofNullable(contextId));
		when(p.getTypeId()).thenReturn(Optional.ofNullable(typeId));
		when(p.getDescription()).thenReturn(Optional.ofNullable(description));
		when(p.getMultiplicity()).thenReturn(multiplicity);
		return p;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getAlias()}.
	 */
	@Test
	default void testGetAlias() {
		assertNotNull(create().getAlias());

		String alias = "alias1";
		assertEquals(alias, createTestInstance(settings(), alias,
				null, null, null, null, Multiplicity.ONE).getAlias());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getLayerId()}.
	 */
	@Test
	default void testGetLayerId() {
		assertNotPresent(create().getLayerId());

		String id = "layer1";
		assertOptionalEquals(id, createTestInstance(settings(), "alias",
				id, null, null, null, Multiplicity.ONE).getLayerId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getContextId()}.
	 */
	@Test
	default void testGetContextId() {
		assertNotPresent(create().getContextId());

		String id = "context1";
		assertOptionalEquals(id, createTestInstance(settings(), "alias",
				null, id, null, null, Multiplicity.ONE).getContextId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getTypeId()}.
	 */
	@Test
	default void testGetTypeId() {
		assertNotPresent(create().getTypeId());

		String id = "type1";
		assertOptionalEquals(id, createTestInstance(settings(), "alias",
				null, null, id, null, Multiplicity.ONE).getTypeId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getDescription()}.
	 */
	@Test
	default void testGetDescription() {
		assertNotPresent(create().getDescription());

		String desc = "some description";
		assertOptionalEquals(desc, createTestInstance(settings(), "alias",
				null, null, null, desc, Multiplicity.ONE).getDescription());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getMultiplicity()}.
	 */
	@Test
	default void testGetMultiplicity() {
		for(Multiplicity multiplicity : Multiplicity.values()) {
			assertEquals(multiplicity, createTestInstance(settings(), "alias",
					null, null, null, null, multiplicity).getMultiplicity());
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#defaultEquals(de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite, de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite)}.
	 */
	@Test
	default void testDefaultEquals() {
		LayerPrerequisite p1 = mockLayerPrerequisite("alias1", "layer1", "context1", "type1", "desc1", Multiplicity.ONE);
		LayerPrerequisite p2 = mockLayerPrerequisite("alias1", null, null, null, null, Multiplicity.ONE);
		LayerPrerequisite p3 = mockLayerPrerequisite("alias1", "layer1", null, null, null, Multiplicity.ONE);
		LayerPrerequisite p4 = mockLayerPrerequisite("alias1", null, "context1", null, null, Multiplicity.ONE);
		LayerPrerequisite p5 = mockLayerPrerequisite("alias1", null, null, "type1", null, Multiplicity.ONE);
		LayerPrerequisite p6 = mockLayerPrerequisite("alias1", null, null, null, "desc1", Multiplicity.ONE);

		LayerPrerequisite p1_2 = mockLayerPrerequisite("alias1", "layer1", "context1", "type1", "desc1", Multiplicity.ONE);

		assertTrue(LayerPrerequisite.defaultEquals(p1, p1));
		assertTrue(LayerPrerequisite.defaultEquals(p2, p2));
		assertTrue(LayerPrerequisite.defaultEquals(p3, p3));
		assertTrue(LayerPrerequisite.defaultEquals(p4, p4));
		assertTrue(LayerPrerequisite.defaultEquals(p5, p5));
		assertTrue(LayerPrerequisite.defaultEquals(p6, p6));

		assertTrue(LayerPrerequisite.defaultEquals(p1, p1_2));

		assertPairwiseNotEquals(LayerPrerequisite::defaultEquals,
				p1, p2, p3, p4, p5, p6);
	}

}
