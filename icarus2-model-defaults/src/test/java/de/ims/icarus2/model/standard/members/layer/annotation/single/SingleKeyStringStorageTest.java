/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.members.layer.annotation.single;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
class SingleKeyStringStorageTest implements ManagedAnnotationStorageTest<SingleKeyStringStorage> {

	@Override
	public Class<? extends SingleKeyStringStorage> getTestTargetClass() {
		return SingleKeyStringStorage.class;
	}

	@Override
	public Object testValue(String key) {
		return "test_"+key;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return ValueType.STRING;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return set(ValueType.UNKNOWN, ValueType.STRING);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return set(ValueType.UNKNOWN, ValueType.STRING);
	}

	@Override
	public SingleKeyStringStorage createForLayer(AnnotationLayer layer) {
		return new SingleKeyStringStorage(key());
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyStringStorage#SingleKeyStringStorage()}.
		 */
		@Test
		void testSingleKeyStringStorage() {
			assertNotNull(new SingleKeyStringStorage(key()));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyStringStorage#SingleKeyStringStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyStringStorageInt(int capacity) {
			assertNotNull(new SingleKeyStringStorage(key(), capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyStringStorage#SingleKeyStringStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyStringStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyStringStorage(key(), capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyStringStorage#SingleKeyStringStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testSingleKeyStringStorageBooleanInt(int capacity) {
			assertNotNull(new SingleKeyStringStorage(key(), true, capacity));
			assertNotNull(new SingleKeyStringStorage(key(), false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.single.SingleKeyStringStorage#SingleKeyStringStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testSingleKeyStringStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyStringStorage(key(), true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingleKeyStringStorage(key(), false, capacity));
		}

	}

}
