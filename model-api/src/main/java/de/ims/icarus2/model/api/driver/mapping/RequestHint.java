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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.util.Flag;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum RequestHint implements Flag {

	/**
	 * Specifies that constructed {@link IndexSet} instances returned by any
	 * of the mapping methods of a {@link MappingReader} must not retain links
	 * to back-end data storage facilities such as databases. This flag effectively
	 * forbids lazy loading of mapping data from such storages.
	 * <p>
	 * Reader implementations that can provide lazily loaded mapping data are
	 * required to obey this flag and decouple results from their back-end storage
	 * if necessary.
	 */
	CONNECTIVITY_OFFLINE,

	//TODO
	INPUT_ORDER_SORTED,
	;
}
