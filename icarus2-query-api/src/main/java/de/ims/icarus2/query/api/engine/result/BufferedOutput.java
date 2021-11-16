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

import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ims.icarus2.query.api.engine.ThreadVerifier;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BufferedOutput<T> extends AbstractOutput {

	public static BufferedOutput<Match> nonExtracting(ResultBuffer<Match> buffer,
			Consumer<Match> finalMatchConsumer) {
		return new NonExtracting(buffer, finalMatchConsumer);
	}

	public static BufferedOutput<ResultEntry> extracting(ResultBuffer<ResultEntry> buffer,
			Consumer<ResultEntry> finalMatchConsumer, Extractor...extractors) {
		return new Extracting(buffer, finalMatchConsumer, extractors);
	}

	private final ResultBuffer<T> buffer;

	private final Consumer<T> finalMatchConsumer;

	protected BufferedOutput(ResultBuffer<T> buffer, @Nullable Consumer<T> finalMatchConsumer) {
		this.buffer = requireNonNull(buffer);
		this.finalMatchConsumer = finalMatchConsumer;
	}

	@Override
	protected final void finish() {
		// Ensure buffer is properly finished and merged
		buffer.finish();
		// Now send buffered matches to consumer if present
		if(finalMatchConsumer!=null) {
			buffer.forEachEntry(finalMatchConsumer);
		}
	}

	@Override
	public final long countMatches() { return buffer.size(); }

	@Override
	public final boolean isFull() { return buffer.isFull(); }

	protected final ResultBuffer<T> buffer() { return buffer; }

	/**
	 * Direct forwarding of matches to buffer.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class NonExtracting extends BufferedOutput<Match> {

		NonExtracting(ResultBuffer<Match> buffer, @Nullable Consumer<Match> finalMatchConsumer) {
			super(buffer, finalMatchConsumer);
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.matchSink(buffer().createCollector(threadVerifier))
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

		Extracting(ResultBuffer<ResultEntry> buffer, @Nullable Consumer<ResultEntry> finalMatchConsumer, Extractor[] extractors) {
			super(buffer, finalMatchConsumer);
			// defensive copying
			this.extractors = extractors.clone();
		}

		@Override
		protected MatchCollector createRawCollector(ThreadVerifier threadVerifier) {
			return new TerminalCollectorFactory()
					.threadVerifier(threadVerifier)
					.resultEntrySink(buffer().createCollector(threadVerifier))
					.payloadSize(extractors.length)
					.extractors(extractors)
					.build();
		}
	}
}
