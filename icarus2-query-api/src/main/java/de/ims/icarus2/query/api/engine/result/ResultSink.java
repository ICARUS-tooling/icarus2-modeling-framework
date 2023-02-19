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

import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;
import de.ims.icarus2.query.api.iql.IqlResult;

/**
 * @author Markus Gärtner
 *
 */
public interface ResultSink {

	/**
	 * Initialize the sink and prepare if for an as of yet unknown number of
	 * matches to be consumed. This initialization method is used when the
	 * engine cannot estimate the number of results and when result processing
	 * is performed live, i.e. the {@link QueryOutput} implementation is not
	 * using any form of buffering.
	 */
	void prepare();

	/**
	 * Initialize the sink to expect a given number of matches to be consumed
	 * (given as upper limit).
	 * This initialization method is used whenever the engine can reliably
	 * determine the total number of results, e.g. from a set result size
	 * limit in the query.
	 * @param size the maximum number of matches to be expected
	 */
	void prepare(int size);

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

	/**
	 * Called when the search process was terminated prematurely, either due to
	 * buffer problems or an (internal) error. The behavior of this method is
	 * largely unspecified and implementation-dependent. Generally an implementation
	 * should make an effort to gracefully discard already consumed matches and
	 * roll back any save operations made for those matches.
	 * <p>
	 * The engine guarantees that this method will called a maximum of one times
	 * for any {@link ResultSink} instance.
	 */
	void discard() throws InterruptedException;

	/**
	 * Called by the engine after the search terminated successfully.
	 * <p>
	 * The engine guarantees that this method will called a maximum of one times
	 * for any {@link ResultSink} instance when the last valid match has been
	 * consumed.
	 */
	void finish() throws InterruptedException;
}
