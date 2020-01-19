/**
 *
 */
package de.ims.icarus2.query.api;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class QueryFragment {

	private final Query source;
	private final int begin, end;

	/**
	 * @param source the {@link Query}
	 * @param begin
	 * @param end
	 */
	public QueryFragment(Query source, int begin, int end) {
		requireNonNull(source);
		checkArgument("begin must not be negative", begin>=0);
		checkArgument("end must not be less then begin", end>=begin);
		checkArgument("end must not exceed source query length", end<source.getText().length());

		this.source = source;
		this.begin = begin;
		this.end = end;
	}

	public Query getSource() {
		return source;
	}
	public int getBegin() {
		return begin;
	}
	public int getEnd() {
		return end;
	}

	public String getText() {
		return source.getText().substring(begin, end+1);
	}
}
