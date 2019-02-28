/**
 *
 */
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface ItemTest<I extends Item> extends MemberTest<I> {

	/**
	 * @see de.ims.icarus2.model.api.members.MemberTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<I> apiGuard) {
		MemberTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("alive",
				Boolean.valueOf(Item.DEFAULT_ALIVE));
		apiGuard.defaultReturnValue("locked",
				Boolean.valueOf(Item.DEFAULT_LOCKED));
		apiGuard.defaultReturnValue("dirty",
				Boolean.valueOf(Item.DEFAULT_DIRTY));
	}
}
