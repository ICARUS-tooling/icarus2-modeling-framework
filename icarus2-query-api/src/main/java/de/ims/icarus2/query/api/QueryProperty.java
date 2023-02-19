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
package de.ims.icarus2.query.api;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.query.api.iql.IqlConstants;

/**
 * @author Markus Gärtner
 *
 */
public enum QueryProperty {

	DEFAULT_NODE_ARRANGEMENT("default.arrangement", "Defines a default arrangement for node sequences to be used in the query"),
	;

	private final String key, description;

	private QueryProperty(String key, String description) {
		this.key = IqlConstants.IQL_PREFIX+'.'+checkNotEmpty(key);
		this.description = requireNonNull(description);
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}
	//TODO property for defining default node arrangement for a query
}
