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
package de.ims.icarus2.filedriver;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ElementFlag {

	/**
	 * Element has neither been scanned nor otherwise checked
	 */
	UNKNOWN,

	/**
	 * The element or its data source doesn't exist on the physical storage
	 */
	MISSING,

	/**
	 * Element experienced changes made to it outside of the framework or has
	 * incorrect metadata stored for it.
	 */
	CORRUPTED,

	/**
	 * External problems outside the framework control prevented preparation
	 * of the element and rendered it unusable for further processing. Examples
	 * for this state include failed file creation or other I/O related errors.
	 */
	UNUSABLE,

	/**
	 * Element was checked but has not yet been scanned
	 */
	CHECKED,

	/**
	 * Part of the metadata for the element has been collected.
	 * This state is usable mainly for cases of layer data being
	 * distributed across several files.
	 */
	PARTIALLY_SCANNED,

	/**
	 * All metadata of the element has been collected. Element is fully prepared and usable.
	 */
	SCANNED,

	/**
	 * Parts of the element have been loaded into the data model
	 */
	PARTIALLY_LOADED,

	/**
	 * Entire element has been loaded into the data model.
	 */
	LOADED
	;
}
