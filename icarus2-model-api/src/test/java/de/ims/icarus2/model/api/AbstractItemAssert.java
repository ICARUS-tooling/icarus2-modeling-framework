/**
 *
 */
package de.ims.icarus2.model.api;

import org.assertj.core.api.AbstractAssert;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractItemAssert<A extends AbstractItemAssert<A,I>, I extends Item> extends AbstractAssert<A, I> {

	protected AbstractItemAssert(I actual, Class<?> selfType) {
		super(actual, selfType);
	}

	public A containerIsNull() {
		isNotNull();
		if(actual.getContainer()!=null)
			throw failure("Container expected to be null, but was %s", actual.getContainer());
		return myself;
	}

	public A containerIsNotNull() {
		isNotNull();
		if(actual.getContainer()==null)
			throw failure("Container expected not to be null");
		return myself;
	}

	//TODO
}
