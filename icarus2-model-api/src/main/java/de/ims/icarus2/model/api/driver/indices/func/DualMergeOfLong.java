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
package de.ims.icarus2.model.api.driver.indices.func;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Merges two sorted {@link OfLong long iterators} into a single sorted
 * one. Note that the behavior of this iterator is undefined if the
 * supplied surce iterators are not sorted!
 *
 * @author Markus Gärtner
 *
 */
public class DualMergeOfLong implements PrimitiveIterator.OfLong {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;
	private boolean hasLeft, hasRight;

	public DualMergeOfLong(PrimitiveIterator.OfLong left,
			PrimitiveIterator.OfLong right) {
		requireNonNull(left);
		requireNonNull(right);

		this.left = left;
		this.right = right;

		nextLeft();
		nextRight();
	}

	private void nextLeft() {
		hasLeft = left.hasNext();
		if(hasLeft) {
			leftVal = left.nextLong();
		}
	}

	private void nextRight() {
		hasRight = right.hasNext();
		if(hasRight) {
			rightVal = right.nextLong();
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return hasLeft || hasRight;
	}

	/**
	 * Returns the smallest of the two input iterator's heads.
	 *
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 *
	 * @throws NoSuchElementException if this iterator reached its
	 * end and neither input iterator has any elements left.
	 */
	@Override
	public long nextLong() {
		long result;

		if(hasLeft && (!hasRight || leftVal<=rightVal)) {
			result = leftVal;
			nextLeft();
		} else if(hasRight && (!hasLeft || rightVal<=leftVal)) {
			result = rightVal;
			nextRight();
		} else
			throw new NoSuchElementException();

		return result;
	}

}
