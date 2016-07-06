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
package de.ims.icarus2.util;

/**
 * @author Markus Gärtner
 *
 */
public interface Recyclable {

	/**
	 * Erases all internal states that might be affected by foreign objects and
	 * prepares the item for being added to an object pool.
	 */
	void recycle();

	/**
	 * Verifies the internal state of the item after it has been revived from an
	 * object pool. Returns {@code true} if and only if all internal properties are
	 * properly initialized and the item is in a valid state.
	 * <p>
	 * All internal refreshing logic should go into this method to ensure a pooled
	 * object gets revived properly. Note that this method should be called <b>after</b>
	 * a pooled object has been filled with new data!
	 * <p>
	 * It is perfectly legal for an object to remain in an inconsistent state once this
	 * method has detected a corrupted property and returned {@code false}. Once the
	 * consistency check failed, surrounding client code should treat the object as
	 * garbage and either discard it or recycle it again. Under no circumstances should
	 * such an object be used in the regular lifecycle of a client!
	 *
	 * @return
	 */
	boolean revive();
}
