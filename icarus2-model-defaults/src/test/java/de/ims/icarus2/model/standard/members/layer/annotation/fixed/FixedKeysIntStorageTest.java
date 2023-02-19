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
package de.ims.icarus2.model.standard.members.layer.annotation.fixed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class FixedKeysIntStorageTest implements MultiKeyAnnotationStorageTest<FixedKeysIntStorage>,
	ManagedAnnotationStorageTest<FixedKeysIntStorage> {

	static RandomGenerator rand;

	/** Maps keys to noEntryValues */
	private Object2IntMap<String> setup = new Object2IntOpenHashMap<>();
	private List<String> keys = new ArrayList<>();

	@BeforeEach
	void setUp() {
		for (int i = 0; i < 20; i++) {
			String key = "key_"+i;
			setup.put(key, rand.nextInt());
			keys.add(key);
		}
	}

	@AfterEach
	void tearDown() {
		setup.clear();
		keys.clear();
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG);
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);
	}

	@Override
	public Object testValue(String key) {
		int noEntryValue = setup.getInt(key);
		int value;
		do {
			value = rand.nextInt();
		} while(value==noEntryValue);
		return Integer.valueOf(value);
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
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#noEntryValue(java.lang.String)
	 */
	@Override
	public Object noEntryValue(String key) {
		return Integer.valueOf(setup.getInt(key));
	}

	@Override
	public ValueType valueType(String key) {
		return ValueType.INTEGER;
	}

	@Override
	public FixedKeysIntStorage createForLayer(AnnotationLayer layer) {
		return new FixedKeysIntStorage();
	}

	@Override
	public Class<? extends FixedKeysIntStorage> getTestTargetClass() {
		return FixedKeysIntStorage.class;
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
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage()}.
		 */
		@Test
		void testFixedKeysIntStorage() {
			assertNotNull(new FixedKeysIntStorage());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testFixedKeysIntStorageInt(int capacity) {
			assertNotNull(new FixedKeysIntStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testFixedKeysIntStorageIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysIntStorage(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {UNSET_INT, 1, 10, 100, 10_000})
		void testFixedKeysIntStorageBooleanInt(int capacity) {
			assertNotNull(new FixedKeysIntStorage(true, capacity));
			assertNotNull(new FixedKeysIntStorage(false, capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.fixed.FixedKeysIntStorage#FixedKeysIntStorage(boolean, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testFixedKeysIntStorageBooleanIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysIntStorage(true, capacity));
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new FixedKeysIntStorage(false, capacity));
		}

	}

}
