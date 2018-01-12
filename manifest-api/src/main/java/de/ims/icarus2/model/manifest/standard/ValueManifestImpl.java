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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public class ValueManifestImpl extends DefaultModifiableIdentity implements ValueManifest {

	private Object value;
	private Documentation documentation;
	private final ValueType valueType;

	private static final Set<ValueType> supportedValueTypes = ValueType.filterWithout(
			ValueType.UNKNOWN,
			ValueType.CUSTOM,
			ValueType.IMAGE_RESOURCE,
			ValueType.URL_RESOURCE);

	public ValueManifestImpl(ValueType valueType) {
		requireNonNull(valueType);

		if(!supportedValueTypes.contains(valueType))
			throw new UnsupportedValueTypeException(valueType);

		this.valueType = valueType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ValueManifest#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @return the documentation
	 */
	@Override
	public Documentation getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	@Override
	public void setDocumentation(Documentation documentation) {
		checkNotLocked();

		setDocumentation0(documentation);
	}

	protected void setDocumentation0(Documentation documentation) {
		this.documentation = documentation;
	}

	/**
	 * @param value the value to set
	 */
	@Override
	public void setValue(Object value) {
		checkNotLocked();

		setValue0(value);
	}

	protected void setValue0(Object value) {
		requireNonNull(value);

		this.value = value;
	}

}
