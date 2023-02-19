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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.engine.ThreadVerifier;

/**
 * {@link QueryOutput} implementation that solely relies on an internal
 * {@link Consumer} to process result data and without any form of buffering.
 * <p>
 * Note that this implementation does <b>not</b> offer synchronization for
 * the supplied {@link Consumer}, so client code has to ensure that the
 * implementation used provides sufficient thread-safety!
 *
 *
 * @author Markus Gärtner
 *
 */
public abstract class UnbufferedOutput<T> extends AbstractOutput {

	private static final Logger log = LoggerFactory.getLogger(UnbufferedOutput.class);

	public static UnbufferedOutput<Match> nonExtracting(ResultSink resultSink) {
		return new NonExtracting(resultSink);
	}

	public static UnbufferedOutput<ResultEntry> extracting(ResultSink resultSink,
			PayloadReader payloadReader, Extractor...extractors) {
		return new Extracting(resultSink, payloadReader, extractors);
	}

	protected final LongAdder size = new LongAdder();

	protected final ResultSink resultSink;

	protected UnbufferedOutput(ResultSink resultSink) {
		this.resultSink = requireNonNull(resultSink);
		resultSink.prepare();
	}

	@Override
	public long countMatches() { return size.sum(); }

	@Override
	public boolean isFull() {  return false; }

	@Override
	public void discard() {
		try {
			resultSink.discard();
		} catch (InterruptedException e) {
			log.error("Disrupted while discarding result sink data", e);
		}
	}

	@Override
	protected void finish() {
		try {
			resultSink.finish();
		} catch (InterruptedException e) {
			log.info("Finalizing of result sink got interrupted", e);
			discard();
		}
	}

	/**
	 * Direct forwarding of matches to consumer.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class NonExtracting extends UnbufferedOutput<Match> {

		NonExtracting(ResultSink resultSink) {
			super(resultSink);
		}

		private final Predicate<Match> externalCollector() {
			return item -> {
				resultSink.add(item);
				size.increment();
				return true;
			};
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.matchSink(externalCollector())
					.build();
		}
	}

	/**
	 * Extraction of data for subsequent result processing etc.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class Extracting extends UnbufferedOutput<ResultEntry> {

		private final Extractor[] extractors;
		private final PayloadReader payloadReader;

		Extracting(ResultSink resultSink, PayloadReader payloadReader, Extractor[] extractors) {
			super(resultSink);
			this.payloadReader = requireNonNull(payloadReader);
			this.extractors = extractors.clone();
		}

		private final Predicate<ResultEntry> externalCollector() {
			return entry -> {
				resultSink.add(entry, payloadReader);
				size.increment();
				return true;
			};
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.resultEntrySink(externalCollector())
					.payloadSize(extractors.length)
					.extractors(extractors)
					.build();
		}
	}
}
