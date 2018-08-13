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

import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface ValueSetTest<V extends ValueSet> extends LockableTest<V>, TypedManifestTest<V> {

	@Provider
	V createWithType(TestSettings settings, ValueType valueType);

	/**
	 * @see de.ims.icarus2.test.GenericTest#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default V createTestInstance(TestSettings settings) {
		return createWithType(settings, ValueType.STRING);
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValues()}.
	 */
	@Test
	default void testValues() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#valueCount()}.
	 */
	@Test
	default void testValueCount() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValueAt(int)}.
	 */
	@Test
	default void testGetValueAt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#forEachValue(java.util.function.Consumer)}.
	 */
	@Test
	default void testForEachValue() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#getValueType()}.
	 */
	@Test
	default void testGetValueType() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#indexOfValue(java.lang.Object)}.
	 */
	@Test
	default void testIndexOfValue() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#addValue(java.lang.Object)}.
	 */
	@Test
	default void testAddValueObject() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#addValue(java.lang.Object, int)}.
	 */
	@Test
	default void testAddValueObjectInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.manifest.api.ValueSet#removeValue(int)}.
	 */
	@Test
	default void testRemoveValue() {
		fail("Not yet implemented");
	}

}
