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
package de.ims.icarus2.query.api.engine.result;

import java.util.function.Predicate;

import de.ims.icarus2.query.api.engine.ThreadVerifier;

/**
 * Manages the collection of matches into {@link ResultEntry} instances
 * and the extraction of data for sorting, grouping and other forms of
 * result processing.
 *
 * @author Markus Gärtner
 *
 */
public class TerminalCollector implements MatchCollector {

	private final ThreadVerifier threadVerifier;
	private final Extractor[] extractors;
	private final int payloadSize;
	private final Predicate<ResultEntry> sink;

	@Override
	public boolean collect(MatchSource source) {
		if(Tripwire.ACTIVE) {
			threadVerifier.checkThread();
		}

		final ResultEntry entry = new ResultEntry(source.toMatch(), payloadSize);
		final long[] payload = entry.payload;
		for (int i = 0; i < extractors.length; i++) {
			extractors[i].extract(payload);
		}

		return sink.test(entry);
	}
}
