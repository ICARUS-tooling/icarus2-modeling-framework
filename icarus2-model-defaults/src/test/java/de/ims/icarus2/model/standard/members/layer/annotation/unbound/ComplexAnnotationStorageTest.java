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
package de.ims.icarus2.model.standard.members.layer.annotation.unbound;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.annotation.ManagedAnnotationStorageTest;
import de.ims.icarus2.model.api.layer.annotation.MultiKeyAnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage.AnnotationBundle;
import de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage.GrowingAnnotationBundle;
import de.ims.icarus2.test.TargetedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.random.Randomized;
import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class ComplexAnnotationStorageTest implements ManagedAnnotationStorageTest<ComplexAnnotationStorage>,
		MultiKeyAnnotationStorageTest<ComplexAnnotationStorage> {

	private Map<String, KeyConfig<?>> setup = new LinkedHashMap<>();
	private List<String> keys = new ArrayList<>();

	private static final Set<ValueType> NUMBERS =
			set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);

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

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		for (int i = 0; i < 20; i++) {
			int idx = i<_types.length ? i : rand.random(0, _types.length);
			String key = key(i);
			setup.put(key, new KeyConfig<>(_types[idx], _gen[idx]));
			keys.add(key);
		}
	}

	@AfterEach
	void tearDown() {
		keys.clear();
		setup.clear();
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

	@Override
	public Class<? extends ComplexAnnotationStorage> getTestTargetClass() {
		return ComplexAnnotationStorage.class;
	}

	@Override
	public Set<ValueType> typesForSetters(String key) {
		return setup.get(key).typesForSetters();
	}

	@Override
	public Set<ValueType> typesForGetters(String key) {
		return setup.get(key).typesForGetters();
	}

	@Override
	public ComplexAnnotationStorage createForLayer(AnnotationLayer layer) {
		return new ComplexAnnotationStorage();
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

	@Nested
	class Constructors {

		private Stream<Pair<String,Supplier<AnnotationBundle>>> bundleFactories() {
			return Stream.of(
					pair("compact", ComplexAnnotationStorage.COMPACT_BUNDLE_FACTORY),
					pair("large", ComplexAnnotationStorage.LARGE_BUNDLE_FACTORY),
					pair("growing", ComplexAnnotationStorage.GROWING_BUNDLE_FACTORY));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(java.util.function.Supplier)}.
		 */
		@TestFactory
		Stream<DynamicTest> testComplexAnnotationStorageSupplierOfAnnotationBundle() {
			return bundleFactories().map(info -> dynamicTest(
					info.first,
					() -> assertThat(new ComplexAnnotationStorage(info.second))
						.extracting(ComplexAnnotationStorage::getBundleFactory)
						.isSameAs(info.second)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(int, java.util.function.Supplier)}.
		 */
		@TestFactory
		Stream<DynamicContainer> testComplexAnnotationStorageIntSupplierOfAnnotationBundle() {
			return bundleFactories().map(info  -> dynamicContainer(info.first,
					IntStream.of(UNSET_INT, 1, 10, 100, 10_000).mapToObj(capacity ->
							dynamicTest(String.valueOf(capacity),
									() -> assertThat(new ComplexAnnotationStorage(capacity, info.second))
										.extracting(ComplexAnnotationStorage::getBundleFactory)
										.isSameAs(info.second)))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(int, java.util.function.Supplier)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -2})
		void testComplexAnnotationStorageIntSupplierOfAnnotationBundleInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new ComplexAnnotationStorage(capacity,
							ComplexAnnotationStorage.COMPACT_BUNDLE_FACTORY));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.unbound.ComplexAnnotationStorage#ComplexAnnotationStorage(boolean, int, java.util.function.Supplier)}.
		 */
		@Test
		@Disabled("covered by testComplexAnnotationStorageIntSupplierOfAnnotationBundle")
		void testComplexAnnotationStorageBooleanIntSupplierOfAnnotationBundle() {
			fail("Not yet implemented");
		}

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
			if(NUMBERS.contains(valueType)) {
				return NUMBER_TYPES;
			}

			return singleton(valueType);
		}

		Set<ValueType> typesForSetters() {
			if(NUMBERS.contains(valueType)) {
				return NUMBER_TYPES;
			}

			if(valueType==ValueType.CUSTOM) {
				return set(ValueType.CUSTOM, ValueType.STRING);
			}

			return singleton(valueType);
		}
	}

	@Nested
	class Bundles {

		abstract class TestBase<B extends AnnotationBundle> implements TargetedTest<B> {

			protected List<String> collectKeys(B bundle) {
				List<String> keys = new ArrayList<>();
				assertThat(bundle.collectKeys(keys::add));
				return keys;
			}

			protected Map<String, Object> mapping(int size) {
				return keys().stream()
						.limit(size)
						.collect(Collectors.toMap(k -> k, k -> new Object()));
			}

			@Test
			void testFill() {
				Map<String, Object> mapping = mapping(5);
				B bundle = create();

				for(Entry<String, Object> e : mapping.entrySet()) {
					assertThat(bundle.setValue(e.getKey(), e.getValue()))
						.isEqualTo(true);
				}
			}

			@Test
			void testAddRedundant() {
				Map<String, Object> mapping = mapping(5);
				B bundle = create();
				mapping.forEach(bundle::setValue);

				for(Entry<String, Object> e : mapping.entrySet()) {
					assertThat(bundle.setValue(e.getKey(), e.getValue()))
						.isEqualTo(false);
				}
			}

			@Test
			void testKeys() {
				Map<String, Object> mapping = mapping(5);
				B bundle = create();
				mapping.forEach(bundle::setValue);

				assertThat(collectKeys(bundle))
					.containsExactlyInAnyOrderElementsOf(mapping.keySet());
			}

			@Test
			void testLookup() {
				Map<String, Object> mapping = mapping(5);
				B bundle = create();
				mapping.forEach(bundle::setValue);

				for(Entry<String, Object> e : mapping.entrySet()) {
					assertThat(bundle.getValue(e.getKey()))
						.describedAs("Mapping for key %s", e.getKey())
						.isSameAs(e.getValue());
				}

				assertThat(bundle.getValue("myFantasyKeyXXX")).isNull();
			}

			@Test
			void testDelete() {
				Map<String, Object> mapping = mapping(5);
				B bundle = create();
				mapping.forEach(bundle::setValue);

				for(String key : mapping.keySet()) {
					assertThat(bundle.setValue(key, null)).isTrue();
					assertThat(bundle.getValue(key)).isNull();
				}
			}
		}

		@Nested
		class GrowingBundle extends TestBase<GrowingAnnotationBundle> {

			@Override
			public Class<?> getTestTargetClass() { return GrowingAnnotationBundle.class; }

			@Override
			public GrowingAnnotationBundle createTestInstance(TestSettings settings) {
				return settings.process(new GrowingAnnotationBundle());
			}

			@Test
			void testGrow() {
				final int limit = GrowingAnnotationBundle.ARRAY_THRESHOLD;
				GrowingAnnotationBundle bundle = new GrowingAnnotationBundle(limit);

				for (int i = 0; i < limit; i++) {
					bundle.setValue("key_"+i, new Object());
					assertThat(bundle.isMap()).isFalse();
				}

				bundle.setValue("threshold_key", new Object());
				assertThat(bundle.isMap()).isTrue();
			}

			@Test
			void testShrink() {
				final int limit = GrowingAnnotationBundle.ARRAY_THRESHOLD;
				GrowingAnnotationBundle bundle = new GrowingAnnotationBundle(limit);

				for (int i = 0; i < limit+1; i++) {
					bundle.setValue("key_"+i, new Object());
				}
				assertThat(bundle.isMap()).isTrue();

				assertThat(bundle.setValue("key_0", null)).isTrue();
				assertThat(bundle.isMap()).isFalse();
				assertThat(bundle.setValue("key_1", null)).isTrue();
				assertThat(bundle.isMap()).isFalse();
			}
		}

		//TODO test classes for the other 2 (simple) bundle types
	}
}
