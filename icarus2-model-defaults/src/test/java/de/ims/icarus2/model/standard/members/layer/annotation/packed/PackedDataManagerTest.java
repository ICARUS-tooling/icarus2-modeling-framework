/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.assertUnsupportedType;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackageHandle;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataManager;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackedDataUtils;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.util.PartTest;

/**
 * @author Markus Gärtner
 *
 */
class PackedDataManagerTest {

	private static final Set<ValueType> NUMBERS =
			set(ValueType.INTEGER, ValueType.LONG, ValueType.FLOAT, ValueType.DOUBLE);

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

	private static Object noEntryValue(ValueType type) {
		return _gen[indexOfType(type)].get();
	}

	private static Set<ValueType> typesForGetters(PackageHandle handle) {
		ValueType type = handle.getConverter().getValueType();
//		if(NUMBERS.contains(type)) {
//			return NUMBERS;
//		}
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

		@BeforeEach
		void setUp() {
			IntStream.range(0, 20)
				.forEach(i -> {
					int index = i<_types.length ? i : random(0, _types.length);
					ValueType type = _types[index];
					String key = key(i);
					handles.add(createHandle(type, key, true));
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
				manager.addNotify(owner);
			}

			@AfterEach
			void tearDown() {
				manager.removeNotify(owner);
				manager = null;
				owner = null;
			}

			@Nested
			class ForHandleLookups {

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#registerHandles(java.util.Set)}.
				 */
				@Test
				void testRegisterHandles() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregisterHandles(java.util.Set)}.
				 */
				@Test
				void testUnregisterHandles() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
				 */
				@Test
				void testLookupHandle() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@Test
				void testLookupHandles() {
					fail("Not yet implemented"); // TODO
				}

			}

		}

		@Nested
		class WithFixedInstance implements PartTest<Object, PackedDataManager<Object, Object>> {
			PackedDataManager<Object, Object> manager;
			Object owner;

			@BeforeEach
			void setUp() {
				owner = new Object();
				manager = PackedDataManager.builder()
						.allowBitPacking(true)
						.defaultStorageSource()
						.addHandles(handles)
						.initialCapacity(10)
						.build();
				manager.addNotify(owner);
			}

			@AfterEach
			void tearDown() {
				manager.removeNotify(owner);
				manager = null;
				owner = null;
			}

			@Override
			public PackedDataManager<Object, Object> createTestInstance(TestSettings settings) {
				return settings.process(manager);
			}

			@Override
			public Object createEnvironment() {
				return owner;
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
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregisterHandles(java.util.Set)}.
				 */
				@Test
				void testUnregisterHandles() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandle(de.ims.icarus2.model.manifest.api.AnnotationManifest)}.
				 */
				@Test
				void testLookupHandle() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#lookupHandles(java.util.Set)}.
				 */
				@Test
				void testLookupHandles() {
					fail("Not yet implemented"); // TODO
				}

			}

			@Nested
			class ForValueChecks {

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#clear(java.util.function.IntSupplier, de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle[])}.
				 */
				@Test
				void testClearIntSupplierPackageHandleArray() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#clear(de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager.PackageHandle[])}.
				 */
				@Test
				void testClearPackageHandleArray() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues()}.
				 */
				@Test
				void testHasValues() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#hasValues(java.lang.Object)}.
				 */
				@Test
				void testHasValuesE() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#collectHandles(java.lang.Object, java.util.Collection, java.util.function.Consumer)}.
				 */
				@Test
				void testCollectHandles() {
					fail("Not yet implemented"); // TODO
				}

			}

			@Nested
			class ForRegistration {

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#register(java.lang.Object)}.
				 */
				@Test
				void testRegister() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.lang.Object)}.
				 */
				@Test
				void testUnregisterE() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#unregister(java.util.function.Supplier)}.
				 */
				@Test
				void testUnregisterSupplierOfQextendsE() {
					fail("Not yet implemented"); // TODO
				}

				/**
				 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.PackedDataManager#isRegistered(java.lang.Object)}.
				 */
				@Test
				void testIsRegistered() {
					fail("Not yet implemented"); // TODO
				}

			}
		}
	}
}
