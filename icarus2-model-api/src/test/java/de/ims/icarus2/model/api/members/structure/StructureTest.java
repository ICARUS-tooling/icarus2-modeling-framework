/**
 *
 */
package de.ims.icarus2.model.api.members.structure;

import de.ims.icarus2.model.api.members.container.ContainerTest;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface StructureTest<S extends Structure> extends ContainerTest<S> {

	/**
	 * @see de.ims.icarus2.model.api.members.container.ContainerTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<S> apiGuard) {
		ContainerTest.super.configureApiGuard(apiGuard);

		apiGuard.defaultReturnValue("augmented",
				Boolean.valueOf(Structure.DEFAULT_AUGMENTED));
		apiGuard.defaultReturnValue("edgesComplete",
				Boolean.valueOf(Structure.DEFAULT_EDGES_COMPLETE));
	}
}
