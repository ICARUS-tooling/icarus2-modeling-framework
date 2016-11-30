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

/**
 * @author Markus Gärtner
 *
 */
public enum GlobalErrorCode implements ErrorCode {

	/**
	 * Not strictly speaking an actual error code, but used to signal that
	 * some exception or other means of notification was meant purely for
	 * informative purposes.
	 */
	INFO(1),

	/**
	 * Hint that a certain implementation is missing mandatory functionality.
	 */
	NOT_IMPLEMENTED(3),

	//**************************************************
	//       1xx  GENERAL ERRORS
	//**************************************************

	/**
	 * Represents an error whose cause could not be identified or when a
	 * {@code ModelException} only contains an error message without the
	 * exact type of error being specified.
	 */
	UNKNOWN_ERROR(100),

	/**
	 * Wraps an {@link OutOfMemoryError} object that was thrown when
	 * the Java VM ran out of memory to allocate objects.
	 */
	INSUFFICIENT_MEMORY(101),

	/**
	 * A method that performs a blocking operation but which did not signal
	 * so by declaring {@link InterruptedException} to be thrown encountered
	 * an active {@link Thread#interrupted() interruption} flag.
	 */
	INTERRUPTED(102),

	/**
	 * A general I/O error occurred.
	 */
	IO_ERROR(110),

	/**
	 * Reading from a resource is not possible.
	 */
	NO_READ_ACCESS(111),

	/**
	 * Writing to a resource is not possible.
	 */
	NO_WRITE_ACCESS(112),

	/**
	 * An operation could not be completed due to lack of local data
	 * (e.g. client code missed to set vital fields before usage).
	 */
	MISSING_DATA(113),

	/**
	 * Wraps the semantics of {@link IllegalStateException}
	 */
	ILLEGAL_STATE(114),

	/**
	 * Wraps the semantics of {@link IllegalArgumentException}
	 */
	INVALID_INPUT(115),

	/**
	 * Wraps the semantics of {@link UnsupportedOperationException}
	 */
	UNSUPPORTED_OPERATION(116),

	/**
	 * An operation was cancelled by the user.
	 */
	CANCELLED(117),

	/**
	 * A given long index exceeds the {@value Integer#MAX_VALUE} limit or
	 * a buffer structure couldn't be enlarged due to the size overflowing
	 * integer value space.
	 */
	INDEX_OVERFLOW(120),

	/**
	 * Delegating an operation to another class failed and the exception
	 * indicating this failure will be wrapped in this error.
	 */
	DELEGATION_FAILED(130),

	/**
	 * A feature only provided by the JDK is required.
	 */
	VM_JDK_REQUIRED(140),

	/**
	 * Signals a bug in the internal implementations of the framework.
	 */
	INTERNAL_ERROR(150),

	/**
	 * Trying to instantiate a member of a foreign class failed.
	 * <p>
	 * This error wraps {@link ClassNotFoundException}, {@link InstantiationException}
	 * and {@link IllegalAccessException}.
	 */
	INSTANTIATION_ERROR(160),

	//**************************************************
	//       7xx  DATA ERRORS
	//**************************************************

	/**
	 * Mismatch between expected size of an array object and its actual length.
	 *
	 * FIXME should be removed, only used for 2 error cases in ValueType
	 */
	DATA_ARRAY_SIZE(701),

	;

	private final int code;

	private GlobalErrorCode(int code) {
		this.code = code;
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#code()
	 */
	@Override
	public int code() {
		return code;
	}
}
