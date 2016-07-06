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
package de.ims.icarus2.model.manifest.types;

import de.ims.icarus2.IcarusException;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;


/**
 * @author Markus Gärtner
 *
 */
public class UnsupportedValueTypeException extends IcarusException {

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
