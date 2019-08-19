/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertUnsupportedType;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertCollectionEquals;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
import static de.ims.icarus2.test.TestUtils.randomSubLists;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackageHandle;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataManager;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataUtils;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.guard.ApiGuard;
import de.ims.icarus2.util.MutablePrimitives.MutableInteger;

/**
 * @author Markus Gärtner
 *
 */
class PackedDataManagerTest {

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
			() -> randomString(20),
			() -> Integer.valueOf(random().nextInt()),
			() -> Long.valueOf(random().nextLong()),
			() -> Double.valueOf(random().nextDouble()*Double.MAX_VALUE),
			() -> Float.valueOf(random().nextFloat()*Float.MAX_VALUE),
			() -> Boolean.valueOf(random().nextBoolean()),
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
		return "test_"+index;
	}

	private static AnnotationManifest mockAnnotationManifest(ValueType type, String key) {
		AnnotationManifest annotationManifest = mock(AnnotationManifest.class);
		when(annotationManifest.getValueType()).thenReturn(type);
		when(annotationManifest.getKey()).thenReturn(Optional.of(key));
		when(annotationManifest.getNoEntryValue()).thenReturn(Optional.ofNullable(noEntryValue(type)));
		return annotationManifest;
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
		AnnotationManifest manifest = mockAnnotationManifest(type, key);
		return PackedDataUtils.createHandle(manifest, allowBitPacking);
	}

	private static Object item() {
		return new Object();
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
					int index = i<_types.length ? i : random(0, _types.length);
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
					return randomSubLists(handles.subList(1, handles.size()), 0.5)
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
					return randomSubLists(handles, 0.5)
							.map(list -> dynamicTest(String.valueOf(list.size()), () -> {
								Set<Object> sources = list.stream()
										.map(PackageHandle::getSource)
										.collect(Collectors.toSet());
								assertCollectionEquals(list, manager.lookupHandles(sources).values());
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
				 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
				 */
				@Override
				public void configureApiGuard(ApiGuard<PackedDataManager<Object, Object>> apiGuard) {
					ApiGuardedTest.super.configureApiGuard(apiGuard);

					apiGuard.detectUnmarkedMethods(true)
						.nullGuard(true);
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getBoolean(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
					 */
					@SuppressWarnings("boxing")
					@TestFactory
					Stream<DynamicTest> testGetBoolean() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									boolean value = random().nextBoolean();
									if(typesForSetters(handle).contains(ValueType.BOOLEAN)) {
										manager.setBoolean(item, handle, value);
										assertEquals(value, manager.getBoolean(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setBoolean(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getInteger(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetInteger() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									int value = random().nextInt();
									if(typesForSetters(handle).contains(ValueType.INTEGER)) {
										manager.setInteger(item, handle, value);
										assertEquals(value, manager.getInteger(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setInteger(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getLong(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetLong() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									long value = random().nextLong();
									if(typesForSetters(handle).contains(ValueType.LONG)) {
										manager.setLong(item, handle, value);
										assertEquals(value, manager.getLong(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setLong(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getFloat(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetFloat() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									float value = random().nextFloat();
									if(typesForSetters(handle).contains(ValueType.FLOAT)) {
										manager.setFloat(item, handle, value);
										assertEquals(value, manager.getFloat(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setFloat(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getDouble(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
					 */
					@TestFactory
					Stream<DynamicTest> testGetDouble() {
						return IntStream.range(0, handles.size())
								.mapToObj(i -> dynamicTest(label(i), () -> {
									PackageHandle handle = handles.get(i);
									Object item = item();
									manager.register(item);
									double value = random().nextDouble();
									if(typesForSetters(handle).contains(ValueType.DOUBLE)) {
										manager.setDouble(item, handle, value);
										assertEquals(value, manager.getDouble(item, handle));
									} else {
										assertUnsupportedType(() -> manager.setDouble(item, handle, value));
									}
								}));
					}

					/**
					 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#getValue(java.lang.Object, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle)}.
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
						return randomSubLists(handles, 0.5)
								.map(list -> dynamicTest(String.valueOf(list.size()), () -> {
									Set<Object> sources = list.stream()
											.map(PackageHandle::getSource)
											.collect(Collectors.toSet());
									assertCollectionEquals(list, manager.lookupHandles(sources).values());
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
								.limit(random(10, 20))
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
						random().ints(0, handles.size())
								.distinct()
								.limit(handles.size()/2)
								.forEach(index -> {
									clearP.set(index);
									tmpP.add(handles.get(index));
								});

						// Decide on random items to be cleared
						BitSet clearI = new BitSet(items.length);
						List<Object> tmpI = new ArrayList<>();
						random().ints(0, items.length)
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
								.limit(random(10, 20))
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
						random().ints(0, handles.size())
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
						manager.setString(item, handles.get(0), randomString(10));
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
						int[] indices = random().ints(0, handles.size())
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
								.limit(random(10, 30))
								.collect(Collectors.toList());

						items.forEach(item -> assertTrue(manager.register(item)));

						int from = random(1, items.size()/2);
						int to = random(items.size()/2 + 1, items.size()-2);

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

				}

			}
		}
	}
}
