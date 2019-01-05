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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.types.UnsupportedValueTypeException;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public class ValueManifestImpl extends DefaultModifiableIdentity<ValueManifest> implements ValueManifest {

	private Optional<Object> value = Optional.empty();
	private Optional<Documentation> documentation = Optional.empty();
	private final ValueType valueType;

	public ValueManifestImpl(ValueType valueType) {
		requireNonNull(valueType);

		if(!ValueManifest.SUPPORTED_VALUE_TYPES.contains(valueType))
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
	public Optional<Object> getValue() {
		return value;
	}

	/**
	 * @return the documentation
	 */
	@Override
	public Optional<Documentation> getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	@Override
	public ValueManifest setDocumentation(Documentation documentation) {
		checkNotLocked();

		setDocumentation0(documentation);

		return this;
	}

	protected void setDocumentation0(Documentation documentation) {
		this.documentation = Optional.of(documentation);
	}

	/**
	 * @param value the value to set
	 */
	@Override
	public ValueManifest setValue(Object value) {
		checkNotLocked();

		setValue0(value);

		return this;
	}

	protected void setValue0(Object value) {
		requireNonNull(value);

		valueType.checkValue(value);

		this.value = Optional.of(value);
	}

}
