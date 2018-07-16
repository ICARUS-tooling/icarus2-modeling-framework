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
package de.ims.icarus2.model.api.edit.change;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;

/**
 *
 * @author Markus Gärtner
 *
 * @param <E> type of element that was added or removed
 * @param <C> type of the container or structure the element was added to or removed from
 */
public interface AtomicAddChange<E extends Item, C extends Container> extends AtomicChange {

	/**
	 * Returns whether or not this change <b>originally</b> was an add.
	 * @return
	 */
	boolean isAdd();

	E getElement();

	C getContainer();

	long getIndex();
}
