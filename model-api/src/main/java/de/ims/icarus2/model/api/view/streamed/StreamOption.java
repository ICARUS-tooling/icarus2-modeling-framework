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
 */
package de.ims.icarus2.model.api.view.streamed;

/**
 * @author Markus Gärtner
 *
 */
public enum StreamOption {



	/**
	 * If set, then the stream implementation is capable of skipping
	 * portions of the underlying data.
	 * <p>
	 * This usually comes with a certain trade-off on terms of buffer
	 * efficiency, and client code should only decide to use this
	 * option if frequent skips over large numbers of items are to be
	 * expected.
	 */
	SKIP_SUPPORTED,

	/**
	 * If set, client code can put a mark on an item in the stream
	 * and then successively go back to that item later.
	 * <p>
	 * Note that implementations might impose a limit on the number
	 * of items between the mark and the current position in the
	 * stream up to which the mark can be kept alive.
	 */
	MARK_SUPPORTED,
	;
}
