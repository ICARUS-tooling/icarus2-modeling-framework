/**
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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class FixedKeysBoolean31BitStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysBoolean31BitStorage>,
		ManagedAnnotationStorageTest<FixedKeysBoolean31BitStorage> {

	private static final LookupList<String> keys = new LookupList<>();
	static {
		IntStream.range(0, FixedKeysBoolean31BitStorage.MAX_KEY_COUNT)
			.forEach(i -> keys.add("test_" + i));

	}

	static RandomGenerator rand;

	private boolean[] noEntryValues;

	@BeforeEach
	void setUp() {
		noEntryValues = new boolean[keys.size()];
		for (int i = 0; i < keys.size(); i++) {
			noEntryValues[i] = rand.nextBoolean();
		}
	}

	@AfterEach
	void tearDown() {
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
	public FixedKeysBoolean31BitStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysBoolean31BitStorage();
	}

	@Override
	public Class<? extends FixedKeysBoolean31BitStorage> getTestTargetClass() {
		return FixedKeysBoolean31BitStorage.class;
	}

	@Override
	public List<String> keys() {
		List<String> list = new ArrayList<>();
		keys.forEach(list::add);
		return list;
	}

	@Override
	public String key() {
		return keys.get(0);
	}

	@Nested
	class Constructors {

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage#FixedKeysBoolean31BitStorage()}.
		 */
		@Test
		void testFixedKeysBoolean31BitStorage() {
			assertNotNull(new FixedKeysBoolean31BitStorage());
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage#FixedKeysBoolean31BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean31BitStorageInt(int capacity) {
			assertNotNull(new FixedKeysBoolean31BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage#FixedKeysBoolean31BitStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean31BitStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean31BitStorage(capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage#FixedKeysBoolean31BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { UNSET_INT, 1, 10, 100, 10_000 })
		void testFixedKeysBoolean31BitStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysBoolean31BitStorage(true, capacity));
			assertNotNull(new FixedKeysBoolean31BitStorage(false, capacity));
		}

		/**
		 * Test method for
		 * {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysBoolean31BitStorage#FixedKeysBoolean31BitStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = { 0, -2 })
		void testFixedKeysBoolean31BitStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT, () -> new FixedKeysBoolean31BitStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysBoolean31BitStorage(false, capacity));
		}

	}

}
