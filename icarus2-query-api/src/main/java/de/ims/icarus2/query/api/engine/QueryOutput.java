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
package de.ims.icarus2.query.api.engine;

import de.ims.icarus2.query.api.engine.result.MatchCollector;

/**
 * @author Markus Gärtner
 *
 */
public interface QueryOutput {

	/** Creates a new handler that collects results for the current thread in the given lane,
	 * verified by the given {@link ThreadVerifier}. */
	MatchCollector createCollector(LaneSetup lane, ThreadVerifier threadVerifier);

	/** Makes sure the result handler for the specified lane that was used in the current
	 * thread is closed down and buffer resources are freed up. */
	void closeCollector(LaneSetup lane);

	/** Estimates the number of matches so far. The returned value is a best-effort guess
	 * as long as the associated search is still in progress. Only when it is completed
	 * (either by finishing successfully or being canceled/failed) does the number represent
	 * the final number of matches. Note that for a search with limited result capacity
	 * the reported number can temporarily exceed the result limit, but the final number
	 * will not. */
	long countMatches();

	/** Returns {@code true} if the query output was limited and that limit has already
	 * been reached with the results collected so far. */
	boolean isFull();
}
