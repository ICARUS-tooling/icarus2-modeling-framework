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

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import de.ims.icarus2.query.api.QueryFragment;

/**
 * @author Markus Gärtner
 *
 */
public final class IqlUtils {

	public static void main(String[] args) {
		//TODO add code to produce JSON schema from POJO
	}

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jdk8Module()); // Properly handles java.util.Optional
		//TODO apply default configuration for IQL
		return mapper;
	}

	public static QueryFragment fragment(String query, JsonLocation loc) {
		requireNonNull(query);
		if(loc==null) {
			return null;
		}
		long offset = loc.getCharOffset();
		if(offset==-1L) {
			return null;
		}
		int _offset = strictToInt(offset);
		return new QueryFragment(query, _offset, _offset);
	}
}
