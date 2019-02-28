/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import static de.ims.icarus2.model.api.ModelTestUtils.mockStructure;

import de.ims.icarus2.model.api.members.ItemTest;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Edge;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface EdgeTest<E extends Edge> extends ItemTest<E> {

	/**
	 * @see de.ims.icarus2.model.api.members.ItemTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<E> apiGuard) {
		ItemTest.super.configureApiGuard(apiGuard);

		apiGuard.parameterResolver(Container.class, e -> mockStructure());
	}
}
