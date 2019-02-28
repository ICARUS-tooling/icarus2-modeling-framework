/**
 *
 */
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.test.ApiGuardedTest;
import de.ims.icarus2.test.GenericTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.contracts.EqualsContract;
import de.ims.icarus2.test.contracts.HashContract;
import de.ims.icarus2.test.guard.ApiGuard;

/**
 * @author Markus Gärtner
 *
 */
public interface MemberTest<I extends Item> extends GenericTest<I>, ApiGuardedTest<I>,
		EqualsContract<I>, HashContract<I> {

	/**
	 * @see de.ims.icarus2.test.ApiGuardedTest#configureApiGuard(de.ims.icarus2.test.guard.ApiGuard)
	 */
	@Override
	default void configureApiGuard(ApiGuard<I> apiGuard) {
		ApiGuardedTest.super.configureApiGuard(apiGuard);

		apiGuard.detectUnmarkedMethods(true);
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default I createTestInstance(TestSettings settings) {
		return settings.process(createNoArgs());
	}

	/**
	 * This implementation return the {@code source}argument.
	 *
	 * {@inheritDoc}
	 *
	 * @see de.ims.icarus2.test.contracts.EqualsContract#createEqual(java.lang.Object)
	 */
	@Override
	default I createEqual(I source) {
		return source;
	}
}
