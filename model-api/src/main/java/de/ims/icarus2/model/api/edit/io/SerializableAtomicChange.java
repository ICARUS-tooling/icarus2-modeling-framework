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
package de.ims.icarus2.model.api.edit.io;

import de.ims.icarus2.model.api.edit.AtomicChange;

/**
 * @author Markus Gärtner
 *
 */
public interface SerializableAtomicChange extends AtomicChange {


	/**
	 * Optional method for creating a unified serializable representation of this
	 * change. The change implementation is to create a blank new proxy and fill its
	 * fields with the appropriate contextual information for the type of change it
	 * models.
	 *
	 * @return a filled out {@link AtomicChangeProxy} instance containing all the information
	 * required to reproduce this change or {@code null} if creating such a proxy is not supported.
	 */
	AtomicChangeProxy toProxy();
}
