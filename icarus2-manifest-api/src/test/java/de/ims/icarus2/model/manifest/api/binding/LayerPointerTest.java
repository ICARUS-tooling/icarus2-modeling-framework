/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.test.TestUtils.assertPairwiseNotEquals;
import static de.ims.icarus2.test.TestUtils.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerPointerTest<P extends LayerPointer> extends GenericTest<P> {

	@Provider
	P createTestInstance(TestSettings settings, String layerId, String contextId);

	public static LayerPointer mockLayerPointer(String layerId, String contextId) {
		LayerPointer p = mock(LayerPointer.class);
		when(p.getLayerId()).thenReturn(layerId);
		when(p.getContextId()).thenReturn(contextId);
		return p;
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPointer#getContextId()}.
	 */
	@Test
	default void testGetContextId() {
		P pointer1 = create();
		assertNull(pointer1.getContextId());

		String contextId = "contextId";
		P pointer2 = createTestInstance(settings(), null, contextId);
		assertEquals(contextId, pointer2.getContextId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPointer#getLayerId()}.
	 */
	@Test
	default void testGetLayerId() {
		P pointer1 = create();
		assertNull(pointer1.getLayerId());

		String layerId = "contextId";
		P pointer2 = createTestInstance(settings(), layerId, null);
		assertEquals(layerId, pointer2.getLayerId());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.binding.LayerPointer#defaultEquals(de.ims.icarus2.model.manifest.api.binding.LayerPointer, de.ims.icarus2.model.manifest.api.binding.LayerPointer)}.
	 */
	@Test
	default void testDefaultEquals() {
		LayerPointer p1 = mockLayerPointer("layer1", "context1");
		LayerPointer p2 = mockLayerPointer("layer2", "context2");
		LayerPointer p3 = mockLayerPointer("layer1", null);
		LayerPointer p4 = mockLayerPointer(null, "context1");
		LayerPointer p5 = mockLayerPointer(null, null);

		LayerPointer p1_2 = mockLayerPointer("layer1", "context1");

		assertTrue(LayerPointer.defaultEquals(p1, p1));
		assertTrue(LayerPointer.defaultEquals(p2, p2));
		assertTrue(LayerPointer.defaultEquals(p3, p3));
		assertTrue(LayerPointer.defaultEquals(p4, p4));
		assertTrue(LayerPointer.defaultEquals(p5, p5));

		assertTrue(LayerPointer.defaultEquals(p1, p1_2));

		assertPairwiseNotEquals(LayerPointer::defaultEquals,
				p1, p2, p3, p4, p5);
	}

}
