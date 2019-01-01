/**
 *
 */
package de.ims.icarus2.model.standard.members.container;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.api.members.container.Container;

/**
 * An extended default edit verifier that overrides the index validation methods
 * of {@link DefaultContainerEditVerifier} to check that supplied indices are
 * not located within the "wrapped" part of the item storage.
 *
 * @author Markus Gärtner
 *
 */
public class AugmentedContainerEditVerifier extends DefaultContainerEditVerifier {

	private AugmentedItemStorage storage;

	/**
	 * @param source
	 */
	public AugmentedContainerEditVerifier(Container source, AugmentedItemStorage storage) {
		super(source);

		this.storage = requireNonNull(storage);
	}

	@Override
	protected boolean isValidAddItemIndex(long index) {
//		System.out.printf("add: idx=%d wrapped=%b size=%d%n",index,
//				storage.isWrappedIndex(getSource(), index),
//				storage.getItemCount(getSource()));
		return index>=0
				&& !storage.isWrappedIndex(getSource(), index)
				&& index<=storage.getItemCount(getSource());
	}

	@Override
	protected boolean isValidRemoveItemIndex(long index) {
//		System.out.printf("rem: idx=%d wrapped=%b size=%d%n",index,
//				storage.isWrappedIndex(getSource(), index),
//				storage.getItemCount(getSource()));
		return index>=0
				&& !storage.isWrappedIndex(getSource(), index)
				&& index<storage.getItemCount(getSource());
	}

	/**
	 * @return the storage
	 */
	public AugmentedItemStorage getStorage() {
		return storage;
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.container.DefaultContainerEditVerifier#close()
	 */
	@Override
	public void close() {
		storage = null;
		super.close();
	}
}