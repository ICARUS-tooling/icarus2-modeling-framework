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
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertUnsupportedType;
import static de.ims.icarus2.test.TestTags.CONCURRENT;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.filledArray;
import static de.ims.icarus2.test.util.Triple.nullableTriple;
import static de.ims.icarus2.util.collections.CollectionUtils.list;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.Builder;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.DisabledOnCi;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.concurrent.Circuit;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.test.util.Triple;
import de.ims.icarus2.util.BuilderTest;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;
import de.ims.icarus2.util.collections.Substitutor;
import de.ims.icarus2.util.mem.ByteAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class PackedDataManagerTest {

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

	private static int indexOfType(ValueType type) {
		for (int i = 0; i < _types.length; i++) {
			if(_types[i] == type) {
				return i;
			}
		}
		throw new IllegalArgumentException("Unsupported type: "+type);
	}

	private static String key(int index) {
		return "key_"+index;
	}

	private static Object testValue(PackageHandle handle) {
		Object noEntryValue = handle.getNoEntryValue();
		@SuppressWarnings("unchecked")
		Supplier<Object> gen = _gen[indexOfType(handle.getConverter().getValueType())];
		Object value;
		do {
			value = gen.get();
		} while(Objects.equals(noEntryValue, value));
		return value;
	}

	private static Object noEntryValue(ValueType type) {
		return _gen[indexOfType(type)].get();
	}

	private static Set<ValueType> typesForGetters(PackageHandle handle) {
		ValueType type = handle.getConverter().getValueType();
		if(type==ValueType.STRING) {
			return set(ValueType.CUSTOM, ValueType.STRING);
		}
		return singleton(type);
	}

	private static Set<ValueType> typesForSetters(PackageHandle handle) {
		ValueType type = handle.getConverter().getValueType();
		if(type==ValueType.CUSTOM) {
			return set(ValueType.CUSTOM, ValueType.STRING);
		}
		return singleton(type);
	}

	private static PackageHandle createHandle(ValueType type, String key, boolean allowBitPacking) {
		Object noEntryValue = noEntryValue(type);
		if(type.isPrimitiveType()) {
			return new PackageHandle(key, noEntryValue,
					BytePackConverter.forPrimitiveType(type, allowBitPacking));
		}

		Substitutor<Object> substitutor = new Substitutor<>(10_000);
		BytePackConverter converter = new BytePackConverter.SubstitutingConverterInt<>(
				type, 4, substitutor, substitutor);

		return new PackageHandle(key, noEntryValue, converter);
	}

	private static Object item() {
		return new Object();
	}

	@Nested
	class ForBuilder implements BuilderTest<PackedDataManager<Object,Object>, PackedDataManager.Builder<Object,Object>> {

		/**
		 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
		 */
		@Override
		public Class<?> getTestTargetClass() {
			return Builder.class;
		}

		/**
		 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
		 */
		@Override
		public Builder<Object, Object> createTestInstance(TestSettings settings) {
			return settings.process(PackedDataManager.builder());
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidOps()
		 */
		@Override
		public List<Triple<String, Class<? extends Throwable>, Consumer<? super Builder<Object, Object>>>> invalidOps() {
			return list(
					Triple.triple("zero initialCapacity", IllegalArgumentException.class, b -> b.initialCapacity(0)),
					Triple.triple("negative initialCapacity", IllegalArgumentException.class, b -> b.initialCapacity(-1234))
			);
		}

		/**
		 * @see de.ims.icarus2.util.BuilderTest#invalidConfigurations()
		 */
		@Override
		public List<Pair<String, Consumer<? super Builder<Object, Object>>>> invalidConfigurations() {
			return list(
					Pair.pair("missing handles and no dynamic registration",
							b -> b.storageSource(mock(IntFunction.class)).initialCapacity(100))
			);
		}

		@Test
		void testAddHandlesArray() {
			PackageHandle[] handles = filledArray(10, PackageHandle.class);
			Builder<Object, Object> builder = create();

			for (int i = 0; i < handles.length; i++) {
				builder.addHandles(handles[i]);

				assertThat(builder.getHandles()).containsExactlyInAnyOrder(Arrays.copyOfRange(handles, 0, i+1));
			}
		}

		@Test
		void testAddDuplicateHandlesArray() {
			PackageHandle[] handles = filledArray(10, PackageHandle.class);
			Builder<Object, Object> builder = create();
			builder.addHandles(handles);

			Stream.of(handles).forEach(handle -> assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> builder.addHandles(handle)));
		}

		@Test
		void testAddHandlesCollection() {
			PackageHandle[] handles = filledArray(10, PackageHandle.class);
			Builder<Object, Object> builder = create();

			for (int i = 0; i < handles.length; i++) {
				builder.addHandles(set(handles[i]));

				assertThat(builder.getHandles()).containsExactlyInAnyOrder(Arrays.copyOfRange(handles, 0, i+1));
			}
		}

		@Test
		void testAddDuplicateHandlesCollection() {
			PackageHandle[] handles = filledArray(10, PackageHandle.class);
			Builder<Object, Object> builder = create();
			builder.addHandles(handles);

			Stream.of(handles).forEach(handle -> assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> builder.addHandles(set(handle))));
		}

		@Test
		void testDefaultStorageSource() {
			Builder<Object, Object> builder = create();
			builder.defaultStorageSource();
			assertThat(builder.getStorageSource()).isNotNull();
		}
	}

	@Nested
	class WithHandlesAndBitPacking {
		List<PackageHandle> handles = new ArrayList<>();
		List<Supplier<Object>> generators = new ArrayList<>();

		@SuppressWarnings("unchecked")
		@BeforeEach
		void setUp() {
			IntStream.range(0, 20)
				.forEach(i -> {
					int index = i<_types.length ? i : rand.random(0, _types.length);
					ValueType type = _types[index];
					String key = key(i);
					handles.add(createHandle(type, key, true));
					generators.add(_gen[index]);
				});
		}

		@AfterEach
		void tearDown() {
			handles.clear();
		}

		String label(int i) {
			return String.valueOf(i)+" "+handles.get(i).getConverter().getValueType().getName();
		}

		@Nested
		class WithDynamicInstance {
			PackedDataManager<Object, Object> manager;
			Object owner;

			@BeforeEach
			void setUp() {
				owner = new Object();
				manager = PackedDataManager.builder()
						.allowBitPacking(true)
						.defaultStorageSource()
						.allowDynamicChunkComposition(true)
						.initialCapacity(10)
						.build();
			}

			@AfterEach
			void tearDown() {
				manager = null;
				owner = null;
			}

			//TODO add tests for actual incremental build of the udnerlying storage

			//TODO add tests for add and remove owners

			@Nested
			class ForHandleLookups {

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#registerHandles(java.util.Set)}.
				 */
				@Test
				void testRegisterHandles() {
					manager.registerHandles(handles);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#registerHandles(java.util.Set)}.
				 */
				@Test
				void testRegisterHandlesDuplicates() {
					manager.registerHandles(handles);

					for(PackageHandle handle : handles) {
						assertThrows(IllegalArgumentException.class,
								() -> manager.registerHandles(singleton(handle)));
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregisterHandles(java.util.Set)}.
				 */
				@Test
				void testUnregisterHandles() {
					manager.registerHandles(handles);
					manager.unregisterHandles(handles);
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregisterHandles(java.util.Set)}.
				 */
				@Test
				void testUnregisterHandlesUnknown() {
					for(PackageHandle handle : handles) {
						assertThrows(IllegalArgumentException.class,
								() -> manager.unregisterHandles(singleton(handle)));
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
				 */
				@Test
				void testLookupHandleEmpty() {
					assertNull(manager.lookupHandle(new Object()));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
				 */
				@Test
				void testLookupHandleForeign() {
					manager.registerHandles(handles);
					assertNull(manager.lookupHandle(new Object()));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
				 */
				@Test
				void testLookupHandle() {
					manager.registerHandles(handles);
					for(PackageHandle handle : handles) {
						assertSame(handle, manager.lookupHandle(handle.getSource()));
					}
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@Test
				void testLookupHandlesEmpty() {
					assertTrue(manager.lookupHandles(singleton(new Object())).isEmpty());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@Test
				void testLookupHandlesForeignSingle() {
					manager.registerHandles(handles);
					assertTrue(manager.lookupHandles(singleton(new Object())).isEmpty());
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@TestFactory
				Stream<DynamicTest> testLookupHandlesForeignMulti() {
					manager.registerHandles(handles.subList(0, 1));
					return rand.randomSubLists(handles.subList(1, handles.size()), 0.5)
							.map(list -> dynamicTest(String.valueOf(list.size()), () -> {
								Set<Object> sources = list.stream()
										.map(PackageHandle::getSource)
										.collect(Collectors.toSet());
								assertTrue(manager.lookupHandles(sources).isEmpty());
							}));
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@TestFactory
				Stream<DynamicTest> testLookupHandlesFilled() {
					manager.registerHandles(handles);
					return rand.randomSubLists(handles, 0.5)
							.map(list -> dynamicTest(String.valueOf(list.size()), () -> {
								Set<Object> sources = list.stream()
										.map(PackageHandle::getSource)
										.collect(Collectors.toSet());
								assertThat(manager.lookupHandles(sources).values()).hasSameElementsAs(list);
							}));
				}
			}

		}

		@Nested
		class WithFixedInstance {

			PackedDataManager<Object, Object> manager;

			@BeforeEach
			void setUp() {
				manager = PackedDataManager.builder()
						.allowBitPacking(true)
						.defaultStorageSource()
						.addHandles(handles)
						.initialCapacity(10)
						.build();
			}

			@AfterEach
			void tearDown() {
				manager = null;
			}

			@Nested
			class WhenPreAdded implements ApiGuardedTest<PackedDataManager<Object, Object>> {

				Object owner;

				@BeforeEach
				void setUp() {
					owner = new Object();
					manager.addNotify(owner);
				}

				@AfterEach
				void tearDown() {
					manager.removeNotify(owner);
					owner = null;
				}

				@Override
				public PackedDataManager<Object, Object> createTestInstance(TestSettings settings) {
					return settings.process(manager);
				}

				@Override
				public Class<?> getTestTargetClass() {
					return PackedDataManager.class;
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isWeakKeys()}.
				 */
				@Test
				void testIsWeakKeys() {
					assertFalse(manager.isWeakKeys());
				}

				@Nested
				class Empty {

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, ByteAllocator, int)}.
					 */
					@SuppressWarnings("boxing")
					@TestFactory
					Stream<DynamicTest> testGetBoolean() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.BOOLEAN)) {
										assertEquals(((Boolean)handle.getNoEntryValue()).booleanValue(),
												manager.getBoolean(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getBoolean(item(), handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetInteger() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.INTEGER)) {
										assertEquals(((Number)handle.getNoEntryValue()).intValue(),
												manager.getInteger(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getInteger(item(), handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetLong() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.LONG)) {
										assertEquals(((Number)handle.getNoEntryValue()).longValue(),
												manager.getLong(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getLong(item(), handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetFloat() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.FLOAT)) {
										assertEquals(((Number)handle.getNoEntryValue()).floatValue(),
												manager.getFloat(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getFloat(item(), handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetDouble() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.DOUBLE)) {
										assertEquals(((Number)handle.getNoEntryValue()).doubleValue(),
												manager.getDouble(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getDouble(item(), handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetValue() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									assertEquals(handle.getNoEntryValue(),
											manager.getValue(item(), handle));
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, ByteAllocator, int)}.
					 */
					@SuppressWarnings("cast")
					@TestFactory
					Stream<DynamicTest> testGetString() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									if(typesForGetters(handle).contains(ValueType.STRING)) {
										assertEquals((String)handle.getNoEntryValue(),
												manager.getString(item(), handle));
									} else {
										assertUnsupportedType(() -> manager.getString(item(), handle));
									}
								}));
					}
				}

				@Nested
				class NoEntryValue {

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, ByteAllocator, int)}.
					 */
					@SuppressWarnings("boxing")
					@TestFactory
					Stream<DynamicTest> testGetBoolean() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.BOOLEAN)) {
										assertEquals(((Boolean)handle.getNoEntryValue()).booleanValue(),
												manager.getBoolean(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getBoolean(item, handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetInteger() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.INTEGER)) {
										assertEquals(((Number)handle.getNoEntryValue()).intValue(),
												manager.getInteger(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getInteger(item, handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetLong() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.LONG)) {
										assertEquals(((Number)handle.getNoEntryValue()).longValue(),
												manager.getLong(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getLong(item, handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetFloat() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.FLOAT)) {
										assertEquals(((Number)handle.getNoEntryValue()).floatValue(),
												manager.getFloat(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getFloat(item, handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetDouble() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.DOUBLE)) {
										assertEquals(((Number)handle.getNoEntryValue()).doubleValue(),
												manager.getDouble(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getDouble(item, handle));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetValue() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									assertEquals(handle.getNoEntryValue(), manager.getValue(item, handle));
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, ByteAllocator, int)}.
					 */
					@SuppressWarnings("cast")
					@TestFactory
					Stream<DynamicTest> testGetString() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									if(typesForGetters(handle).contains(ValueType.STRING)) {
										assertEquals((String)handle.getNoEntryValue(),
												manager.getString(item, handle));
									} else {
										assertUnsupportedType(() -> manager.getString(item, handle));
									}
								}));
					}
				}

				@Nested
				class FullCycle {

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, ByteAllocator, int)}.
					 */
					@SuppressWarnings("boxing")
					@TestFactory
					Stream<DynamicTest> testGetBoolean() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									boolean value = rand.nextBoolean();
									if(typesForSetters(handle).contains(ValueType.BOOLEAN)) {
										manager.setBoolean(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getBoolean(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setBoolean(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetInteger() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									int value = rand.nextInt();
									if(typesForSetters(handle).contains(ValueType.INTEGER)) {
										manager.setInteger(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getInteger(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setInteger(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetLong() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									long value = rand.nextLong();
									if(typesForSetters(handle).contains(ValueType.LONG)) {
										manager.setLong(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getLong(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setLong(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetFloat() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									float value = rand.nextFloat();
									if(typesForSetters(handle).contains(ValueType.FLOAT)) {
										manager.setFloat(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getFloat(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setFloat(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetDouble() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									double value = rand.nextDouble();
									if(typesForSetters(handle).contains(ValueType.DOUBLE)) {
										manager.setDouble(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getDouble(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setDouble(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, ByteAllocator, int)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetValue() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									Object value = new Object();
									if(typesForSetters(handle).contains(ValueType.CUSTOM)
											|| typesForSetters(handle).contains(ValueType.STRING)) {
										manager.setValue(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getValue(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setValue(item, handle, value));
									}
								}));
					}

					@TestFactory
					Stream<DynamicTest> testGetString() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									Object value = new Object();
									if(typesForSetters(handle).contains(ValueType.STRING)) {
										manager.setValue(item, handle, value);
										assertTrue(manager.isUsed(item));
										assertEquals(value, manager.getValue(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setValue(item, handle, value));
									}
								}));
					}
				}

				@Nested
				class ForHandleLookups {

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#registerHandles(java.util.Set)}.
					 */
					@Test
					void testRegisterHandles() {
						assertThrows(IllegalStateException.class,
								() -> manager.registerHandles(handles));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregisterHandles(java.util.Set)}.
					 */
					@Test
					void testUnregisterHandles() {
						assertThrows(IllegalStateException.class,
								() -> manager.unregisterHandles(handles));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
					 */
					@Test
					void testLookupHandle() {
						for(PackageHandle handle : handles) {
							assertSame(handle, manager.lookupHandle(handle.getSource()));
						}
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
					 */
					@Test
					void testLookupHandleForeign() {
						assertNull(manager.lookupHandle(new Object()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
					 */
					@TestFactory
					Stream<DynamicTest> testLookupHandles() {
						return rand.randomSubLists(handles, 0.5)
								.map(list -> dynamicTest(String.valueOf(list.size()), () -> {
									Set<Object> sources = list.stream()
											.map(PackageHandle::getSource)
											.collect(Collectors.toSet());
									assertThat(manager.lookupHandles(sources).values()).hasSameElementsAs(list);
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
					 */
					@Test
					void testLookupHandlesForeign() {
						assertTrue(manager.lookupHandles(singleton(new Object())).isEmpty());
					}

				}

				@Nested
				class ForValueChecks {

					/**
					 * Test method for {@link PackedDataManager#clear(Supplier, PackageHandle[])}.
					 */
					@RepeatedTest(RUNS)
					void testClearSupplierEPackageHandleArray() {
						Object[] items = Stream.generate(Object::new)
								.limit(rand.random(10, 20))
								.toArray();

						/*
						 * Fill complete slot for every item with random data
						 * (guaranteed to be different from noEntryValues).
						 */
						for(Object item : items) {
							assertTrue(manager.register(item));
							for (int i = 0; i < handles.size(); i++) {
								PackageHandle handle = handles.get(i);
								manager.setValue(item, handle, testValue(handle));
							}
						}

						// Decide on random handles to be cleared
						BitSet clearP = new BitSet(handles.size());
						List<PackageHandle> tmpP = new ArrayList<>();
						rand.ints(0, handles.size())
								.distinct()
								.limit(handles.size()/2)
								.forEach(index -> {
									clearP.set(index);
									tmpP.add(handles.get(index));
								});

						// Decide on random items to be cleared
						BitSet clearI = new BitSet(items.length);
						List<Object> tmpI = new ArrayList<>();
						rand.ints(0, items.length)
								.distinct()
								.limit(items.length/2)
								.forEach(index -> {
									clearI.set(index);
									tmpI.add(items[index]);
								});
						Iterator<Object> it = tmpI.iterator();
						Supplier<Object> supplier = () -> it.hasNext() ? it.next() : null;

						manager.clear(supplier, tmpP.toArray(new PackageHandle[0]));

						for(int i=0; i<items.length; i++) {
							for (int j = 0; j < handles.size(); j++) {
								Object item = items[i];
								PackageHandle handle = handles.get(j);
								if(clearI.get(i) && clearP.get(j)) {
									assertEquals(handle.getNoEntryValue(),
											manager.getValue(item, handle));
								} else {
									assertNotEquals(handle.getNoEntryValue(),
											manager.getValue(item, handle));
								}
							}
						}
					}

					/**
					 * Test method for {@link PackedDataManager#clear(PackageHandle[])}.
					 */
					@RepeatedTest(RUNS)
					void testClearPackageHandleArray() {
						Object[] items = Stream.generate(Object::new)
								.limit(rand.random(10, 20))
								.toArray();

						/*
						 * Fill complete slot for every item with random data
						 * (guaranteed to be different from noEntryValues).
						 */
						for(Object item : items) {
							assertTrue(manager.register(item));
							for (int i = 0; i < handles.size(); i++) {
								PackageHandle handle = handles.get(i);
								manager.setValue(item, handle, testValue(handle));
							}
						}

						// Decide on random handles to be cleared
						BitSet clear = new BitSet(handles.size());
						List<PackageHandle> tmp = new ArrayList<>();
						rand.ints(0, handles.size())
								.distinct()
								.limit(handles.size()/2)
								.forEach(index -> {
									clear.set(index);
									tmp.add(handles.get(index));
								});

						manager.clear(tmp.toArray(new PackageHandle[0]));

						for(Object item : items) {
							for (int i = 0; i < handles.size(); i++) {
								PackageHandle handle = handles.get(i);
								if(clear.get(i)) {
									assertEquals(handle.getNoEntryValue(),
											manager.getValue(item, handle));
								} else {
									assertNotEquals(handle.getNoEntryValue(),
											manager.getValue(item, handle));
								}
							}
						}
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues()}.
					 */
					@Test
					void testHasValuesEmpty() {
						assertFalse(manager.hasValues());
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues()}.
					 */
					@Test
					void testHasValuesUnfilled() {
						manager.register(item());
						assertTrue(manager.hasValues());
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues()}.
					 */
					@Test
					void testHasValuesFilled() {
						Object item = item();
						manager.register(item);
						manager.setString(item, handles.get(0), rand.randomString(10));
						assertTrue(manager.hasValues());
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues(java.lang.Object)}.
					 */
					@Test
					void testHasValuesEEmpty() {
						assertFalse(manager.hasValues(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues(java.lang.Object)}.
					 */
					@Test
					void testHasValuesEForeign() {
						manager.register(item());
						assertFalse(manager.hasValues(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues(java.lang.Object)}.
					 */
					@Test
					void testHasValuesERegistered() {
						Object item = item();
						manager.register(item);
						assertTrue(manager.hasValues(item));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#collectHandles(java.lang.Object, java.util.Collection, java.util.function.Consumer)}.
					 */
					@RepeatedTest(RUNS)
					void testCollectHandles() {
						// Decide on random handles to be used
						int[] indices = rand.ints(0, handles.size())
								.distinct()
								.limit(handles.size()/2)
								.toArray();

						Object item = item();
						manager.register(item);

						// Set actual values
						Set<PackageHandle> expected = new HashSet<>();
						for(int index : indices) {
							PackageHandle handle = handles.get(index);
							Object value = generators.get(index).get();

							manager.setValue(item, handle, value);

							// We're only interested in handles with non-default values
							if(!value.equals(handle.getNoEntryValue())) {
								expected.add(handle);
							}
						}

						Set<PackageHandle> actual = new HashSet<>();
						boolean result = manager.collectUsedHandles(item, expected, actual::add);

						assertEquals(expected, actual);
						if(expected.isEmpty()) {
							assertFalse(result);
						} else {
							assertTrue(result);
						}
					}

				}

				@Nested
				class ForRegistration {

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#register(java.lang.Object)}.
					 */
					@Test
					void testRegister() {
						assertTrue(manager.register(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#register(java.lang.Object)}.
					 */
					@Test
					void testRegisterRepeated() {
						Object item = item();
						assertTrue(manager.register(item));
						assertFalse(manager.register(item));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.lang.Object)}.
					 */
					@Test
					void testUnregisterEEmpty() {
						assertFalse(manager.unregister(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.lang.Object)}.
					 */
					@Test
					void testUnregisterEForeign() {
						assertTrue(manager.register(item()));
						assertFalse(manager.unregister(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.lang.Object)}.
					 */
					@Test
					void testUnregisterE() {
						Object item = item();
						assertTrue(manager.register(item));
						assertTrue(manager.unregister(item));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.util.function.Supplier)}.
					 */
					@RepeatedTest(RUNS)
					void testUnregisterSupplierOfQextendsE() {
						List<Object> items = Stream.generate(Object::new)
								.limit(rand.random(10, 30))
								.collect(Collectors.toList());

						items.forEach(item -> assertTrue(manager.register(item)));

						int from = rand.random(1, items.size()/2);
						int to = rand.random(items.size()/2 + 1, items.size()-2);

						MutableInteger cursor = new MutableInteger(from);
						Supplier<Object> supplier = () -> {
							int idx = cursor.getAndIncrement();
							return idx<=to ? items.get(idx) : null;
						};

						assertEquals(to-from+1, manager.unregister(supplier));

						cursor.setInt(from);
						Supplier<Object> supplier2 = () -> {
							int idx = cursor.getAndIncrement();
							return idx==from ? items.get(idx) : null;
						};

						assertEquals(0, manager.unregister(supplier2));

						for (int i = 0; i < items.size(); i++) {
							Object item = items.get(i);
							if(i>=from && i<=to) {
								assertFalse(manager.isRegistered(item));
							} else {
								assertTrue(manager.isRegistered(item));
							}
						}
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isRegistered(java.lang.Object)}.
					 */
					@Test
					void testIsRegistered() {
						Object item = item();
						assertTrue(manager.register(item));
						assertTrue(manager.isRegistered(item));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isRegistered(java.lang.Object)}.
					 */
					@Test
					void testIsRegisteredEmpty() {
						assertFalse(manager.isRegistered(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isRegistered(java.lang.Object)}.
					 */
					@Test
					void testIsRegisteredForeign() {
						manager.register(item());
						assertFalse(manager.isRegistered(item()));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isUsed(Object)}.
					 */
					@Test
					void testIsUsedUnwritten() {
						Object item = item();
						assertTrue(manager.register(item));
						assertFalse(manager.isUsed(item));
					}
				}

			}
		}

		/**
		 * Note: The following tests are NOT meant to provide proof of thread-safety, but
		 * merely as a cheap way to potentially flag missing thread-safety.
		 *
		 * @author Markus Gärtner
		 *
		 */
		@Nested
		@DisabledOnCi
		class ConcurrentStress {

			@DisabledOnCi
			@Tag(CONCURRENT)
			@TestFactory
			@DisplayName("readers+writers, single handle+item, all types")
			Stream<DynamicNode> testReadAndWriteSingleHandle(TestReporter reporter) throws Exception {

				return IntStream.of(1, 2, 10, 100).boxed() // writers
						.flatMap(writers -> IntStream.of(1, 2, 10, 100).boxed() // readers
								.map(readers -> Pair.pair(writers, readers)))
						.map(pRW -> dynamicContainer(String.format("writers=%d readers=%d", pRW.first, pRW.second),
								IntStream.range(0, _types.length) // only pick the deterministic subset of handles
								.mapToObj(i -> Pair.pair(handles.get(i), _gen[i]))
								.map(pHG -> dynamicTest(pHG.first.getConverter().getValueType().getName(), () -> {
									PackageHandle handle = pHG.first;
									Supplier<?> gen = pHG.second;

									PackedDataManager<Object, Object> manager = PackedDataManager.builder()
										.allowBitPacking(true)
										.defaultStorageSource()
										.addHandles(singleton(handle))
										.initialCapacity(10)
										.collectStats(true)
										.build();

									Object owner = new Object();
									manager.addNotify(owner);

									Object item = "item_0";
									manager.register(item);

									Object[] values = Stream.concat(
												Stream.generate(gen)
												.limit(100),
												Stream.of(handle.getNoEntryValue()))
											.toArray();

									Set<Object> legalValues = set(values);

									int writers = pRW.first.intValue();
									int readers = pRW.second.intValue();

									int writes = 100_000;
									int reads = 100_000;

									try(Circuit circuit = new Circuit(writers+readers)) {

										LongAdder totalWrites = new LongAdder();
										LongAdder totalReads = new LongAdder();

										// Add writer tasks
										for (int i = 0; i < writers; i++) {
											circuit.addTask(() -> {
												for (int j = 0; j < writes; j++) {
													Object value = values[j%values.length];
													manager.setValue(item, handle, value);
													totalWrites.increment();
												}
											});
										}
										// Add reader tasks
										for (int i = 0; i < readers; i++) {
											circuit.addTask(() -> {
												for (int j = 0; j < reads; j++) {
													Object value = manager.getValue(item, handle);
													assertTrue(legalValues.contains(value),
															String.format("Cycle %d: expected %s to be a legal value",
																	Integer.valueOf(j), value));
													totalReads.increment();
												}
											});
										}

										Duration duration = circuit.executeAndWait();

										reporter.publishEntry(String.format(
												"type=%s writers=%d writeOps=%d readers=%d readOps=%d duration=%s stats=%s",
												handle.getConverter().getValueType(),
												Integer.valueOf(writers), Long.valueOf(totalWrites.sum()),
												Integer.valueOf(readers), Long.valueOf(totalReads.sum()),
												duration, manager.getStats()));
									}
								}))));
			}

			@Nested
			class WithFixedManager {

				private int itemCount = 100;
				private int valuesPerCombo = 10;

				PackedDataManager<Object, Object> manager;

				Object owner;
				Object[] items;

				@BeforeEach
				void setUp() {
					manager = PackedDataManager.builder()
							.allowBitPacking(true)
							.defaultStorageSource()
							.addHandles(handles)
							.initialCapacity(10)
							.collectStats(true)
//							.failForUnwritten(true)
							.build();

					owner = new Object();
					manager.addNotify(owner);
					items = rand.randomContent(itemCount);
					Stream.of(items).forEach(manager::register);
				}

				@AfterEach
				void tearDown() {
					Stream.of(items).forEach(manager::unregister);

					manager.removeNotify(owner);
					manager = null;
					owner = null;
					items = null;
				}

				@DisabledOnCi
				@Tag(CONCURRENT)
				@ParameterizedTest(name="write [{index}] writers={0}")
				@DisplayName("writers only")
				@ValueSource(ints = {1, 2, 10, 20, 100})
				void testConcurrentWrite(int writers,
						TestReporter reporter) throws Exception {
					LongAdder totalWrites = new LongAdder();

					try(Circuit circuit = new Circuit(writers)) {
						for (int i = 0; i < writers; i++) {
							circuit.addTask(() -> {
								int steps = rand.random(100, 10_000);
								for (int j = 0; j < steps; j++) {
									int index = rand.random(0, handles.size());

									Object value = generators.get(index).get();
									PackageHandle handle = handles.get(index);
									Object item = rand.random(items);

									assertTrue(manager.isRegistered(item));

									manager.setValue(item, handle, value);
								}
								totalWrites.add(steps);
							});
						}

						Duration duration = circuit.executeAndWait(2, TimeUnit.SECONDS);

						reporter.publishEntry(String.format(
								"writers=%d entries=%d time=%s stats=%s",
								Integer.valueOf(writers), Long.valueOf(totalWrites.sum()),
								duration, manager.getStats()));
					}
				}

				@DisabledOnCi
				@Tag(CONCURRENT)
				@ParameterizedTest(name="read+write [{index}] writers={0} readers={1}")
				@DisplayName("readers+writers, fixed data")
				@CsvSource({
					// writers, readers
					"1,   1",
					"1,   20",
					"1,   100",
					"5,   20",
					"5,   100",
					"20,  20",
					"20,  5",
					"100, 5",
					"100, 1",
				})
				void testConcurrentReadAndWriteFixed(int writers, int readers,
						TestReporter reporter) throws Exception {

					Map<String, PackageHandle> handleLookup = new Object2ObjectOpenHashMap<>();
					handles.forEach(handle -> handleLookup.put((String) handle.getSource(), handle));

					// item, handle, value
					Map<Object, Map<String, Set<Object>>> data = new Object2ObjectOpenHashMap<>();

					// Generate fixed data
					for (Object item : items) {
						Map<String, Set<Object>> map = new Object2ObjectOpenHashMap<>();
						for (int i = 0; i < handles.size(); i++) {
							PackageHandle handle = handles.get(i);
							Set<Object> values = new ObjectOpenHashSet<>();

							Object noEntryValue = handle.getNoEntryValue();
							values.add(noEntryValue);

							for (int j = 0; j < valuesPerCombo; j++) {
								Object value = generators.get(i).get();
								values.add(value);
								manager.setValue(item, handle, handle.getNoEntryValue());
							}
							map.put((String)handle.getSource(), values);
						}
						data.put(item, map);
					}

					LongAdder totalWrites = new LongAdder();
					LongAdder totalReads = new LongAdder();

					BiConsumer<Object, PackageHandle> verifier = (item, handle) -> {
						Object value = manager.getValue(item, handle);
						Map<String, Set<Object>> map = data.get(item);
						assertNotNull(map, "No data for tiem: "+item);
						Set<Object> values = map.get(handle.getSource());
						assertNotNull(values, String.format("No values for %s/%s", item, handle.getSource()));

						if(!values.contains(value))
							throw new AssertionFailedError(String.format(
									"Unrecognized combo after %d reads: %s/%s/%s - lega values are %s",
										Long.valueOf(totalReads.sum()), item, handle.getSource(),value, values));
						totalReads.increment();
					};

					try(Circuit circuit = new Circuit(writers+readers)) {
						// Add writer tasks
						for (int i = 0; i < writers; i++) {
							circuit.addTask(() -> {
								Object[] items = WithFixedManager.this.items.clone();
								String[] keys = handleLookup.keySet().toArray(new String[0]);
								rand.shuffle(items);
								rand.shuffle(keys);

								for(Object item : items) {
									for(String key : keys) {
										Object[] values = data.get(item).get(key).toArray();
										rand.shuffle(values);
										for(Object value : values) {
											manager.setValue(item, handleLookup.get(key), value);
										}
									}
								}
							});
						}
						// Add reader tasks
						for (int i = 0; i < readers; i++) {
							circuit.addTask(() -> {
								for(Object item : items) {
									for(PackageHandle handle : handles) {
										verifier.accept(item, handle);
									}
								}
							});
						}

						Duration duration = circuit.executeAndWait(0, TimeUnit.SECONDS);

						reporter.publishEntry(String.format(
								"writers=%d writeOps=%d readers=%d readOps=%d time=%s stats=%s",
								Integer.valueOf(writers), Long.valueOf(totalWrites.sum()),
								Integer.valueOf(readers), Long.valueOf(totalReads.sum()),
								duration, manager.getStats()));
					}
				}

				@DisabledOnCi
				@Tag(CONCURRENT)
				@ParameterizedTest(name="read+write [{index}] writers={0} readers={1}")
				@DisplayName("readers+writers, randomized data")
				@CsvSource({
					// writers, readers
					"1,   1",
					"1,   20",
					"1,   100",
					"5,   20",
					"5,   100",
					"20,  20",
					"20,  5",
					"100, 5",
					"100, 1",
				})
				void testConcurrentReadAndWriteRandom(int writers, int readers,
						TestReporter reporter) throws Exception {

					// item, handle, value
					Set<Triple<Object, PackageHandle, Object>> legalCombinations
						= Collections.synchronizedSet(new ObjectOpenHashSet<>());

					for(Object item : items) {
						for(PackageHandle handle : handles) {
							legalCombinations.add(nullableTriple(item, handle, handle.getNoEntryValue()));
							manager.setValue(item, handle, handle.getNoEntryValue());
						}
					}

					LongAdder totalWrites = new LongAdder();
					LongAdder totalReads = new LongAdder();

					LongAdder unsetReads = new LongAdder();
					LongAdder comparisons = new LongAdder();

					Consumer<Object> writer = item -> {
						int index = rand.random(0, handles.size());

						Object value = generators.get(index).get();
						PackageHandle handle = handles.get(index);

						assertTrue(manager.isRegistered(item));

						legalCombinations.add(nullableTriple(item, handle, value));

						manager.setValue(item, handle, value);

						assertTrue(manager.isUsed(item));
					};

					Consumer<Object> reader = item -> {
						int index = rand.random(0, handles.size());

						PackageHandle handle = handles.get(index);

						assertTrue(manager.isRegistered(item));

						Object value = manager.getValue(item, handle);

						if(Objects.equals(handle.getNoEntryValue(), value)) {
							unsetReads.increment();
							return;
						}

						assertTrue(manager.isUsed(item));

						Triple<Object, PackageHandle, Object> combo =
								nullableTriple(item, handle, value);
						comparisons.increment();

						assertTrue(legalCombinations.contains(combo),
								() -> "Unrecognized combo: "+combo);
					};

					// Write _something_ initially for every item, so we guarantee actual readable content
	//				Stream.of(items).forEach(writer);

					try(Circuit circuit = new Circuit(writers+readers)) {
						// Add writer tasks
						for (int i = 0; i < writers; i++) {
							circuit.addTask(() -> {
								int steps = rand.random(10, 1000);
								for (int j = 0; j < steps; j++) {
									writer.accept(rand.random(items));
								}
								totalWrites.add(steps);
							});
						}
						// Add reader tasks
						for (int i = 0; i < readers; i++) {
							circuit.addTask(() -> {
								int steps = rand.random(100, 50_000);
								for (int j = 0; j < steps; j++) {
									reader.accept(rand.random(items));
								}
								totalReads.add(steps);
							});
						}

						Duration duration = circuit.executeAndWait(0, TimeUnit.SECONDS);

						reporter.publishEntry(String.format(
								"writers=%d writeOps=%d readers=%d readOps=%d unsetReads=%d comparisons=%d time=%s stats=%s",
								Integer.valueOf(writers), Long.valueOf(totalWrites.sum()),
								Integer.valueOf(readers), Long.valueOf(totalReads.sum()),
								Long.valueOf(unsetReads.sum()), Long.valueOf(comparisons.sum()),
								duration, manager.getStats()));
					}
				}

			}
		}
	}
}
