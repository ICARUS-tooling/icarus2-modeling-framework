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

import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.bytes.ByteArraySet;
import it.unimi.dsi.fastutil.bytes.ByteSet;

/**
 * @author Markus Gärtner
 *
 */
class ContainerTypeTest {

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#getStringValue()}.
	 */
	@Test
	void testGetStringValue() {
		Set<String> usedValues = new HashSet<>();

		for(ContainerType type : ContainerType.values()) {
			String value = type.getStringValue();
			assertFalse(usedValues.contains(value), "duplicate string form: "+value);
			usedValues.add(value);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#isCompatibleWith(de.ims.icarus2.model.manifest.api.ContainerType)}.
	 */
	@Test
	void testIsCompatibleWith() {
		assertTrue(ContainerType.LIST.isCompatibleWith(ContainerType.SPAN));
		assertTrue(ContainerType.LIST.isCompatibleWith(ContainerType.SINGLETON));
		assertTrue(ContainerType.SPAN.isCompatibleWith(ContainerType.SINGLETON));

		assertFalse(ContainerType.SPAN.isCompatibleWith(ContainerType.LIST));
		assertFalse(ContainerType.SINGLETON.isCompatibleWith(ContainerType.SPAN));
		assertFalse(ContainerType.SINGLETON.isCompatibleWith(ContainerType.LIST));
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#getCompatibleTypes()}.
	 */
	@Test
	void testGetCompatibleTypes() {
		assertCollectionEquals(set(ContainerType.LIST.getCompatibleTypes()),
				ContainerType.SPAN, ContainerType.SINGLETON);
		assertCollectionEquals(set(ContainerType.SPAN.getCompatibleTypes()),
				ContainerType.SINGLETON);
		assertArrayEquals(new Object[0], ContainerType.SINGLETON.getCompatibleTypes());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#getIncompatibleTypes()}.
	 */
	@Test
	void testGetIncompatibleTypes() {
		assertCollectionEquals(set(ContainerType.SINGLETON.getIncompatibleTypes()),
				ContainerType.SPAN, ContainerType.LIST);
		assertCollectionEquals(set(ContainerType.SPAN.getIncompatibleTypes()),
				ContainerType.LIST);
		assertArrayEquals(new Object[0], ContainerType.LIST.getIncompatibleTypes());
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#id()}.
	 */
	@Test
	void testId() {
		ByteSet usedIds = new ByteArraySet();
		for(ContainerType type : ContainerType.values()) {
			byte id = type.id();
			assertFalse(usedIds.contains(id), "duplicate id: "+id);
			usedIds.add(id);
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#parseContainerType(java.lang.String)}.
	 */
	@Test
	void testParseContainerType() {
		for(ContainerType type : ContainerType.values()) {
			assertEquals(type, ContainerType.parseContainerType(type.getStringValue()));
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#forId(byte)}.
	 */
	@Test
	void testForId() {
		for(ContainerType type : ContainerType.values()) {
			assertEquals(type, ContainerType.forId(type.id()));
		}
	}

}
