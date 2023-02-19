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
package de.ims.icarus2.model.standard.members.item;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static de.ims.icarus2.model.api.ModelTestUtils.mockContainer;
import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.model.api.ModelTestUtils.mockPosition;
import static de.ims.icarus2.model.api.ModelTestUtils.mockRasterizer;
import static de.ims.icarus2.model.api.ModelTestUtils.stubOrder;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.RUNS;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.test.TestUtils.npeAsserter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.id.IdManager;
import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.ItemTest;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.raster.Position;
import de.ims.icarus2.model.api.raster.Rasterizer;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
class DefaultFragmentTest implements ItemTest<Fragment> {

	/**
	 * @see de.ims.icarus2.test.GenericTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends Fragment> getTestTargetClass() {
		return DefaultFragment.class;
	}

	@Nested
	class Constructors {
		@Test
		void testDefaultFragment() {
			assertNotNull(new DefaultFragment());
		}

		//TODO many more to test now
	}

	@Nested
	class WithBareInstance {

		DefaultFragment instance;

		@BeforeEach
		void setUp() {
			instance = new DefaultFragment();
		}

		@AfterEach
		void tearDown() {
			instance = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getMemberType()}.
		 */
		@Test
		void testGetMemberType() {
			assertEquals(MemberType.FRAGMENT, instance.getMemberType());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentBegin()}.
		 */
		@Test
		void testGetFragmentBegin() {
			assertNull(instance.getFragmentBegin());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentEnd()}.
		 */
		@Test
		void testGetFragmentEnd() {
			assertNull(instance.getFragmentEnd());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setItem(de.ims.icarus2.model.api.members.item.Item)}.
		 */
		@Test
		void testSetItem() {
			assertSetter(instance,
					DefaultFragment::setItem, mockItem(),
					NPE_CHECK, NO_CHECK);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getItem()}.
		 */
		@Test
		void testGetItem() {
			assertGetter(instance, mockItem(),
					mockItem(), NO_DEFAULT(),
					DefaultFragment::getItem,
					DefaultFragment::setItem);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getBeginOffset()}.
		 */
		@Test
		void testGetBeginOffset_IllegalState() {
			assertThrows(IllegalStateException.class,
					() -> instance.getBeginOffset());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getEndOffset()}.
		 */
		@Test
		void testGetEndOffset_IllegalState() {
			assertThrows(IllegalStateException.class,
					() -> instance.getEndOffset());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getLayer()}.
		 */
		@Test
		void testGetLayer() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE, () -> instance.getLayer());
		}

		@Nested
		class WithItem {

			LongStream offsets(RandomGenerator rng) {
				return LongStream.concat(
						LongStream.of(),
						rng.longs(10, 2, Long.MAX_VALUE)
				);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getBeginOffset()}.
			 */
			@SuppressWarnings("boxing")
			@ParameterizedTest
			@RandomizedTest
			Stream<DynamicTest> testGetBeginOffset(RandomGenerator rng) {
				return offsets(rng).mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
					Item item = mockItem();
					instance.setItem(item);
					when(item.getBeginOffset()).thenReturn(value);
					assertEquals(value, instance.getBeginOffset());
					verify(item).getBeginOffset();
				}));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getEndOffset()}.
			 */
			@SuppressWarnings("boxing")
			@ParameterizedTest
			@RandomizedTest
			Stream<DynamicTest> testGetEndOffset(RandomGenerator rng) {
				return offsets(rng).mapToObj(value -> dynamicTest(String.valueOf(value), () -> {
					Item item = mockItem();
					instance.setItem(item);
					when(item.getEndOffset()).thenReturn(value);
					assertEquals(value, instance.getEndOffset());
					verify(item).getEndOffset();
				}));
			}
		}
	}

	/**
	 * Needs a working container, layer and {@link IdManager}
	 * @author Markus Gärtner
	 *
	 */
	@RandomizedTest
	@Nested
	class WithComplexEnvironment {

		private IdManager idManager;
		private FragmentLayer layer;
		private Rasterizer rasterizer;
		private Container container;
		private Item item;
		private long id, index;

		private long axisSize;

		private DefaultFragment instance;

		private Position begin1, begin2, end1, end2;

		@SuppressWarnings("boxing")
		@BeforeEach
		void setUp(RandomGenerator rng) {
			idManager = mock(IdManager.class);
			layer = mock(FragmentLayer.class);
			container = mockContainer();
			item = mockItem();
			rasterizer = mockRasterizer(1);

			/*
			 * |....|......|.........|
			 * 0   used   size   Long.MAX
			 */
			axisSize = Math.max(20L, rng.nextLong()/2);

			begin1 = mockPosition(rng.nextInt(8));
			end1 = mockPosition(axisSize/2-2);
			begin2 = mockPosition(rng.nextInt(5));
			end2 = mockPosition(axisSize/2-1);

			stubOrder(rasterizer, begin1, end1);
			stubOrder(rasterizer, begin2, end1);
			stubOrder(rasterizer, begin1, end2);
			stubOrder(rasterizer, begin2, end2);

			when(container.getLayer()).thenReturn(layer);
			when(layer.getIdManager()).thenReturn(idManager);
			when(layer.getRasterizer()).thenReturn(rasterizer);

			when(layer.getRasterSize(eq(item), eq(0))).thenReturn(axisSize);

			id = Math.max(1L, rng.nextLong());
			index = Math.max(1L, rng.nextLong());

			instance = new DefaultFragment();
			instance.setContainer(container);
			instance.setId(id);
		}

		@AfterEach
		void tearDown() {
			idManager = null;
			layer = null;
			item = null;
			rasterizer = null;
			container = null;
			id = IcarusUtils.UNSET_LONG;
			index = IcarusUtils.UNSET_LONG;
			instance = null;
			end1 = begin1 = null;
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#revive()}.
		 */
		@SuppressWarnings("boxing")
		@RepeatedTest(value=RUNS)
		void testRevive() {
			when(idManager.indexOfId(anyLong())).thenReturn(IcarusUtils.UNSET_LONG);

			assertFalse(instance.revive());

			when(idManager.indexOfId(id)).thenReturn(index);

			assertFalse(instance.revive());

			instance.setItem(item);

			assertFalse(instance.revive());

			instance.setFragmentBegin(begin1);

			assertFalse(instance.revive());

			instance.setFragmentEnd(end1);

			assertTrue(instance.revive());
		}


		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#recycle()}.
		 */
		@Test
		void testRecycle() {
			instance.setItem(item);
			instance.setFragmentBegin(begin1);
			instance.setFragmentEnd(end1);

			assertSame(item, instance.getItem());
			assertSame(begin1, instance.getFragmentBegin());
			assertSame(end1, instance.getFragmentEnd());

			instance.recycle();

			assertNull(instance.getItem());
			assertNull(instance.getFragmentBegin());
			assertNull(instance.getFragmentEnd());
		}

		@Nested
		class WithPresetItem {

			@BeforeEach
			void setUp() {
				instance.setItem(item);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentBegin()}.
			 */
			@RepeatedTest(value=RUNS)
			void testGetFragmentBegin() {
				assertGetter(instance,
						begin1, begin2,
						NO_DEFAULT(),
						Fragment::getFragmentBegin,
						Fragment::setFragmentBegin);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentEnd()}.
			 */
			@RepeatedTest(value=RUNS)
			void testGetFragmentEnd() {
				assertGetter(instance,
						end1, end2,
						NO_DEFAULT(),
						Fragment::getFragmentEnd,
						Fragment::setFragmentEnd);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentBegin(de.ims.icarus2.model.api.raster.Position)}.
			 */
			@RepeatedTest(value=RUNS)
			void testSetFragmentBegin() {
				assertSetter(instance,
						Fragment::setFragmentBegin,
						begin1, NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)}.
			 */
			@RepeatedTest(value=RUNS)
			void testSetFragmentEnd() {
				assertSetter(instance,
						Fragment::setFragmentEnd,
						end1, NPE_CHECK,
						NO_CHECK);
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setSpan(de.ims.icarus2.model.api.raster.Position, de.ims.icarus2.model.api.raster.Position)}.
			 */
			@TestFactory
			Stream<DynamicTest> testSetSpanNull() {
				return Stream.of(
						dynamicTest("begin null", npeAsserter(() -> instance.setSpan(null, end1))),
						dynamicTest("end null", npeAsserter(() -> instance.setSpan(begin1, null))),
						dynamicTest("both null", npeAsserter(() -> instance.setSpan(null, null))));
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setSpan(de.ims.icarus2.model.api.raster.Position, de.ims.icarus2.model.api.raster.Position)}.
			 */
			@RepeatedTest(value=RUNS)
			void testSetSpan() {
				instance.setSpan(begin1, end1);
				assertSame(begin1, instance.getFragmentBegin());
				assertSame(end1, instance.getFragmentEnd());
			}

		}
	}

}
