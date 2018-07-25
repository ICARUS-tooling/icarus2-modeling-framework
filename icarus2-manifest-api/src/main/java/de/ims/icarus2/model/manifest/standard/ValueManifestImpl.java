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
