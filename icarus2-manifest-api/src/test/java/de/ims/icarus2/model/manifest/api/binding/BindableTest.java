/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertCollectionEmpty;
import static de.ims.icarus2.test.TestUtils.assertCollectionNotEmpty;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.Multiplicity;

/**
 * @author Markus Gärtner
 *
 */
public interface BindableTest<B extends Bindable> extends GenericTest<B> {

	@Provider
	B createWithBindingEndpoints(TestSettings settings, Set<LayerPrerequisite> bindingEndpoints);

	Set<Multiplicity> getSupportedBindingMultiplicities();

	public static LayerPrerequisite mockPrerequisite(String alias, String layerId,
			String contextId, String description, String typeId, Multiplicity multiplicity) {
		LayerPrerequisite p = mock(LayerPrerequisite.class);
		when(p.getAlias()).thenReturn(alias);
		when(p.getLayerId()).thenReturn(Optional.ofNullable(layerId));
		when(p.getContextId()).thenReturn(Optional.ofNullable(contextId));
		when(p.getDescription()).thenReturn(Optional.ofNullable(description));
		when(p.getTypeId()).thenReturn(Optional.ofNullable(typeId));
		when(p.getMultiplicity()).thenReturn(multiplicity);

		return p;
	}

	public static LayerPrerequisite mockPrerequisite(int index, Multiplicity multiplicity) {
		return mockPrerequisite(
				"alias"+index,
				"layerId"+index,
				"contextId"+index,
				"description"+index,
				"typeId"+index, multiplicity);
	}

	public static void assertPrerequisiteEquals(LayerPrerequisite expected, LayerPrerequisite actual) {
		assertEquals(expected.getAlias(), actual.getAlias());
		assertEquals(expected.getLayerId(), actual.getLayerId());
		assertEquals(expected.getContextId(), actual.getContextId());
		assertEquals(expected.getDescription(), actual.getDescription());
		assertEquals(expected.getTypeId(), actual.getTypeId());
		assertEquals(expected.getMultiplicity(), actual.getMultiplicity());
	}

	@Test
	@RandomizedTest
	default void testGetBindingEndpoints(RandomGenerator rand) {

		// Test with unset endpoints, so expect empty set
		B instance = createTestInstance(settings());
		Set<LayerPrerequisite> bindingEndpoints = instance.getBindingEndpoints();
		assertNotNull(bindingEndpoints);
		assertCollectionEmpty(bindingEndpoints);

		// Test with predefined collection of endpoints
		Set<Multiplicity> supportedMultiplicities = getSupportedBindingMultiplicities();
		Supplier<Multiplicity> multRand = rand.randomizer(supportedMultiplicities);
		Set<LayerPrerequisite> origBindings = new HashSet<>();
		for(int i=0; i<100; i++) {
			origBindings.add(mockPrerequisite(i, multRand.get()));
		}
		B filledInstance = createWithBindingEndpoints(settings(), origBindings);
		Set<LayerPrerequisite> newEndpoints = filledInstance.getBindingEndpoints();
		assertCollectionNotEmpty(newEndpoints);
		assertEquals(origBindings.size(), newEndpoints.size());
		// Complicated assertion, as we can't use plain equals() on those endpoints
		Map<String, LayerPrerequisite> lookup = new HashMap<>();
		for(LayerPrerequisite binding : origBindings) {
			lookup.put(binding.getAlias(), binding);
		}
		for(LayerPrerequisite endpoint : newEndpoints) {
			LayerPrerequisite origBinding = lookup.get(endpoint.getAlias());
			assertNotNull(origBinding, "Unknown binding "+endpoint.getAlias());
			assertPrerequisiteEquals(origBinding, endpoint);
		}
	}
}
