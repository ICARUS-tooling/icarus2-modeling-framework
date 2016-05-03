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

 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/DualMergeOfLong.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import de.ims.icarus2.model.api.ModelConstants;

/**
 * @author Markus Gärtner
 * @version $Id: DualMergeOfLong.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class DualMergeOfLong implements PrimitiveIterator.OfLong, ModelConstants {

	private final PrimitiveIterator.OfLong left, right;

	private long leftVal, rightVal;

	public DualMergeOfLong(PrimitiveIterator.OfLong left,
			PrimitiveIterator.OfLong right) {
		checkNotNull(left);
		checkNotNull(right);

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
