/**
 *
 */
package de.ims.icarus2.query.api.engine.filter;

import de.ims.icarus2.query.api.engine.result.ResultSink;

/**
 * Extends the {@link ResultSink} interface with a mechanism to signal the absence
 * of produced candidates in a way that doesn't contradict the original contract.
 * <p>
 * Normally the workflow would go something like this:
 * <br>
 * <pre>
 * {@code
 * ResultSink sink = ...;
 * sink.prepare();
 * // Fill sink here
 * sink.finish();
 * }
 * </pre>
 *
 * @author Markus Gärtner
 *
 */
public interface CandidateSink extends ResultSink {

	/**
	 * Tells the sink that the filter process has finished and was unable to produce any candidates
	 * due to its inability to actually filter based on the given query fragment(s). This allows
	 * to break the {@link #prepare()} - {@link #finish()} lifecycle rule. A normal
	 * {@link #prepare()} - {@link #finish()} invocation without intermediary calls to
	 * {@link #add(de.ims.icarus2.query.api.engine.result.Match)} or {@link #add(de.ims.icarus2.query.api.engine.result.ResultEntry,
	 * de.ims.icarus2.query.api.engine.result.PayloadReader)} indicates that the constraints expressed
	 * in the query did not yield a single valid match. In contrast, using {@link #ignore()} tells
	 * the query engine that the filter was unable to handle the given query fragment(s) and
	 * no actual filtering happened at all.
	 * <p>
	 * Note that calling this method effectively disables the sink for any reception of further
	 * candidates, similar to what {@link #finish()} or {@link #discard()} would do.
	 */
	void ignore();
}
