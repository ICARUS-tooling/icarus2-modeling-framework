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
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public interface ResolverContext {

	/**
	 * Returns the currently active host container
	 *
	 * @return
	 */
	Container currentContainer();

	/**
	 * Returns the index within the current container that reading is taking place at
	 *
	 * @return
	 */
	long currentIndex();

	/**
	 * Returns the item with which currently read data should be associated
	 *
	 * @return
	 */
	Item currentItem();

	/**
	 * Returns the current raw input data that should be read and converted
	 *
	 * @return
	 */
	CharSequence rawData();

	/**
	 * Tells the context that the current raw input line should be treated as being consumed,
	 * meaning that the code controlling the read operation must not pass it on to other
	 * subsequent tasks.
	 */
	default void consumeData() {
		// no-op
	}
}
