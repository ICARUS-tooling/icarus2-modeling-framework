/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlProperty extends AbstractIqlQueryElement {

	/**
	 * Mandatory key used to identify this property or switch
	 */
	@JsonProperty(value=IqlTags.KEY, required=true)
	private String key;

	/**
	 * Value to set for this property if it is not a switch.
	 * Can be either a String or any primitive value.
	 */
	@JsonProperty(IqlTags.VALUE)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<Object> value = Optional.empty();

	@Override
	public IqlType getType() { return IqlType.PROPERTY; }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(key, IqlTags.KEY);
	}

	public String getKey() { return key; }

	public Optional<Object> getValue() { return value; }

	public void setKey(String key) { this.key = checkNotEmpty(key); }

	public void setValue(Object value) { this.value = Optional.of(value); }
}
