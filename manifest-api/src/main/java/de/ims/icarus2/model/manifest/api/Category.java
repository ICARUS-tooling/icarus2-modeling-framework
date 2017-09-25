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
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.util.id.Identity;

/**
 * Link to a category or term definition.
 * The concept extends the basic idea of {@link Identity}
 * and adds a {@link #getNamespace() namespace} information
 * that is used to specify the source of the category
 * definition.
 *
 * @author Markus Gärtner
 *
 */
public interface Category extends Identity {

	/**
	 * Returns the source or context in which this category
	 * has been defined in.
	 *
	 * @return
	 */
	String getNamespace();

	/**
	 * Returns the unique identifier of this category within
	 * its declared {@link #getNamespace() namespace}. Note that
	 * unlike the original contract of {@link Identity} the
	 * value returned by this method must follow the defined
	 * vocabulary of the designated {@link #getNamespace() namespace}!
	 *
	 * @see de.ims.icarus2.util.id.Identity#getId()
	 */
	@Override
	String getId();
}
