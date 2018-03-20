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
package de.ims.icarus2.model.api.driver.mapping;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.util.Flag;

/**
 * @author Markus Gärtner
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

	/**
	 * Specifies that any indices provided as input for the request (in the form
	 * of {@link IndexSet} instances) are sorted. This effectively overrides the
	 * info given by individual index sets via their {@link IndexSet#isSorted()}
	 * methods.
	 * <p>
	 * This hint exists to work around the fact that the info from an index set is
	 * usually done on a best-effort basis.
	 */
	INPUT_ORDER_SORTED,

	/**
	 * Signals that a mapping should not throw an exception when encountering
	 * indices that it has no entries for.
	 */
	IGNORE_UNKNOWN_INDICES,
	;
}
