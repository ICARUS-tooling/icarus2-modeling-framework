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

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Map;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.QueryOutput;
import de.ims.icarus2.query.api.engine.ThreadVerifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public abstract class BufferedOutput<T> implements QueryOutput {

	public static BufferedOutput<Match> nonExtracting(ResultBuffer<Match> buffer) {
		return new NonExtracting(buffer);
	}

	public static BufferedOutput<ResultEntry> extracting(ResultBuffer<ResultEntry> buffer,
			Extractor...extractors) {
		return new Extracting(buffer, extractors);
	}

	private final ResultBuffer<T> buffer;

	private final Map<Thread, MatchCollector> openCollectors = new Object2ObjectOpenHashMap<>();
	private final Object collectorLock = new Object();

	protected BufferedOutput(ResultBuffer<T> buffer) {
		this.buffer = requireNonNull(buffer);
	}

	@Override
	public final MatchCollector createTerminalCollector(ThreadVerifier threadVerifier) {
		MatchCollector collector = createRawCollector(threadVerifier);
		synchronized (collectorLock) {
			if(openCollectors.put(threadVerifier.getThread(), collector)!=null)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						"Thread already has an active collector: "+threadVerifier.getThread());
		}
		return collector;
	}

	protected abstract MatchCollector createRawCollector(ThreadVerifier threadVerifier);

	@Override
	public final void closeTerminalCollector(ThreadVerifier threadVerifier) {
		synchronized (collectorLock) {
			MatchCollector collector = openCollectors.remove(threadVerifier.getThread());
			if(collector==null)
				throw new QueryException(GlobalErrorCode.INVALID_INPUT,
						"No open collector available for thread: "+threadVerifier.getThread());

			// When last collector is closed we need to also finalize our result buffer
			if(openCollectors.isEmpty()) {
				buffer.finish();
			}
		}
	}

	@Override
	public void close() {
		synchronized (collectorLock) {
			checkState("Unclosed collectors left over", openCollectors.isEmpty());
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

		NonExtracting(ResultBuffer<Match> buffer) {
			super(buffer);
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

		Extracting(ResultBuffer<ResultEntry> buffer, Extractor[] extractors) {
			super(buffer);
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
