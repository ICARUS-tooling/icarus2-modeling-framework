/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base type for unique elements that are referenced via a explicit name.
 *
 * @author Markus Gärtner
 *
 */
public abstract class IqlNamedReference extends IqlUnique {

	@JsonProperty(value=IqlTags.NAME, required=true)
	private String name;

	public String getName() { return name; }

	public void setName(String name) { this.name = checkNotEmpty(name); }

	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(name, IqlTags.NAME);
	}
}
