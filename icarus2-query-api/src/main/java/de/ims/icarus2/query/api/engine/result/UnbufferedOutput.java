/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

	public static UnbufferedOutput<Match> nonExtracting(Consumer<Match> collector) {
		return new NonExtracting(collector);
	}

	public static UnbufferedOutput<ResultEntry> extracting(Consumer<ResultEntry> collector,
			Extractor...extractors) {
		return new Extracting(collector, extractors);
	}

	private final LongAdder size = new LongAdder();

	private final Consumer<T> collector;

	protected UnbufferedOutput(Consumer<T> collector) {
		this.collector = requireNonNull(collector);
	}

	@Override
	public long countMatches() { return size.sum(); }

	@Override
	public boolean isFull() {  return false; }

	private final void process(T element) {
		collector.accept(element);
		size.increment();
	}

	protected final Predicate<T> externalCollector() {
		return item -> {
			process(item);
			return true;
		};
	}

	@Override
	protected void finish() { /* no-op */ }

	/**
	 * Direct forwarding of matches to consumer.
	 *
	 * @author Markus Gärtner
	 *
	 */
	static final class NonExtracting extends UnbufferedOutput<Match> {

		NonExtracting(Consumer<Match> finalMatchConsumer) {
			super(finalMatchConsumer);
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

		Extracting(Consumer<ResultEntry> finalMatchConsumer, Extractor[] extractors) {
			super(finalMatchConsumer);
			this.extractors = extractors.clone();
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
