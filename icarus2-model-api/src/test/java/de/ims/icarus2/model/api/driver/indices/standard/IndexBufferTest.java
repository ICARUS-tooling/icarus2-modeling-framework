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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.test.TestUtils.assertIOOB;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;

/**
 * @author Markus Gärtner
 *
 */
class IndexBufferTest implements RandomAccessIndexSetTest<IndexBuffer> {

	private static final Function<Config, IndexSet> constructor = config -> {
		long[] indices = config.getIndices();
		IndexBuffer buffer = new IndexBuffer(config.getValueType(), indices.length);
		buffer.add(indices);
		return buffer;
	};

	private static int randomSize() {
		return random(10, 100);
	}

	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.map(type -> new Config()
						.valueType(type)
						.defaultFeatures())
				.flatMap(config -> Stream.of(
						// Empty
						config.clone()
							.label(config.getValueType()+" empty")
							.indices()
							.sorted(true)
							.set(new IndexBuffer(config.getValueType(), randomSize())),
						// Sorted
						config.clone()
							.label(config.getValueType()+" sorted")
							.sortedIndices(randomSize())
							.sorted(true)
							.set(constructor),
						// Random
						config.clone()
							.label(config.getValueType()+" random")
							.randomIndices(randomSize())
							.set(constructor)
						));
	}

	@Override
	public Class<?> getTestTargetClass() {
		return IndexBuffer.class;
	}

	@Override
	public IndexBuffer createTestInstance(TestSettings settings) {
		return settings.process(new IndexBuffer(10));
	}

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {1, 10, 10_000})
		void testIndexBufferInt(int capacity) {
			IndexBuffer buffer = new IndexBuffer(capacity);
			assertEquals(capacity, buffer.remaining());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MAX_VALUE})
		void testIndexBufferIntInvalidCapacity(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new IndexBuffer(capacity));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(de.ims.icarus2.model.api.driver.indices.IndexValueType, int)}.
		 */
		@TestFactory
		Stream<DynamicNode> testIndexBufferIndexValueTypeInt() {
			return Stream.of(IndexValueType.values())
					.map(type -> dynamicContainer(type.name(),
							IntStream.of(1, 10, 10_000).mapToObj(capacity ->
								dynamicTest(String.valueOf(capacity), () -> {
									IndexBuffer buffer = new IndexBuffer(type, capacity);
									assertEquals(capacity, buffer.remaining());
									assertEquals(type, buffer.getIndexValueType());
								}))));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#IndexBuffer(de.ims.icarus2.model.api.driver.indices.IndexValueType, int)}.
		 */
		@ParameterizedTest
		@ValueSource(ints = {0, -1, Integer.MAX_VALUE})
		void testIndexBufferIndexValueTypeInt(int capacity) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new IndexBuffer(random(IndexValueType.values()), capacity));
		}

	}

	@Nested
	class WithInstance {
		private IndexBuffer buffer;
		int capacity;

		@BeforeEach
		void setUp() {
			capacity = randomSize();
			buffer = new IndexBuffer(capacity);
		}

		@AfterEach
		void tearDown() {
			buffer = null;
		}

		@Nested
		class WhenEmpty {

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#size()}.
			 */
			@Test
			void testSize() {
				assertEquals(0, buffer.size());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#isEmpty()}.
			 */
			@Test
			void testIsEmpty() {
				assertTrue(buffer.isEmpty());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#externalize()}.
			 */
			@Test
			void testExternalize() {
				IndexSet ex = buffer.externalize();
				assertNotNull(ex);
				assertTrue(ex.isEmpty());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#snapshot()}.
			 */
			@Test
			void testSnapshot() {
				assertNull(buffer.snapshot());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#remaining()}.
			 */
			@Test
			void testRemaining() {
				assertEquals(capacity, buffer.remaining());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#indexAt(int)}.
			 */
			@Test
			void testIndexAt() {
				assertIOOB(() -> buffer.indexAt(0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#firstIndex()}.
			 */
			@Test
			void testFirstIndex() {
				assertEquals(UNSET_LONG, buffer.firstIndex());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#lastIndex()}.
			 */
			@Test
			void testLastIndex() {
				assertEquals(UNSET_LONG, buffer.lastIndex());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#export(int, int, byte[], int)}.
			 */
			@Test
			void testExportIntIntByteArrayInt() {
				assertIOOB(() -> buffer.export(0, 0, new byte[capacity], 0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#export(int, int, short[], int)}.
			 */
			@Test
			void testExportIntIntShortArrayInt() {
				assertIOOB(() -> buffer.export(0, 0, new short[capacity], 0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#export(int, int, int[], int)}.
			 */
			@Test
			void testExportIntIntIntArrayInt() {
				assertIOOB(() -> buffer.export(0, 0, new int[capacity], 0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#export(int, int, long[], int)}.
			 */
			@Test
			void testExportIntIntLongArrayInt() {
				assertIOOB(() -> buffer.export(0, 0, new long[capacity], 0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#subSet(int, int)}.
			 */
			@Test
			void testSubSet() {
				assertIOOB(() -> buffer.subSet(0, 0));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#sort()}.
			 */
			@Test
			void testSort() {
				assertTrue(buffer.sort());
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#isSorted()}.
			 */
			@Test
			void testIsSorted() {
				assertTrue(buffer.isSorted());
			}

		}
	}

	abstract static class EqualityTest {

		private IndexBuffer buffer;
		private int size;
		//TODO

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#clear()}.
		 */
		@Test
		void testClear() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#snapshot()}.
		 */
		@Test
		void testSnapshot() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#remaining()}.
		 */
		@Test
		void testRemaining() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long)}.
		 */
		@Test
		void testAddLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long, long)}.
		 */
		@Test
		void testAddLongLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.function.IntSupplier)}.
		 */
		@Test
		void testAddIntSupplier() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.function.LongSupplier)}.
		 */
		@Test
		void testAddLongSupplier() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(byte[])}.
		 */
		@Test
		void testAddByteArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(byte[], int, int)}.
		 */
		@Test
		void testAddByteArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(short[])}.
		 */
		@Test
		void testAddShortArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(short[], int, int)}.
		 */
		@Test
		void testAddShortArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(int[])}.
		 */
		@Test
		void testAddIntArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(int[], int, int)}.
		 */
		@Test
		void testAddIntArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long[])}.
		 */
		@Test
		void testAddLongArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(long[], int, int)}.
		 */
		@Test
		void testAddLongArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet)}.
		 */
		@Test
		void testAddIndexSet() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet, int)}.
		 */
		@Test
		void testAddIndexSetInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet, int, int)}.
		 */
		@Test
		void testAddIndexSetIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(de.ims.icarus2.model.api.driver.indices.IndexSet[])}.
		 */
		@Test
		void testAddIndexSetArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItem(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testAddFromItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.function.Supplier)}.
		 */
		@Test
		void testAddFromItemsSupplierOfQextendsItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(de.ims.icarus2.model.api.members.item.Item[])}.
		 */
		@Test
		void testAddFromItemsItemArray() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(de.ims.icarus2.model.api.members.item.Item[], int, int)}.
		 */
		@Test
		void testAddFromItemsItemArrayIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.List)}.
		 */
		@Test
		void testAddFromItemsListOfQextendsItem() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.List, int)}.
		 */
		@Test
		void testAddFromItemsListOfQextendsItemInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#addFromItems(java.util.List, int, int)}.
		 */
		@Test
		void testAddFromItemsListOfQextendsItemIntInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.PrimitiveIterator.OfLong)}.
		 */
		@Test
		void testAddOfLong() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.PrimitiveIterator.OfInt)}.
		 */
		@Test
		void testAddOfInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.stream.LongStream)}.
		 */
		@Test
		void testAddLongStream() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#add(java.util.stream.IntStream)}.
		 */
		@Test
		void testAddIntStream() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#accept(int)}.
		 */
		@Test
		void testAcceptInt() {
			fail("Not yet implemented"); // TODO
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer#accept(long)}.
		 */
		@Test
		void testAcceptLong() {
			fail("Not yet implemented"); // TODO
		}
	}
}
