/**
 *
 */
package de.ims.icarus2.model.api.edit.change;

import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 * @author Markus Gärtner
 *
 */
public interface AtomicBulkChange<E extends Item, C extends Container> extends AtomicChange {


	/**
	 * Returns whether or not this change <b>originally</b> was an add.
	 * @return
	 */
	boolean isAdd();

	DataSequence<? extends E> getItems();

	C getContainer();

	long getBeginIndex();

	long getEndIndex();
}
