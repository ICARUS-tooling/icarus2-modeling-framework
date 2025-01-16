/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.assertOptGetter;
import static de.ims.icarus2.test.TestUtils.assertOptionalEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.binding.LayerPrerequisiteTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.Multiplicity;

/**
 * @author Markus Gärtner
 *
 */
public interface PrerequisiteManifestTest extends
	EmbeddedTest<PrerequisiteManifest>, LayerPrerequisiteTest<PrerequisiteManifest>, LockableTest<PrerequisiteManifest> {

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#getAllowedHostTypes()
	 */
	@Override
	default Set<ManifestType> getAllowedHostTypes() {
		return singleton(ManifestType.CONTEXT_MANIFEST);
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default PrerequisiteManifest createTestInstance(TestSettings settings) {
		return createTestInstance(settings, "randomAlias");
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.EmbeddedTest#createEmbedded(de.ims.icarus2.test.TestSettings, de.ims.icarus2.model.manifest.api.TypedManifest)
	 */
	@Override
	default PrerequisiteManifest createEmbedded(TestSettings settings, TypedManifest host) {
		return createEmbedded(settings, host, "randomAlias");
	}

	@Provider
	PrerequisiteManifest createEmbedded(TestSettings settings, TypedManifest host, String alias);

	@Provider
	PrerequisiteManifest createTestInstance(TestSettings settings,
			String alias);

	/**
	 * @see de.ims.icarus2.model.manifest.api.binding.LayerPrerequisiteTest#createTestInstance(de.ims.icarus2.test.TestSettings, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.util.Multiplicity)
	 */
	@Provider
	@Override
	default PrerequisiteManifest createTestInstance(TestSettings settings,
			String alias, @Nullable String layerId,
			@Nullable String contextId, @Nullable String typeId,
			@Nullable String description, Multiplicity multiplicity) {
		PrerequisiteManifest instance = createTestInstance(settings, alias);
		if(layerId!=null) {
			instance.setLayerId(layerId);
		}
		if(contextId!=null) {
			instance.setContextId(contextId);
		}
		if(typeId!=null) {
			instance.setTypeId(typeId);
		}
		if(description!=null) {
			instance.setDescription(description);
		}
		return instance;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getMultiplicity()}.
	 */
	@Override
	@Test
	default void testGetMultiplicity() {
		// The current contract for prerequisite manifests restricts the multiplicity to one
		assertEquals(Multiplicity.ONE, create().getMultiplicity());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getContextManifest()}.
	 */
	@Test
	default void testGetContextManifest() {
		assertNotNull(create().getContextManifest());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#getUnresolvedForm()}.
	 */
	@SuppressWarnings("boxing")
	@Test
	default void testGetUnresolvedForm() {
		/*
		 * Scenario (bracketed manifests are mocked):
		 *
		 * (context1) -> (prerequisite1)
		 *
		 * (context2) -> prerequisite2
		 *
		 * with context2 using context1 as template and
		 * prerequisite1 being the resolved form of
		 * prerequisite1 declared in context1
		 */

		String alias = "alias1";
		PrerequisiteManifest prerequisite1 = mock(PrerequisiteManifest.class);
		when(prerequisite1.getAlias()).thenReturn(alias);

		ContextManifest context1 = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		when(context1.getPrerequisite(anyString())).thenReturn(Optional.empty());
		when(context1.getPrerequisite(alias)).thenReturn(Optional.of(prerequisite1));

		ContextManifest context2 = mockTypedManifest(ManifestType.CONTEXT_MANIFEST);
		when(context2.hasTemplate()).thenReturn(Boolean.TRUE);
		when(context2.getTemplate()).thenReturn(context1);

		PrerequisiteManifest prerequisite2 = createEmbedded(settings(), context2, alias);

		assertOptionalEquals(prerequisite1, prerequisite2.getUnresolvedForm());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setDescription(java.lang.String)}.
	 */
	@Test
	default void testSetDescription() {
		// Check both getter and setter in one go
		assertOptGetter(create(),
				"description1", "description2",
				NO_DEFAULT(),
				PrerequisiteManifest::getDescription,
				PrerequisiteManifest::setDescription);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setLayerId(java.lang.String)}.
	 */
	@Test
	default void testSetLayerId() {
		// Check both getter and setter in one go
		assertOptGetter(create(),
				"layer1", "layer2",
				NO_DEFAULT(),
				PrerequisiteManifest::getLayerId,
				PrerequisiteManifest::setLayerId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setTypeId(java.lang.String)}.
	 */
	@Test
	default void testSetTypeId() {
		// Check both getter and setter in one go
		assertOptGetter(create(),
				"type1", "type2",
				NO_DEFAULT(),
				PrerequisiteManifest::getTypeId,
				PrerequisiteManifest::setTypeId);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest#setContextId(java.lang.String)}.
	 */
	@Test
	default void testSetContextId() {
		// Check both getter and setter in one go
		assertOptGetter(create(),
				"context1", "context2",
				NO_DEFAULT(),
				PrerequisiteManifest::getContextId,
				PrerequisiteManifest::setContextId);
	}

}
