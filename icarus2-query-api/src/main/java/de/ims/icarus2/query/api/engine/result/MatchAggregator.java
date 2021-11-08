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

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

/**
 * Aggregates the individual matches from multiple lanes into a
 * single {@link MultiMatch} and delegates to another {@link MatchCollector collector}.
 *
 * @author Markus Gärtner
 *
 */
public class MatchAggregator implements MatchCollector {

	/** The n-1 initial sources for individual matches */
	private final MatchSource[] sources;
	/** Destination for the constructed final aggregated match */
	private final MatchCollector sink;

	private final MultiMatchBuilder matchBuilder = new MultiMatchBuilder();

	public MatchAggregator(MatchSource[] sources, MatchCollector sink) {
		this.sources = sources.clone();
		this.sink = requireNonNull(sink);
	}

	/**
	 * Combines all the individual lane matches into a single
	 * {@link MultiMatch} and delegates it to the internal {@link Predicate sink}
	 * defined at construction time.
	 */
	@Override
	public boolean collect(MatchSource source) {
		// Apply lanes #0 to #n-1
		for (int i = 0; i < sources.length; i++) {
			sources[i].drainTo(matchBuilder);
		}
		// Apply lane #n
		source.drainTo(matchBuilder);

		MultiMatch match = matchBuilder.build();
		return sink.collect(match);
	}
}
