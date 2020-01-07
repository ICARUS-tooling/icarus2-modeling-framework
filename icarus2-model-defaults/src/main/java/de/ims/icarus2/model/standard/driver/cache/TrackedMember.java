/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Fragment;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.members.structure.Structure;
import de.ims.icarus2.model.standard.members.container.DefaultContainer;
import de.ims.icarus2.model.standard.members.item.DefaultFragment;
import de.ims.icarus2.model.standard.members.item.DefaultItem;
import de.ims.icarus2.model.standard.members.structure.DefaultStructure;

/**
 * Provides simple extensions of the default implementations for {@link Item items},
 * {@link Fragment fragments}, {@link Container containers} and {@link Structure structures}.
 * <p>
 * The base for those extensions is an interface that models a <i>usage counter</> resembling
 * a count of the current working references to the member in question.
 *
 * @author Markus Gärtner
 *
 */
public interface TrackedMember {

	/**
	 * Increments and returns the current use counter of the member
	 *
	 * @return the use counter of this member after it has been incremented
	 */
	int incrementUseCounter();

	/**
	 * Decrements and returns the current use counter of the member
	 *
	 * @return the use counter of this member after it has been decremented
	 */
	int decrementUseCounter();

	/**
	 * Returns the current use counter of the member
	 *
	 * @return the use counter of this member
	 */
	int getUseCounter();

	public static class TrackedItem extends DefaultItem implements TrackedMember {

		private int useCounter;

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedFragment extends DefaultFragment implements TrackedMember {

		private int useCounter;

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedContainer extends DefaultContainer implements TrackedMember {

		private int useCounter;

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}

	public static class TrackedStructure extends DefaultStructure implements TrackedMember {

		private int useCounter;

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#incrementUseCounter()
		 */
		@Override
		public int incrementUseCounter() {
			return ++useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#decrementUseCounter()
		 */
		@Override
		public int decrementUseCounter() {
			if(useCounter==0)
				throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Use counter already 0");
			return --useCounter;
		}

		/**
		 * @see de.ims.icarus2.model.standard.driver.cache.TrackedMember#getUseCounter()
		 */
		@Override
		public int getUseCounter() {
			return useCounter;
		}

	}
}
