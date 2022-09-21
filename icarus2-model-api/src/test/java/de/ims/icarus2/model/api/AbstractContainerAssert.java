/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractContainerAssert<A extends AbstractContainerAssert<A, C>, C extends Container> extends AbstractItemAssert<A,C> {

	protected AbstractContainerAssert(C actual, Class<?> selfType) {
		super(actual, selfType);
	}

	//TODO
}
