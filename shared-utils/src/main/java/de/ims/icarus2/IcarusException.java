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

import static de.ims.icarus2.util.classes.ClassUtils._int;
import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class IcarusException extends RuntimeException {
	private static final long serialVersionUID = -4948728458934647064L;

	private final ErrorCode errorCode;

	/**
	 * @param message
	 * @param cause
	 */
	public IcarusException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * @param message
	 */
	public IcarusException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * @param cause
	 */
	public IcarusException(ErrorCode errorCode, Throwable cause) {
		super(cause);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return String.format("[%d: %s] %s", _int(errorCode.code()), errorCode.name(), super.toString());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) {
			return false;
		}
		if(obj instanceof IcarusException) {
			return errorCode==((IcarusException)obj).errorCode;
		}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * (errorCode.hashCode()+1);
	}

	/**
	 * @return the errorCode
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
