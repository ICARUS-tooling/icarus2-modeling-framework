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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.AnnotationStorage;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.random.Randomized;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class PackedAnnotationStorageTest implements MultiKeyAnnotationStorageTest<PackedAnnotationStorage>,
		ManagedAnnotationStorageTest<PackedAnnotationStorage> {

	static RandomGenerator rand;

	private static final ValueType[] _types = {
			ValueType.STRING,
			ValueType.INTEGER,
			ValueType.LONG,
			ValueType.DOUBLE,
			ValueType.FLOAT,
			ValueType.BOOLEAN,
			ValueType.CUSTOM,
	};

	@SuppressWarnings("rawtypes")
	private static final Supplier[] _gen = {
			() -> rand.randomString(20),
			() -> Integer.valueOf(rand.nextInt()),
			() -> Long.valueOf(rand.nextLong()),
			() -> Double.valueOf(rand.nextDouble()*Double.MAX_VALUE),
			() -> Float.valueOf(rand.nextFloat()*Float.MAX_VALUE),
			() -> Boolean.valueOf(rand.nextBoolean()),
			() -> new Object(),
	};

	private static String key(int index) {
		return "test_"+index;
	}

	private Map<String, KeyConfig<?>> setup = new LinkedHashMap<>();
	private List<String> keys = new ArrayList<>();
	private int capacity;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		capacity = rand.random(100, 1000);

		for (int i = 0; i < 20; i++) {
			int idx = i<_types.length ? i : rand.random(0, _types.length);
			String key = key(i);
			setup.put(key, new KeyConfig<>(_types[idx], _gen[idx]));
			keys.add(key);
		}
	}

	@AfterEach
	void tearDown() {
		capacity = UNSET_INT;
		keys.clear();
		setup.clear();
	}

	@Override
	public PackedAnnotationStorage createForLayer(AnnotationLayer layer) {
		Set<PackageHandle> handles = PackedDataUtils.createHandles(layer.getManifest(), true);
		PackedDataManager<Item, AnnotationStorage> manager =
				PackedDataManager.<Item, AnnotationStorage>builder()
				.addHandles(handles)
				.allowBitPacking(true)
				.defaultStorageSource()
				.initialCapacity(capacity)
				.autoRegister(true)
				.build();

		return new PackedAnnotationStorage(() -> manager);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest#createUnadded()
	 */
	@Override
	public PackedAnnotationStorage createUnadded() {
		PackedDataManager<Item, AnnotationStorage> manager =
				PackedDataManager.<Item, AnnotationStorage>builder()
				.allowDynamicChunkComposition(true)
				.allowBitPacking(true)
				.defaultStorageSource()
				.initialCapacity(capacity)
				.autoRegister(true)
				.build();

		return new PackedAnnotationStorage(() -> manager);
	}

	/**
	 * @see de.ims.icarus2.util.PartTest#prepareAdd(de.ims.icarus2.util.Part, java.lang.Object)
	 */
	@Override
	public void prepareAdd(PackedAnnotationStorage storage, AnnotationLayer layer) {
		PackedDataManager<Item, AnnotationStorage> manager = storage.dataManager();
		assertTrue(manager.isAllowDynamicChunkComposition());

		manager.registerHandles(PackedDataUtils.createHandles(layer.getManifest(),
				manager.isAllowBitPacking()));
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#valueType(java.lang.String)
	 */
	@Override
	public ValueType valueType(String key) {
		return setup.get(key).valueType;
	}

	@Override
	public Object testValue(String key) {
		return setup.get(key).testValue();
	}

	@Override
	public Object noEntryValue(String key) {
		return setup.get(key).noEntryValue;
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#keyForType(de.ims.icarus2.model.manifest.types.ValueType)
	 */
	@Override
	public String keyForType(ValueType type) {
		for (int i = 0; i < _types.length; i++) {
			if(_types[i]==type) {
				return key(i);
			}
		}
		throw new IllegalArgumentException("Unsupported type: "+type);
	}

	/**
	 * @see de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest#key()
	 */
	@Override
	public String key() {
		return keys.get(0);
	}

	@Override
	public List<String> keys() {
		return keys;
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return PackedAnnotationStorage.class;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return setup.get(key).typesForSetters();
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return setup.get(key).typesForGetters();
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

	private static final class KeyConfig<T> {
		private final ValueType valueType;
		private final Supplier<T> generator;
		private final T noEntryValue;

		public KeyConfig(ValueType valueType, Supplier<T> generator) {
			this.valueType = requireNonNull(valueType);
			this.generator = requireNonNull(generator);

			this.noEntryValue = requireNonNull(generator.get());
		}

		T testValue() {
			T value;
			do {
				value = requireNonNull(generator.get());
			} while(Objects.equals(noEntryValue, value));
			return value;
		}

		Set<ValueType> typesForGetters() {
//			if(NUMBERS.contains(valueType)) {
//				return NUMBER_TYPES;
//			}

			return singleton(valueType);
		}

		Set<ValueType> typesForSetters() {
//			if(NUMBERS.contains(valueType)) {
//				return NUMBER_TYPES;
//			}

			if(valueType==ValueType.CUSTOM) {
				return set(ValueType.CUSTOM, ValueType.STRING);
			}

			return singleton(valueType);
		}
	}

}
