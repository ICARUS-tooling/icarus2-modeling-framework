/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public final class ErrorCodeScope {

	private static final Int2ObjectMap<ErrorCodeScope> _scopes = new Int2ObjectOpenHashMap<>();

	private static final int STEP = 1000;

	public static ErrorCodeScope newScope(int code, String label) {
		checkCodeParameter(code);
		requireNonNull(label);
		checkArgument("Label must not be empty", !label.trim().isEmpty());

		synchronized (_scopes) {
			ErrorCodeScope scope = _scopes.get(code);
			if(scope!=null)
				throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Code already in use: "+code);

			scope = new ErrorCodeScope(code, label);

			_scopes.put(code, scope);

			return scope;
		}
	}

	public static ErrorCodeScope forCode(int code) {
		checkCodeParameter(code);

		synchronized (_scopes) {
			return _scopes.get(code);
		}
	}

	private static void checkCodeParameter(int code) {
		checkArgument("Code must not be zero or negative", code>0);
		checkArgument("Code must be a multiple of "+STEP, code%STEP==0);
	}

	private final int code;
	private final String label;

	private ErrorCodeScope(int code, String label) {
		this.code = code;
		this.label = label;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	public boolean isValidCode(int code) {
		code -= this.code;
		return code>=0 && code<STEP;
	}

	public void checkCode(int code) {
		if(!isValidCode(code))
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Not a valid "+label+" code: "+code);
	}

	public int getRawCode(int code) {
		checkCode(code);

		return code-this.code;
	}
}
