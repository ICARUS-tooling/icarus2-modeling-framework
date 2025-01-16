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

import static org.assertj.core.api.Assertions.assertThat;
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
		assertThat(ContainerType.LIST.getCompatibleTypes()).containsOnly(
				ContainerType.SPAN, ContainerType.SINGLETON);
		assertThat(ContainerType.SPAN.getCompatibleTypes()).containsOnly(ContainerType.SINGLETON);
		assertThat(ContainerType.SINGLETON.getCompatibleTypes()).isEmpty();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#getIncompatibleTypes()}.
	 */
	@Test
	void testGetIncompatibleTypes() {
		assertThat(ContainerType.SINGLETON.getIncompatibleTypes()).containsOnly(
				ContainerType.SPAN, ContainerType.LIST);
		assertThat(ContainerType.SPAN.getIncompatibleTypes()).containsOnly(ContainerType.LIST);
		assertThat(ContainerType.LIST.getIncompatibleTypes()).isEmpty();
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ContainerType#id()}.
	 */
	@Test
	void testId() {
		ByteSet usedIds = new ByteArraySet();
		for(ContainerType type : ContainerType.values()) {
			byte id = type.id();
			assertThat(usedIds.contains(id)).as("duplicate id: "+id).isFalse();
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
