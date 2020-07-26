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
package de.ims.icarus2.model.manifest.api;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.util.ManifestUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestException extends IcarusRuntimeException {

	public static Supplier<ManifestException> error(ErrorCode errorCode, String message) {
		return () -> new ManifestException(errorCode, message);
	}

	/**
	 * Calls {@link #error(ErrorCode, String)} with {@link ManifestErrorCode#MANIFEST_ERROR}
	 * @param message
	 * @return
	 */
	public static Supplier<ManifestException> error(String message) {
		return error(ManifestErrorCode.MANIFEST_ERROR, message);
	}

	public static Supplier<ManifestException> missing(ManifestFragment fragment, String property) {
		return () -> new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
				"Missing property '"+property+"' in manifest "+ManifestUtils.getName(fragment));
	}

	public static Supplier<ManifestException> noHost(ManifestFragment fragment) {
		return () -> new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
				"Manifest is required to have a host environment: "+ManifestUtils.getName(fragment));
	}

	public static Supplier<ManifestException> noElement(ManifestFragment fragment, String name) {
		return () -> new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
				"Missing child element '"+name+"' in manifest "+ManifestUtils.getName(fragment));
	}

	private static final long serialVersionUID = 7579478541873972798L;

	//FIXME add ManifestFragment field

	/**
	 * @param errorCode
	 * @param message
	 * @param cause
	 */
	public ManifestException(ErrorCode errorCode, String message,
			@Nullable Throwable cause) {
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
	public ManifestException(ErrorCode errorCode, @Nullable Throwable cause) {
		super(errorCode, cause);
	}
}
