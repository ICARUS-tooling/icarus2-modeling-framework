/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractParsedQueryFragment extends IqlUnique {

	@JsonProperty(IqlProperties.FRAGMENT)
	private IqlFragment fragment;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlUnique#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkNestedNotNull(fragment, "fragment");
	}

	public IqlFragment getFragment() { return fragment; }

	public void setFragment(IqlFragment fragment) { this.fragment = requireNonNull(fragment); }
}
