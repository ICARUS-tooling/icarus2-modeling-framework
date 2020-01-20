/**
 *
 */
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlFragment extends AbstractIqlQueryElement {

	@JsonProperty(IqlProperties.START)
	private int start;
	@JsonProperty(IqlProperties.START)
	private int stop;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.FRAGMENT;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(start>=0, "start", "start must not be negative");
		checkCondition(stop>=start, "stop", "stop must be equal or greater than start");
	}

	public int getStart() { return start; }

	public int getStop() { return stop; }

	public void setStart(int start) { this.start = start; }

	public void setStop(int stop) { this.stop = stop; }
}
