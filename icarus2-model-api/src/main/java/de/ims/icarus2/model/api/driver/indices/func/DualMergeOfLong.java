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
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;
import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * @author Markus Gärtner
 *
 */
public class DualMergeOfLong implements PrimitiveIterator.OfLong {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;

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
		leftVal = left.hasNext() ? left.nextLong() : UNSET_LONG;
	}

	private void nextRight() {
		rightVal = right.hasNext() ? right.nextLong() : UNSET_LONG;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return leftVal!=UNSET_LONG || rightVal!=UNSET_LONG;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		long result;

		if(leftVal!=UNSET_LONG && leftVal<rightVal) {
			result = leftVal;
			nextLeft();
		} else if(rightVal!=UNSET_LONG) {
			result = rightVal;
			nextRight();
		} else
			throw new NoSuchElementException();

		return result;
	}

}
