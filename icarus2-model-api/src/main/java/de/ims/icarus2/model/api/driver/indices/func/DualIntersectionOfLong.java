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

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DualIntersectionOfLong implements PrimitiveIterator.OfLong {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;
	private long value;

	public DualIntersectionOfLong(PrimitiveIterator.OfLong left,
			PrimitiveIterator.OfLong right) {
		requireNonNull(left);
		requireNonNull(right);

		this.left = left;
		this.right = right;

		leftVal = nextLeft();
		rightVal = nextRight();

		value = IcarusUtils.UNSET_LONG;
	}

	private long nextLeft() {
		return left.hasNext() ? left.nextLong() : IcarusUtils.UNSET_LONG;
	}

	private long nextRight() {
		return right.hasNext() ? right.nextLong() : IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {

		// Check if either begining on the stream or if nextLong() has reset the value
		if(value==IcarusUtils.UNSET_LONG) {
			while(leftVal!=IcarusUtils.UNSET_LONG && rightVal!=IcarusUtils.UNSET_LONG) {
				if(leftVal<rightVal) {
					leftVal = nextLeft();
				} else if(leftVal>rightVal) {
					rightVal = nextRight();
				} else {
					value = leftVal;
					leftVal = nextLeft();
					rightVal = nextRight();
					break;
				}
			}
		}

		return value!=IcarusUtils.UNSET_LONG;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		// Not calling hasNext() since that is the client code's job
		if(value==IcarusUtils.UNSET_LONG)
			throw new NoSuchElementException();

		long result = value;
		// Indicator for hasNext() to look for refresh next value
		value = IcarusUtils.UNSET_LONG;
		return result;
	}

}