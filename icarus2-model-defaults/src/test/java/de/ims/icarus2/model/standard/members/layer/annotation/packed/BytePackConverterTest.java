/**
 *
 */
package de.ims.icarus2.model.standard.members.layer.annotation.packed;

import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.test.TestUtils.randomString;
import static de.ims.icarus2.test.util.Pair.pair;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.collections.CollectionUtils.set;
import static de.ims.icarus2.util.collections.CollectionUtils.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import de.ims.icarus2.model.api.layer.annotation.AnnotationStorageTest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.PackageHandle;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.BitwiseBooleanConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.BooleanConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.DoubleConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.FloatConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.IntConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.LongConverter;
import de.ims.icarus2.model.standard.members.layers.annotation.packed.BytePackConverter.SubstitutingConverterInt;
import de.ims.icarus2.test.util.Pair;
import de.ims.icarus2.util.collections.LookupList;
import de.ims.icarus2.util.mem.ByteAllocator;
import de.ims.icarus2.util.mem.ByteAllocator.Cursor;

/**
 * @author Markus Gärtner
 *
 */
class BytePackConverterTest {

	abstract class BaseTest<C extends BytePackConverter> {

		ByteAllocator alloc;
		Cursor cursor;
		PackageHandle handle;

		@SuppressWarnings("boxing")
		@BeforeEach
		void setUp() {
			alloc = new ByteAllocator(ByteAllocator.MIN_SLOT_SIZE, ByteAllocator.MIN_CHUNK_POWER);
			cursor = alloc.newCursor();
			cursor.moveTo(alloc.alloc());
			handle = mock(PackageHandle.class);
			when(handle.getIndex()).thenReturn(0);
			when(handle.getBit()).thenReturn(0);
		}

		@AfterEach
		void tearDown() {
			alloc.clear();
			alloc = null;
			cursor = null;
			handle = null;
		}

		Set<ValueType> types() {
			return singleton(valueType());
		}

		abstract ValueType valueType();

		abstract Stream<Pair<String, C>> instances();

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverter#getValueType()}.
		 */
		@TestFactory
		Stream<DynamicTest> testGetValueType() {
			return instances().map(p -> dynamicTest(p.first,
					() -> assertEquals(valueType(), p.second.getValueType())));
		}

