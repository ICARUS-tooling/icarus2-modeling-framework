/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/DualIntersectionOfLong.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import de.ims.icarus2.model.api.ModelConstants;

/**
 * @author Markus Gärtner
 * @version $Id: DualIntersectionOfLong.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class DualIntersectionOfLong implements PrimitiveIterator.OfLong, ModelConstants {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;
	private long value;

	public DualIntersectionOfLong(PrimitiveIterator.OfLong left,
			PrimitiveIterator.OfLong right) {
		checkNotNull(left);
		checkNotNull(right);

		this.left = left;
		this.right = right;

		leftVal = nextLeft();
		rightVal = nextRight();

		value = NO_INDEX;
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

		// Check if either begining on the stream or if nextLong() has reset the value
		if(value==NO_INDEX) {
			while(leftVal!=NO_INDEX && rightVal!=NO_INDEX) {
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

		return value!=NO_INDEX;
	}

	/**
	 * @see java.util.PrimitiveIterator.OfLong#nextLong()
	 */
	@Override
	public long nextLong() {
		// Not calling hasNext() since that is the client code's job
		if(value==NO_INDEX)
			throw new NoSuchElementException();

		long result = value;
		// Indicator for hasNext() to look for refresh next value
		value = NO_INDEX;
		return result;
	}

}
