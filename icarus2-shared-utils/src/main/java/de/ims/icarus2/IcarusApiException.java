/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * @author Markus Gärtner
 *
 */
public class IcarusApiException extends Exception {

	private static final long serialVersionUID = -8840920069446164364L;

	private final ErrorCode errorCode;

	/**
	 * @param message
	 * @param cause
	 */
	public IcarusApiException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * @param message
	 */
	public IcarusApiException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = requireNonNull(errorCode);
	}

	/**
	 * @param cause
	 */
	public IcarusApiException(ErrorCode errorCode, Throwable cause) {
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
		if(obj==this) {
			return true;
		} else if(obj instanceof IcarusApiException) {
			IcarusApiException other = (IcarusApiException) obj;
			return errorCode==other.errorCode
					&& Objects.equals(getMessage(), other.getMessage());
			//TODO we do ignore stack trace here
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
