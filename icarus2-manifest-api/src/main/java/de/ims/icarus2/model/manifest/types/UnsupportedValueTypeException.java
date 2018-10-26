/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.types;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;


/**
 * @author Markus Gärtner
 *
 */
public class UnsupportedValueTypeException extends ManifestException {

	private static final long serialVersionUID = 3427046321201797935L;

	private final ValueType valueType;

	private static final String DEFAULT_MESSAGE = "Value type not supported: "; //$NON-NLS-1$

	public UnsupportedValueTypeException(String message, ValueType valueType) {
		super(ManifestErrorCode.MANIFEST_UNSUPPORTED_TYPE, message);

		this.valueType = valueType;
	}

	public UnsupportedValueTypeException(ValueType valueType) {
		this(DEFAULT_MESSAGE+valueType, valueType);
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return valueType;
	}
}
