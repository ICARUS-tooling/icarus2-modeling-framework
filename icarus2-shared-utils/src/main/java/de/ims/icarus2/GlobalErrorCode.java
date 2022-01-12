/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
	 * Hint that a certain implementation is missing mandatory/requested functionality.
	 */
	NOT_IMPLEMENTED(3),

	/**
	 * A method or certain usage of a method is deprecated.
	 * This is a significantly stronger case of deprecation compared to the
	 * {@code Deprecated} annotation.
	 */
	DEPRECATED(4),


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
	 * <p>
	 * Also used to wrap {@link InterruptedException} when the enclosing method
	 * does not declare {@link InterruptedException} to be thrown.
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
	 * A given value exceeds the value space of a more limited type or a
	 * buffer structure reached the upper limit of its supported size.
	 */
	VALUE_OVERFLOW(120),

	/**
	 * Delegating an operation to another class failed and the exception
	 * indicating this failure will be wrapped in this error.
	 */
	DELEGATION_FAILED(130),

	/**
	 * A given enum constant is unknown to the code using it in a switch
	 * statement or similar construct. This error type is exclusively
	 * intended to signal that an enum has received a new constant and the
	 * code in question hasn't been updated to properly handle the change.
	 * <p>
	 * Apart from pure enums it can also be used for any quasi static
	 * collection of constants.
	 */
	UNKNOWN_ENUM(131),

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

	//TODO do we need this category at all?
	;

	private static volatile ErrorCodeScope SCOPE;

	public static ErrorCodeScope getScope() {
		ErrorCodeScope scope = SCOPE;
		if(scope==null) {
			synchronized (GlobalErrorCode.class) {
				if((scope = SCOPE) == null) {
					scope = SCOPE = ErrorCodeScope.newScope(1000, GlobalErrorCode.class.getSimpleName());
				}
			}
		}
		return scope;
	}

	private final int code;

	private GlobalErrorCode(int code) {
		this.code = code;

		ErrorCode.register(this);
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#code()
	 */
	@Override
	public int code() {
		return code+getScope().getCode();
	}

	/**
	 * @see de.ims.icarus2.ErrorCode#scope()
	 */
	@Override
	public ErrorCodeScope scope() {
		return getScope();
	}

	/**
	 * Resolves the given error code to the matching enum constant.
	 * {@code Code} can be given both as an internal id or global code.
	 *
	 * @param code
	 * @return
	 */
	public static GlobalErrorCode forCode(int code) {
		getScope().checkCode(code);

		ErrorCode error = ErrorCode.forCode(code);

		if(error==null)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Unknown error code: "+code);
		if(!GlobalErrorCode.class.isInstance(error))
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Corrupted mapping for error code: "+code);

		return GlobalErrorCode.class.cast(error);
	}
}
