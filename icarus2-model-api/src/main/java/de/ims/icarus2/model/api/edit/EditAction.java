/**
 *
 */
package de.ims.icarus2.model.api.edit;

import de.ims.icarus2.model.api.members.item.Item;

/**
 * @author Markus Gärtner
 *
 */
public interface EditAction<T extends Item> {

	Class<T> getSourceClass();

	Class<? extends Item> getElementClass();

	EditType getType();

	boolean isBatch();

	public enum EditType {
		ADD,
		REMOVE,
		MOVE,
		CHANGE_TERMINAL,
		;
	}
}
