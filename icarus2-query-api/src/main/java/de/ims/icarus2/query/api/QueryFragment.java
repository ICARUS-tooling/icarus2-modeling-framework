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
package de.ims.icarus2.query.api;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class QueryFragment {

	private final String source;
	private final int begin, end;

	/**
	 * @param source the {@link Query}
	 * @param begin first character index to include (inclusive)
	 * @param end last character index to include (inclusive)
	 */
	public QueryFragment(String source, int begin, int end) {
		requireNonNull(source);
		checkArgument("begin must not be negative", begin>=0);
		checkArgument("end must not be less then begin", end>=begin);
		checkArgument("end must not exceed source query length", end<source.length());

		this.source = source;
		this.begin = begin;
		this.end = end;
	}

	public String getSource() {
		return source;
	}
	public int getBegin() {
		return begin;
	}
	public int getEnd() {
		return end;
	}

	public String getText() {
		return source.substring(begin, end+1);
	}
}
