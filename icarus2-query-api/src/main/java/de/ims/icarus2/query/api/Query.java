/**
 *
 */
package de.ims.icarus2.query.api;

import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class Query {

	private final String text;

	/**
	 * @param text
	 */
	public Query(String text) {
		this.text = requireNonNull(text);
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
}
