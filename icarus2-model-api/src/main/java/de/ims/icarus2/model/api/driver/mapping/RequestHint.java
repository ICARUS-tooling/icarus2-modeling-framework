/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
