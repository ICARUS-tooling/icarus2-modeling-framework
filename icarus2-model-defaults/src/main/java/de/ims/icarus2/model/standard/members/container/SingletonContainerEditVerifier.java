/**
 * 
 */
package de.ims.icarus2.model.standard.members.container;

import de.ims.icarus2.model.api.members.container.Container;

/**
 *
 * @author Markus Gärtner
 *
 */
public class SingletonContainerEditVerifier extends DefaultContainerEditVerifier {

	/**
	 * @param source
	 */
	public SingletonContainerEditVerifier(Container source) {
		super(source);
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.DefaultContainerEditVerifier#isValidAddItemIndex(long)
	 */
	@Override
	protected boolean isValidAddItemIndex(long index) {
		return index==0L && getSource().getItemCount()==0L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.DefaultContainerEditVerifier#isValidRemoveItemIndex(long)
	 */
	@Override
	protected boolean isValidRemoveItemIndex(long index) {
		return index==0L && getSource().getItemCount()==1L;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.DefaultContainerEditVerifier#canMoveItem(long, long)
	 */
	@Override
	public boolean canMoveItem(long index0, long index1) {
		return false;
	}
}