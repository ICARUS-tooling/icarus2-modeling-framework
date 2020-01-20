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
public abstract class IqlAliasedReference extends IqlUnique {

	@JsonProperty(IqlProperties.NAME)
	private String name;

	@JsonProperty(IqlProperties.ALIAS)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> alias = Optional.empty();

	public String getName() { return name; }

	public Optional<String> getAlias() { return alias; }

	public void setName(String name) { this.name = checkNotEmpty(name); }

	public void setAlias(String alias) { this.alias = Optional.of(checkNotEmpty(alias)); }

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkStringNotEmpty(name, IqlProperties.NAME);

		checkOptionalStringNotEmpty(alias, IqlProperties.ALIAS);
	}
}