		@TestFactory
		Stream<DynamicTest> testBoolean() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.BOOLEAN)) {
					converter.setBoolean(handle, cursor, true);
					assertTrue(converter.getBoolean(handle, cursor));

					converter.setBoolean(handle, cursor, false);
					assertFalse(converter.getBoolean(handle, cursor));
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setBoolean(handle, cursor, random().nextBoolean()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getBoolean(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testInteger() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.INTEGER)) {
					IntStream.generate(random()::nextInt)
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setInteger(handle, cursor, value);
							assertEquals(value, converter.getInteger(handle, cursor));
						});
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setInteger(handle, cursor, random().nextInt()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getInteger(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testLong() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.LONG)) {
					LongStream.generate(random()::nextLong)
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setLong(handle, cursor, value);
							assertEquals(value, converter.getLong(handle, cursor));
						});
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setLong(handle, cursor, random().nextLong()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getLong(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testFloat() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.FLOAT)) {
					DoubleStream.generate(random()::nextFloat)
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setFloat(handle, cursor, (float) value);
							assertEquals((float)value, converter.getFloat(handle, cursor));
						});
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setFloat(handle, cursor, random().nextFloat()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getFloat(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testDouble() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.DOUBLE)) {
					DoubleStream.generate(random()::nextDouble)
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setDouble(handle, cursor, value);
							assertEquals(value, converter.getDouble(handle, cursor));
						});
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setDouble(handle, cursor, random().nextDouble()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getDouble(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testValue() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.CUSTOM)) {
					Stream.generate(Object::new)
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setValue(handle, cursor, value);
							assertEquals(value, converter.getValue(handle, cursor));
						});
				} else if(valueType().isPrimitiveType()) {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setValue(handle, cursor, new Object()));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getValue(handle, cursor));
				}
			}));
		}

		@TestFactory
		Stream<DynamicTest> testString() {
			return instances().map(p -> dynamicTest(p.first, () -> {
				C converter = p.second;

				if(types().contains(ValueType.STRING)) {
					Stream.generate(() -> randomString(20))
						.limit(random(10, 20))
						.forEach(value -> {
							converter.setString(handle, cursor, value);
							assertEquals(value, converter.getString(handle, cursor));
						});
				} else {
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.setString(handle, cursor, randomString(10)));
					AnnotationStorageTest.assertUnsupportedType(
							() -> converter.getString(handle, cursor));
				}
			}));
		}

	}

	@Nested
	class ForPackedBoolean extends BaseTest<BitwiseBooleanConverter> {

		@Override
		ValueType valueType() {
			return ValueType.BOOLEAN;
		}

		@Override
		Stream<Pair<String, BitwiseBooleanConverter>> instances() {
			return Stream.of(pair("default", new BitwiseBooleanConverter()));
		}

		@SuppressWarnings("boxing")
		@TestFactory
		Stream<DynamicTest> testBitOffset() {
			return IntStream.range(0, 8)
					.mapToObj(bit -> dynamicTest("bit "+bit, () -> {
						when(handle.getBit()).thenReturn(bit);
						BitwiseBooleanConverter converter = new BitwiseBooleanConverter();

						converter.setBoolean(handle, cursor, true);
						assertTrue(converter.getBoolean(handle, cursor));

						converter.setBoolean(handle, cursor, false);
						assertFalse(converter.getBoolean(handle, cursor));
					}));
		}
	}

	@Nested
	class ForBoolean extends BaseTest<BooleanConverter> {

		@Override
		ValueType valueType() {
			return ValueType.BOOLEAN;
		}

		@Override
		Stream<Pair<String, BooleanConverter>> instances() {
			return Stream.of(pair("default", new BooleanConverter()));
		}
	}

	@Nested
	class ForInt extends BaseTest<IntConverter> {

		@Override
		ValueType valueType() {
			return ValueType.INTEGER;
		}

		@Override
		Stream<Pair<String, IntConverter>> instances() {
			return Stream.of(pair("default", new IntConverter()));
		}
	}

	@Nested
	class ForLong extends BaseTest<LongConverter> {

		@Override
		ValueType valueType() {
			return ValueType.LONG;
		}

		@Override
		Stream<Pair<String, LongConverter>> instances() {
			return Stream.of(pair("default", new LongConverter()));
		}
	}

	@Nested
	class ForFloat extends BaseTest<FloatConverter> {

		@Override
		ValueType valueType() {
			return ValueType.FLOAT;
		}

		@Override
		Stream<Pair<String, FloatConverter>> instances() {
			return Stream.of(pair("default", new FloatConverter()));
		}
	}

	@Nested
	class ForDouble extends BaseTest<DoubleConverter> {

		@Override
		ValueType valueType() {
			return ValueType.DOUBLE;
		}

		@Override
		Stream<Pair<String, DoubleConverter>> instances() {
			return Stream.of(pair("default", new DoubleConverter()));
		}
	}

	@Nested
	class ForSubstitutionObject extends BaseTest<SubstitutingConverterInt<Object>> {

		@Override
		ValueType valueType() {
			return ValueType.CUSTOM;
		}

		/**
		 * @see de.ims.icarus2.model.standard.members.layer.annotation.packed.BytePackConverterTest.BaseTest#types()
		 */
		@Override
		Set<ValueType> types() {
			return set(ValueType.CUSTOM, ValueType.STRING);
		}

		@Override
		Stream<Pair<String, SubstitutingConverterInt<Object>>> instances() {
			return IntStream.rangeClosed(1, 4)
					.mapToObj(bytes -> {
						LookupList<Object> buffer = new LookupList<>();
						return pair("bytes "+bytes,
								new SubstitutingConverterInt<>(ValueType.CUSTOM, bytes,
										value -> {
											int index = buffer.indexOf(value);
											if(index==UNSET_INT) {
												index = buffer.size();
												buffer.add(value);
											}
											return index;
										},
										buffer::get));
					});
		}
	}

	@Nested
	class ForSubstitutionString extends BaseTest<SubstitutingConverterInt<String>> {

		@Override
		ValueType valueType() {
			return ValueType.STRING;
		}

		@Override
		Stream<Pair<String, SubstitutingConverterInt<String>>> instances() {
			return IntStream.rangeClosed(1, 4)
					.mapToObj(bytes -> {
						LookupList<String> buffer = new LookupList<>();
						return pair("bytes "+bytes,
								new SubstitutingConverterInt<>(ValueType.STRING, bytes,
										value -> {
											int index = buffer.indexOf(value);
											if(index==UNSET_INT) {
												index = buffer.size();
												buffer.add(value);
											}
											return index;
										},
										buffer::get));
					});
		}
	}
}
