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
package de.ims.icarus2.query.api.engine.filter;

import de.ims.icarus2.query.api.engine.GenericSink;

/**
 * Extends the {@link GenericSink} interface with a mechanism to signal the absence
 * of produced candidates in a way that doesn't contradict the original contract.
 * <p>
 * Normally the workflow would go something like this:
 * <br>
 * <pre>
 * {@code
 * GenericSink sink = ...;
 * sink.prepare();
 * // Fill sink here
 * sink.finish();
 * }
 * </pre>
 * <p>
 * If no candidates were to be added to the sink, the cases of not having received
 * any candidates (i.e. the result set is empty) or the inability to evaluate a
 * given query for candidate construction could not properly differentiated.
 * To this end the {@link CandidateSink} interface adds the {@link #ignore()} method
 * for the case that a filter is unable to serve a request.
 *
 * @author Markus Gärtner
 *
 */
public interface CandidateSink extends GenericSink {

	/**
	 * Tells the sink that the filter process has finished and was unable to produce any candidates
	 * due to its inability to actually filter based on the given query fragment(s). This allows
	 * to break the {@link #prepare()} - {@link #finish()} lifecycle rule. A normal
	 * {@link #prepare()} - {@link #finish()} invocation without intermediary calls to
	 * {@link #add(de.ims.icarus2.query.api.engine.result.Match)} or {@link #add(de.ims.icarus2.query.api.engine.result.ResultEntry,
	 * de.ims.icarus2.query.api.engine.result.PayloadReader)} indicates that the constraints expressed
	 * in the query did not yield a single valid match. In contrast, using {@link #ignore()} tells
	 * the query engine that the filter was unable to handle the given query fragment(s) and
	 * no actual filtering happened at all.
	 * <p>
	 * Note that calling this method effectively disables the sink for any reception of further
	 * candidates, similar to what {@link #finish()} or {@link #discard()} would do.
	 */
	void ignore();

	/**
	 *
	 * @param candidate
	 */
	void add(long candidate) throws InterruptedException;

	/**
	 *
	 * @param candidate
	 */
	default void add(long[] candidates) throws InterruptedException {
		add(candidates, 0, candidates.length);
	}

	void add(long[] candidates, int offset, int len) throws InterruptedException;

	//TODO add mechanic to check for active sink?
}
