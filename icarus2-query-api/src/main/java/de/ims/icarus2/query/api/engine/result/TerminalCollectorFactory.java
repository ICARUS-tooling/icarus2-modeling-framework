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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import de.ims.icarus2.query.api.engine.ThreadVerifier;
import de.ims.icarus2.query.api.engine.Tripwire;
import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Manages the collection of matches into {@link ResultEntry} instances
 * and the extraction of data for sorting, grouping and other forms of
 * result processing.
 *
 * @author Markus Gärtner
 *
 */
public class TerminalCollectorFactory extends AbstractBuilder<TerminalCollectorFactory, MatchCollector> {
	private ThreadVerifier threadVerifier;
	private final List<Extractor> extractors = new ObjectArrayList<>();
	private Integer payloadSize;
	private Predicate<ResultEntry> resultEntrySink;
	private Predicate<Match> matchSink;

	public TerminalCollectorFactory threadVerifier(ThreadVerifier threadVerifier) {
		checkState("thread verifier already set", this.threadVerifier==null);
		this.threadVerifier = requireNonNull(threadVerifier);
		return this;
	}

	public TerminalCollectorFactory extractors(Extractor...extractors) {
		CollectionUtils.feedItems(this.extractors, extractors);
		return this;
	}

	public TerminalCollectorFactory payloadSize(int payloadSize) {
		checkState("payload size already set", this.payloadSize==null);
		checkArgument("payload size must be positive", payloadSize>0);
		this.payloadSize = Integer.valueOf(payloadSize);
		return this;
	}

	public TerminalCollectorFactory resultEntrySink(Predicate<ResultEntry> resultEntrySink) {
		checkState("result entry sink already set", this.resultEntrySink==null);
		this.resultEntrySink = requireNonNull(resultEntrySink);
		return this;
	}

	public TerminalCollectorFactory matchSink(Predicate<Match> matchSink) {
		checkState("match sink already set", this.matchSink==null);
		this.matchSink = requireNonNull(matchSink);
		return this;
	}

	ThreadVerifier threadVerifier() { return threadVerifier; }
	Extractor[] extractors() { return extractors.toArray(new Extractor[extractors.size()]); }
	int payloadSize() { return payloadSize.intValue(); }
	Predicate<ResultEntry> resultEntrySink() { return resultEntrySink; }
	Predicate<Match> matchSink() { return matchSink; }

	@Override
	protected void validate() {
		checkState("thread verifier not set", threadVerifier!=null);

		if(resultEntrySink!=null) {
			checkState("must not set match sink when result entry sink is set", matchSink==null);
			checkState("no extractors added", !extractors.isEmpty());
			checkState("payload size not set", payloadSize!=null);
		} else if(matchSink!=null) {
			checkState("match sink does not use extractors", extractors.isEmpty());
			checkState("match sink does not payload", payloadSize==null);
		} else
			throw new IllegalStateException("Must provide a sink for either matches or result entry objects");

	}

	@Override
	protected MatchCollector create() {
		return resultEntrySink!=null ? new Extracting(this) : new NonExtracting(this);
	}

	static class NonExtracting implements MatchCollector, Predicate<MultiMatch> {
		private final ThreadVerifier threadVerifier;
		private final Predicate<Match> sink;

		private NonExtracting(TerminalCollectorFactory factory) {
			threadVerifier = factory.threadVerifier();
			sink = factory.matchSink();
		}

		@Override
		public boolean collect(MatchSource source) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			return sink.test(source.toMatch());
		}

		@Override
		public boolean test(MultiMatch match) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			return sink.test(match);
		}
	}

	static class Extracting implements MatchCollector, Predicate<MultiMatch> {

		private final ThreadVerifier threadVerifier;
		private final Extractor[] extractors;
		private final int payloadSize;
		private final Predicate<ResultEntry> sink;

		private Extracting(TerminalCollectorFactory factory) {
			threadVerifier = factory.threadVerifier();
			extractors = factory.extractors();
			payloadSize = factory.payloadSize();
			sink = factory.resultEntrySink();
		}

		private boolean process(Match match) {
			final ResultEntry entry = new ResultEntry(match, payloadSize);
			final long[] payload = entry.payload;
			for (int i = extractors.length-1; i >= 0; i--) {
				extractors[i].extract(payload);
			}

			return sink.test(entry);
		}

		@Override
		public boolean collect(MatchSource source) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			return process(source.toMatch());
		}

		@Override
		public boolean test(MultiMatch match) {
			if(Tripwire.ACTIVE) {
				threadVerifier.checkThread();
			}

			return process(match);
		}
	}
}
