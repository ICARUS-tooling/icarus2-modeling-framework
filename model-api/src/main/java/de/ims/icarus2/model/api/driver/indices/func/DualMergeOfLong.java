/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import de.ims.icarus2.model.api.ModelConstants;

/**
 * @author Markus Gärtner
 *
 */
public class DualMergeOfLong implements PrimitiveIterator.OfLong, ModelConstants {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;

	public DualMergeOfLong(PrimitiveIterator.OfLong left,
			PrimitiveIterator.OfLong right) {
		requireNonNull(left);
		requireNonNull(right);

		this.left = left;
		this.right = right;

		leftVal = nextLeft();
		rightVal = nextRight();
	}

	private long nextLeft() {
		return left.hasNext() ? left.nextLong() : NO_INDEX;
	}

	private long nextRight() {
		return right.hasNext() ? right.nextLong() : NO_INDEX;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return leftVal!=NO_INDEX || rightVal!=NO_INDEX;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		long result;

		if(leftVal!=NO_INDEX && leftVal<rightVal) {
			result = leftVal;
			leftVal = nextLeft();
		} else if(rightVal!=NO_INDEX) {
			result = rightVal;
			rightVal = nextRight();
		} else
			throw new NoSuchElementException();

		return result;
	}

}
