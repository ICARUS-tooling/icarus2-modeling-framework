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
package de.ims.icarus2.query.api.engine.result;

import de.ims.icarus2.query.api.engine.GenericSink;
import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;
import de.ims.icarus2.query.api.iql.IqlResult;

/**
 * @author Markus Gärtner
 *
 */
public interface ResultSink extends GenericSink {

	/**
	 * Consume the given match, which might be a {@link MultiMatch} instance.
	 * Note that the order of matches provided to this method is highly dependent
	 * on the configuration of the query's {@link IqlResult result} section.
	 * Only in the presence of explicit sorting statements is the order truly
	 * deterministic! Even with buffered results a reproducible result order
	 * cannot be guaranteed. Implementations should therefore not put too much
	 * emphasis on the order at which matches arrive, unless the know the
	 * query to contain sorting statements.
	 */
	void add(Match match);

	void add(ResultEntry entry, PayloadReader payloadReader);
}
