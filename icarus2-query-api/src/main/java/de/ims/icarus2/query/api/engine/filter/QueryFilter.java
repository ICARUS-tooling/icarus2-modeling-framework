/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.filter;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.ResultSink;

/**
 * @author Markus Gärtner
 *
 */
public interface QueryFilter extends DriverModule {

	/**
	 * Attempts to produce result candidates for a part or all of the query payload.
	 * The {@link FilterContext context} parameter provides full access to all the
	 * required information.
	 * <p>
	 * This method is expected to analyze the query content, check with the connected
	 * back-end system, such as an index, and determine whether or not it is able to
	 * <i>potentially</i> generate candidates for the query evaluation.
	 * <p>
	 * <b>Filtering Results:</b>
	 * <br>
	 * Intermediary candidates are sent to the {@code FilterContext#getSink() sink} as {@link Match matches}.
	 * Note that those matches as a minimum only need to provide the {@link Match#getIndex() index}
	 * of their surrounding result segment, such as a sentence. More fine-grained mapping information,
	 * such as individual token (word) nodes, is optional and the query engine might decide to run
	 * a regular exhaustive evaluation of the candidates anyway.
	 * <p>
	 * <b>Filtering Process:</b>
	 * <br>
	 * It is up to the back-end implementation to decide at what stage of the data retrieval candidates
	 * should be sent to the {@link CandidateSink sink}. If it has to communicate with a remote service,
	 * such as a SQL database, it might not be possible to stream individual matches while the evaluation
	 * is still in progress. In such cases all results should be collected, translated to {@link Match}
	 * objects and then forwarded to the {@code sink} (preferably by letting the sink know about the
	 * actual number of candidates to expect when {@link CandidateSink#prepare(int) preparing} it).
	 * <br>
	 * However, for cases where the filter process is exposed to more detailed control, e.g. when
	 * using a LUCENE index, intermediary results can be streamed to the {@code sink} while the
	 * evaluation process is still ongoing. This can enable the query engine to more efficiently
	 * process candidates from multiple filter sources in parallel instead of intersecting them
	 * sequentially.
	 * <br>
	 * No matter <i>how</i> the candidates are produced, it is essential for the query engine to let
	 * it know exactly <i>when</i> the filter process has ended, either successfully or without the production
	 * of any candidates. For this purpose the {@link CandidateSink} needs its {@link ResultSink#finish()}
	 * to be called as soon as the filter is done with producing candidates. If no candidates could be
	 * produced and none of the {@code prepare} methods have been called, the {@code sink} needs to
	 * be told to {@link CandidateSink#ignore() ignore} the filter explicitly.
	 *
	 * @param context
	 * @throws IcarusApiException in case of serious errors that are outside of the filter's control,
	 * such as I/O or connection issues.
	 * @throws QueryException for faulty usage or bugs.
	 * @throws InterruptedException in case the filter got {@link #cancel() canceled} before finishing
	 */
	void filter(FilterContext context) throws IcarusApiException, QueryException, InterruptedException;
}
