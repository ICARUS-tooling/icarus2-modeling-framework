/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
