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
/**
 *
 */
package de.ims.icarus2.query.api;

import java.util.Optional;

import javax.annotation.Nullable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * @author Markus Gärtner
 *
 */
public class QueryException extends IcarusRuntimeException {

	private static final long serialVersionUID = 466513317359445843L;

	/** Part of the original query that led to this error */
	private final Optional<QueryFragment> fragment;

	public QueryException(ErrorCode errorCode, String message, @Nullable QueryFragment fragment,
			@Nullable Throwable cause) {
		super(errorCode, message, cause);
		this.fragment = Optional.ofNullable(fragment);
	}

	public QueryException(ErrorCode errorCode, String message, @Nullable QueryFragment fragment) {
		super(errorCode, message);
		this.fragment = Optional.ofNullable(fragment);
	}

	public QueryException(ErrorCode errorCode, @Nullable QueryFragment fragment,
			@Nullable Throwable cause) {
		super(errorCode, cause);
		this.fragment = Optional.ofNullable(fragment);
	}

	public QueryException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
		super(errorCode, message, cause);
		this.fragment = Optional.empty();

	}

	public QueryException(ErrorCode errorCode, String message) {
		super(errorCode, message);
		this.fragment = Optional.empty();
	}

	public QueryException(ErrorCode errorCode, @Nullable Throwable cause) {
		super(errorCode, cause);
		this.fragment = Optional.empty();
	}

	/**
	 * @return the fragment
	 */
	public Optional<QueryFragment> getFragment() {
		return fragment;
	}
}
