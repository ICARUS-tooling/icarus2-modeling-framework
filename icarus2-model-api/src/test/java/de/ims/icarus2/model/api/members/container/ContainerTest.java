/**
 *
 */
package de.ims.icarus2.model.api.members.container;

import de.ims.icarus2.model.api.members.ItemTest;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface ContainerTest<C extends Container> extends ItemTest<C> {

	/**
	 * @see de.ims.icarus2.model.api.members.MemberTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<C> apiGuard) {
		ItemTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("itemsComplete",
				Boolean.valueOf(Container.DEFAULT_ITEMS_COMPLETE));
	}

}
