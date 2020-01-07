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
 * @author Markus Gärtner
 *
 */
public class DualIntersectionOfLong implements PrimitiveIterator.OfLong {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;
	private boolean hasLeft, hasRight, hasValue;
	private long value;

	public DualIntersectionOfLong(PrimitiveIterator.OfLong left,
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

		// Check if either still at stream begin or if nextLong() has reset the value
		if(!hasValue) {
			while(hasLeft && hasRight) {
				if(leftVal<rightVal) {
					nextLeft();
				} else if(leftVal>rightVal) {
					nextRight();
				} else {
					value = leftVal;
					nextLeft();
					nextRight();
					hasValue = true;
					break;
				}
			}
		}

		return hasValue;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		// Not calling hasNext() since that is the client code's job
		if(!hasValue)
			throw new NoSuchElementException();

		// Indicator for hasNext() to look for refresh next value
		hasValue = false;
		return value;
	}

}
