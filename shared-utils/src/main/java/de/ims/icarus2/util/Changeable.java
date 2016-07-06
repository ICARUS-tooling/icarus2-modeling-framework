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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <i>Publisher</i counter-part of the {@link ChangeListener} interface.
 * A class implementing {@code Changeable} models an entity with at least one
 * aspect that has observable state changes.
 * <p>
 * Note that the <i>subscriber<i> part of the contract does not specify any
 * way to communicate the content or nature of the change that occurred but only
 * signals the event of the change itself.
 *
 * @author Markus Gärtner
 *
 */
public interface Changeable {

	/**
	 * Adds a new listener to receive {@link ChangeEvent events} from this publisher.
	 * Note that implementations should make sure that internal listener lists don't
	 * contain duplicates.
	 *
	 * @param listener
	 */
	void addChangeListener(ChangeListener listener);

	void removeChangeListener(ChangeListener listener);
}
