/**
 *
 */
package de.ims.icarus2.model.api;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * @author Markus Gärtner
 *
 */
public class ContainerAssert extends AbstractContainerAssert<ContainerAssert, Container> {

	public ContainerAssert(Container actual) {
		super(actual, ContainerAssert.class);
	}

	@SuppressWarnings("boxing")
	public ItemAssert element(long index) {
		isNotNull();
		if(index>=actual.getItemCount())
			throw failure("Item index %d out of bounds. Container only holds %d items.", index, actual.getItemCount());
		return new ItemAssert(actual.getItemAt(index));
	}

	//TODO
}
