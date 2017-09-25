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
package de.ims.icarus2;

import de.ims.icarus2.util.id.DuplicateIdentifierException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public interface ErrorCode {

	/**
	 * Returns the global error code for this error.
	 * This is the combination of the {@link #scope() scope}
	 * and the internal error id:<br>
	 * <tt><i>code = scope + internal_id</i></tt>
	 * <br>
	 * E.g. {@code 1100} for an {@link GlobalErrorCode#UNKNOWN_ERROR unknown error}
	 * from the {@link GlobalErrorCode global error domain}.
	 *
	 * @return the actual numerical code of this error
	 */
	int code();

	/**
	 * Returns the basic value range or <i>scope</i> of this error code.
	 * This scope is added to the internal id of an error to form the
	 * public {@link #code() error code}:<br>
	 * <tt><i>code = scope + internal_id</i></tt>
	 * <br>
	 * E.g. {@code 1000} for any error in the {@link GlobalErrorCode global error domain}.
	 *
	 * @return
	 *
	 * @see #code()
	 */
	int scope();

	String name();

	static final Int2ObjectMap<ErrorCode> _lookup = new Int2ObjectOpenHashMap<>();

	@SafeVarargs
	public static <E extends ErrorCode> void register(E... codes) {
		for(ErrorCode code : codes) {
			if(_lookup.containsKey(code.code()))
				throw new DuplicateIdentifierException("Duplicate error code "+code.code()+" - attempted to register: "+code);

			_lookup.put(code.code(), code);
		}
	}

	public static ErrorCode forException(Exception e) {
		return forException(e, null);
	}

	public static ErrorCode forException(Exception e, ErrorCode defaultCode) {
		if(e instanceof IcarusException) {
			return ((IcarusException)e).getErrorCode();
		}

		return defaultCode;
	}
}
