/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.types;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.manifest.ManifestErrorCode;

/**
 * Special exception class for the {@link ManifestErrorCode#MANIFEST_TYPE_CAST}
 * error type.
 * <p>
 * This is a <i>checked exception</i> class.
 *
 * @author Markus Gärtner
 *
 */
public class ValueConversionException extends IcarusApiException {
	private static final long serialVersionUID = 9078565278093991307L;

	private final ValueType type;
	private final Object data;
	private final boolean serialization;

	/**
	 * @param type
	 * @param data
	 * @param serialization
	 */
	public ValueConversionException(String message, Throwable cause,
			ValueType type, Object data, boolean serialization) {
		super(ManifestErrorCode.MANIFEST_TYPE_CAST, message, cause);
		this.type = requireNonNull(type);
		this.data = requireNonNull(data);
		this.serialization = serialization;
	}

	public ValueType getType() {
		return type;
	}

	/**
	 * Returns either the object to be serialized or the {@link CharSequence} to be parsed.
	 * <p>
	 * Which of the two can be expected is dendent on the return value of {@link #isSerialization()}!
	 *
	 * @return
	 */
	public Object getData() {
		return data;
	}

	public boolean isSerialization() {
		return serialization;
	}
}
