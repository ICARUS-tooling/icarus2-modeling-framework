/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.item.Edge;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractEdgeAssert<A extends AbstractEdgeAssert<A>> extends AbstractItemAssert<A, Edge> {

	protected AbstractEdgeAssert(Edge actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public A sourceIsNull() {
		isNotNull();
		if(actual.getSource()!=null)
			throw failure("Source terminal expected to be null, but was %s", actual.getSource());
		return myself;
	}

	public A targetIsNull() {
		isNotNull();
		if(actual.getTarget()!=null)
			throw failure("Target terminal expected to be null, but was %s", actual.getTarget());
		return myself;
	}

	public A sourceIsNotNull() {
		isNotNull();
		if(actual.getSource()==null)
			throw failure("Source terminal expected not to be null");
		return myself;
	}

	public A targetIsNotNull() {
		isNotNull();
		if(actual.getTarget()==null)
			throw failure("Target terminal expected not to be null");
		return myself;
	}

	//TODO
}
