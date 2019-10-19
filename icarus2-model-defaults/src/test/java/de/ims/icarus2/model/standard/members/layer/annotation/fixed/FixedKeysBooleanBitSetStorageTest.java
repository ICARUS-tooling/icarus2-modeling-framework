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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.random.Randomized;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class FixedKeysBooleanBitSetStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysBooleanBitSetStorage>,
		ManagedAnnotationStorageTest<FixedKeysBooleanBitSetStorage> {

	static RandomGenerator rand;

	private List<String> keys = new ArrayList<>();
	private boolean[] noEntryValues;

	@BeforeEach
	void setUp() {
		int size = rand.random(5, 20);
		noEntryValues = new boolean[size];
		for (int i = 0; i < size; i++) {
			noEntryValues[i] = rand.nextBoolean();
			keys.add("test_"+i);
		}
	}

	@AfterEach
	void tearDown() {
		keys.clear();
		noEntryValues = null;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return singleton(ValueType.BOOLEAN);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return singleton(ValueType.BOOLEAN);
	}

	@Override
	public Object testValue(String key) {
		return Boolean.valueOf(!noEntryValues[keys.indexOf(key)]);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest#supportsAutoRemoval()
	 */
	@Override
	public boolean supportsAutoRemoval() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest#supportsAutoRemoveAnnotations()
	 */
	@Override
	public boolean supportsAutoRemoveAnnotations() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest#supportsItemManagement()
	 */
	@Override
	public boolean supportsItemManagement() {
		return false;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Boolean.valueOf(noEntryValues[keys.indexOf(key)]);
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.BOOLEAN;
	}

	@Override
	public FixedKeysBooleanBitSetStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysBooleanBitSetStorage();
	}

	@Override
	public Class<? extends FixedKeysBooleanBitSetStorage> getTestTargetClass() {
		return FixedKeysBooleanBitSetStorage.class;
	}

	@Override
	public List<String> keys() {
		return keys;
	}

	@Override
	public String key() {
		return keys.get(0);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage()}.
		 */
		@Test
		void testFixedKeysBooleanBitSetStorage() {
			assertNotNull(new FixedKeysBooleanBitSetStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBooleanBitSetStorageInt(int capacity) {
			assertNotNull(new FixedKeysBooleanBitSetStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBooleanBitSetStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBooleanBitSetStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBooleanBitSetStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysBooleanBitSetStorage(true, capacity));
			assertNotNull(new FixedKeysBooleanBitSetStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBooleanBitSetStorage#FixedKeysBooleanBitSetStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBooleanBitSetStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBooleanBitSetStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysBooleanBitSetStorage(false, capacity));
		}

	}

}
