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
package de.ims.icarus2.query.api.engine.result;

/**
 * Describes the basic contract of match producing members in the query framework.
 *
 * @author Markus Gärtner
 *
 */
public interface MatchSource {

	/** Converts the data in this {@link MatchSource} into a ready to use
	 * and decoupled {@link Match} instance. Can return {@code this} object
	 * if the implementation also implements {@link Match} directly. */
	Match toMatch();

	/** Send the match data currently stored in this {@link MatchSource} to
	 *  the specified {@link MatchSink sink}. This is usually more efficient
	 *  for intermediate match handling compared to {@link #toMatch() creating}
	 *  a new {@link Match} instance and sending it to a consumer. */
	void drainTo(MatchSink sink);
}
