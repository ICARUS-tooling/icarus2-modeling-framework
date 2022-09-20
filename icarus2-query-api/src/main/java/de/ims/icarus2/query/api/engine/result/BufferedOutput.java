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

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.query.api.engine.ThreadVerifier;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BufferedOutput<T> extends AbstractOutput {

	private static final Logger log = LoggerFactory.getLogger(BufferedOutput.class);

	public static BufferedOutput<Match> nonExtracting(ResultBuffer<Match> buffer,
			ResultSink resultSink) {
		return new NonExtracting(buffer, resultSink);
	}

	public static BufferedOutput<ResultEntry> extracting(ResultBuffer<ResultEntry> buffer,
			ResultSink resultSink, PayloadReader payloadReader, Extractor...extractors) {
		return new Extracting(buffer, resultSink, payloadReader, extractors);
	}

	protected final ResultBuffer<T> buffer;

	protected final ResultSink resultSink;

	protected BufferedOutput(ResultBuffer<T> buffer, ResultSink resultSink) {
		this.buffer = requireNonNull(buffer);
		this.resultSink = requireNonNull(resultSink);
	}

	@Override
	public final long countMatches() { return buffer.size(); }

	@Override
	public final boolean isFull() { return buffer.isFull(); }

	@Override
	public void discard() {
		try {
			resultSink.discard();
		} catch (InterruptedException e) {
			log.error("Disrupted while discarding result sink data", e);
		}
	}

	protected abstract void toSink() throws InterruptedException;

	@Override
	protected void finish() {
		buffer.finish();
		resultSink.prepare(buffer.size());
		try {
			toSink();
			resultSink.finish();
		} catch (InterruptedException e) {
			log.info("Finalizing of result sink got interrupted", e);
			discard();
		}
	}

	/**
	 * Direct forwarding of matches to buffer.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class NonExtracting extends BufferedOutput<Match> {

		NonExtracting(ResultBuffer<Match> buffer, ResultSink resultSink) {
			super(buffer, resultSink);
		}

		@Override
		protected void toSink() throws InterruptedException {
			buffer.forEachEntry(resultSink::add);
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.matchSink(buffer.createCollector(threadVerifier))
					.build();
		}
	}

	/**
	 * Extraction of data for sorting etc.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class Extracting extends BufferedOutput<ResultEntry> {

		private final Extractor[] extractors;
		private final PayloadReader payloadReader;

		Extracting(ResultBuffer<ResultEntry> buffer, ResultSink resultSink,
				PayloadReader payloadReader, Extractor[] extractors) {
			super(buffer, resultSink);
			this.payloadReader = requireNonNull(payloadReader);
			// defensive copying
			this.extractors = extractors.clone();
		}

		private void delegateResultEntry(ResultEntry entry) {
			resultSink.add(entry, payloadReader);
		}

		@Override
		protected void toSink() throws InterruptedException {
			buffer.forEachEntry(this::delegateResultEntry);
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.resultEntrySink(buffer.createCollector(threadVerifier))
					.payloadSize(extractors.length)
					.extractors(extractors)
					.build();
		}
	}
}
