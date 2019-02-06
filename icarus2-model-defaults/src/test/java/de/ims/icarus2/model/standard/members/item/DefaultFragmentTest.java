/**
 *
 */
package de.ims.icarus2.model.standard.members.item;

import static de.ims.icarus2.model.api.ModelTestUtils.mockItem;
import static de.ims.icarus2.test.TestTags.RANDOMIZED;
import static de.ims.icarus2.test.TestUtils.NO_CHECK;
import static de.ims.icarus2.test.TestUtils.NO_DEFAULT;
import static de.ims.icarus2.test.TestUtils.NPE_CHECK;
import static de.ims.icarus2.test.TestUtils.assertGetter;
import static de.ims.icarus2.test.TestUtils.assertSetter;
import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.model.api.layer.FragmentLayer;
import de.ims.icarus2.model.api.members.MemberTest;
import de.ims.icarus2.model.api.members.MemberType;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
class DefaultFragmentTest implements MemberTest<Fragment> {

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
			new DefaultFragment();
		}
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
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentBegin(de.ims.icarus2.model.api.raster.Position)}.
		 */
		@Test
		void testSetFragmentBegin() {
			instance.setFragmentBegin(null);
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)}.
		 */
		@Test
		void testSetFragmentEnd() {
			instance.setFragmentEnd(null);
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

		@Nested
		class WithItem {
			Item item;

			@BeforeEach
			void setUp() {
				item = mockItem();
				instance.setItem(item);
			}

			@AfterEach
			void tearDown() {
				item = null;
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getBeginOffset()}.
			 */
			@SuppressWarnings("boxing")
			@ParameterizedTest
			@ValueSource(longs = {UNSET_LONG, 0, 1, Long.MAX_VALUE})
			@MethodSource("de.ims.icarus2.model.api.ModelTestUtils#randomIndices")
			@Tag(RANDOMIZED)
			void testGetBeginOffset(long value) {
				when(item.getBeginOffset()).thenReturn(value);
				assertEquals(value, instance.getBeginOffset());
				verify(item).getBeginOffset();
			}

			/**
			 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getEndOffset()}.
			 */
			@SuppressWarnings("boxing")
			@ParameterizedTest
			@ValueSource(longs = {UNSET_LONG, 0, 1, Long.MAX_VALUE})
			@MethodSource("de.ims.icarus2.model.api.ModelTestUtils#randomIndices")
			@Tag(RANDOMIZED)
			void testGetEndOffset(long value) {
				when(item.getEndOffset()).thenReturn(value);
				assertEquals(value, instance.getEndOffset());
				verify(item).getEndOffset();
			}
		}

		@Nested
		class WithLayer {
			FragmentLayer layer;

			//TODO
		}
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#recycle()}.
	 */
	@Test
	void testRecycle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#revive()}.
	 */
	@Test
	void testRevive() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getLayer()}.
	 */
	@Test
	void testGetLayer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentBegin()}.
	 */
	@Test
	void testGetFragmentBegin() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#getFragmentEnd()}.
	 */
	@Test
	void testGetFragmentEnd() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentBegin(de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	void testSetFragmentBegin() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setFragmentEnd(de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	void testSetFragmentEnd() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.ims.icarus2.model.standard.members.item.DefaultFragment#setSpan(de.ims.icarus2.model.api.raster.Position, de.ims.icarus2.model.api.raster.Position)}.
	 */
	@Test
	void testSetSpan() {
		fail("Not yet implemented"); // TODO
	}

}
