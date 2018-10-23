/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import java.util.function.Supplier;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestException extends IcarusException {

	public static Supplier<ManifestException> create(ErrorCode errorCode, String message) {
		return () -> new ManifestException(errorCode, message);
	}

	private static final long serialVersionUID = 7579478541873972798L;

	//FIXME add ManifestFragment field

	/**
	 * @param errorCode
	 * @param message
	 * @param cause
	 */
	public ManifestException(ErrorCode errorCode, String message,
			Throwable cause) {
		super(errorCode, message, cause);
	}

	/**
	 * @param errorCode
	 * @param message
	 */
	public ManifestException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public ManifestException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
