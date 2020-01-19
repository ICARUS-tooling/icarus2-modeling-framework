/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.query.api.QueryFragment;

/**
 * @author Markus Gärtner
 *
 */
public class IqlException extends IcarusApiException {

	private static final long serialVersionUID = 466513317359445843L;

	/** Part of the original query that led to this error */
	private final QueryFragment fragment;

	/**
	 * @param errorCode
	 * @param message
	 * @param cause
	 */
	public IqlException(ErrorCode errorCode, String message, QueryFragment fragment, Throwable cause) {
		super(errorCode, message, cause);
		this.fragment = requireNonNull(fragment);
	}

	/**
	 * @param errorCode
	 * @param message
	 */
	public IqlException(ErrorCode errorCode, String message, QueryFragment fragment) {
		super(errorCode, message);
		this.fragment = requireNonNull(fragment);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public IqlException(ErrorCode errorCode, QueryFragment fragment, Throwable cause) {
		super(errorCode, cause);
		this.fragment = requireNonNull(fragment);
	}

	/**
	 * @return the fragment
	 */
	public QueryFragment getFragment() {
		return fragment;
	}
}
