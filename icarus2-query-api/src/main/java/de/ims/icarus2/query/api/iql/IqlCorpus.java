/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.Conditions.checkNullOrNotEmpty;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlCorpus extends IqlNamedReference {

	//TODO extra fields?

	/**
	 * Describes a persistent identifier stemming from a third-party
	 * storage system or referencing service.
	 */
	@JsonProperty(value=IqlProperties.PID)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> pid = Optional.empty();

	@Override
	public IqlType getType() { return IqlType.CORPUS; }

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlNamedReference#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();

		checkOptionalStringNotEmpty(pid, IqlProperties.PID);
	}

	public Optional<String> getPid() { return pid; }

	public void setPid(String pid) { this.pid = Optional.ofNullable(checkNullOrNotEmpty(pid)); }
}
