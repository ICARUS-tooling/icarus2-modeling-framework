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
package de.ims.icarus2.util;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IcarusUtils {

	/**
	 * Maximum value for use in arrays.
	 */
	public static final long MAX_INTEGER_INDEX = Integer.MAX_VALUE-8;

	public static String toLoggableString(Object value) {
		//TODO ensure the generated string is short enough and does not contain line breaks
		return String.valueOf(value);
	}

	public static int ensureIntegerValueRange(long value) {
		if(value>MAX_INTEGER_INDEX)
			throw new IcarusException(GlobalErrorCode.INDEX_OVERFLOW, "Not a legal value in integer range: "+value);

		return (int) value;
	}

	public static int limitToIntegerValueRange(long value) {
		return (int) Math.min(MAX_INTEGER_INDEX, value);
	}

}
