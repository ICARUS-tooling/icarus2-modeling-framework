/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2;

import de.ims.icarus2.util.id.DuplicateIdentifierException;

/**
 * <b>Implementation note:</b>
 * <p>
 * Any class implementing this interface must make sure that all instances created are
 * properly {@link #register(ErrorCode...) registered} without exception! This can be
 * done as part of the respective constructor method or in bulk via a static code fragment.
 *
 * @author Markus Gärtner
 *
 */
public interface ErrorCode {

	//TODO rework the exception framework so that ErrorCode instances are capable of signaling whether or not they can be used in a RuntimeException or require proper Exception context, such as I/O stuff

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
	ErrorCodeScope scope();

	String name();

	@SafeVarargs
	public static <E extends ErrorCode> void register(E... codes) {
		for(ErrorCode code : codes) {
			if(ErrorCodeLookup.map.containsKey(code.code()))
				throw new DuplicateIdentifierException("Duplicate error code "+code.code()+" - attempted to register: "+code);

			ErrorCodeLookup.map.put(code.code(), code);
		}
	}

	public static ErrorCode forCode(int code) {
		return ErrorCodeLookup.map.get(code);
	}

	public static ErrorCode forException(Exception e) {
		return forException(e, null);
	}

	public static ErrorCode forException(Exception e, ErrorCode defaultCode) {
		if(e instanceof IcarusRuntimeException) {
			return ((IcarusRuntimeException)e).getErrorCode();
		}

		return defaultCode;
	}
}
