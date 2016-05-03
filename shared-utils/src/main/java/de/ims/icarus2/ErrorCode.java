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
package de.ims.icarus2;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.ims.icarus2.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ErrorCode {

	int code();

	String name();

	static final TIntObjectMap<ErrorCode> _lookup = new TIntObjectHashMap<>();

	@SafeVarargs
	public static <E extends ErrorCode> void register(E... codes) {
		for(ErrorCode code : codes) {
			if(_lookup.containsKey(code.code()))
				throw new DuplicateIdentifierException("Duplicate error code "+code.code()+" - attempted to register: "+code);

			_lookup.put(code.code(), code);
		}
	}
}
