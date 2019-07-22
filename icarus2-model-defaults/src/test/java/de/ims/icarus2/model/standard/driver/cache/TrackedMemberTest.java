/**
 *
 */
package de.ims.icarus2.model.standard.driver.cache;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedContainer;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedFragment;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedItem;
import de.ims.icarus2.model.standard.driver.cache.TrackedMember.TrackedStructure;

/**
 * @author Markus Gärtner
 *
 */
class TrackedMemberTest {

	static abstract class TestBase<T extends TrackedMember> {
		abstract T create();

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()}.
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()}.
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()}.
		 */
		@Test
		void testUseCounter() {
			T instance = create();

			assertEquals(0, instance.getUseCounter());

			assertEquals(1, instance.incrementUseCounter());
			assertEquals(1, instance.getUseCounter());

			assertEquals(0, instance.decrementUseCounter());
			assertEquals(0, instance.getUseCounter());
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()}.
		 */
		@Test
		void testDecrementUseCounterWhenUnused() {
			assertModelException(GlobalErrorCode.ILLEGAL_STATE,
					() -> create().decrementUseCounter());
		}
	}

	@Nested
	class ForTrackedItem extends TestBase<TrackedItem> {

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMemberTest.TestBase#create()
		 */
		@Override
		TrackedItem create() {
			return new TrackedItem();
		}

	}

	@Nested
	class ForTrackedStructure extends TestBase<TrackedStructure> {

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMemberTest.TestBase#create()
		 */
		@Override
		TrackedStructure create() {
			return new TrackedStructure();
		}

	}

	@Nested
	class ForTrackedFragment extends TestBase<TrackedFragment> {

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMemberTest.TestBase#create()
		 */
		@Override
		TrackedFragment create() {
			return new TrackedFragment();
		}

	}

	@Nested
	class ForTrackedContainer extends TestBase<TrackedContainer> {

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMemberTest.TestBase#create()
		 */
		@Override
		TrackedContainer create() {
			return new TrackedContainer();
		}

	}
}
