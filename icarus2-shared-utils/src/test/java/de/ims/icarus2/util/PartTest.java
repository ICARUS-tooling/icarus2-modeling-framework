/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface PartTest<O, P extends Part<O>> {

	/**
	 * Create a suitable environment for the part under test.
	 * It is preferably if this environment is a mock, but it
	 * can also be a live instance.
	 * <p>
	 * Consecutive invocations of this method must yield different
	 * instances!
	 */
	@Provider
	O createEnvironment();

	/**
	 * Create the {@link Part} under test.
	 */
	@Provider
	P createPart();

	@Test
	default void testPartConsistency() {
		P part = createPart();
		O environment = createEnvironment();

		assertFalse(part.isAdded());
		part.addNotify(environment);
		assertTrue(part.isAdded());
		part.removeNotify(environment);
		assertFalse(part.isAdded());
	}

	@Test
	default void testRpeatedAdd() {
		P part = createPart();
		O environment = createEnvironment();

		part.addNotify(environment);

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> part.addNotify(environment));
	}

	@Test
	default void testPrematureRemove() {
		P part = createPart();

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> part.removeNotify(createEnvironment()));
	}

	@Test
	default void testForeignRemove() {
		P part = createPart();

		O env1 = createEnvironment();
		O env2 = createEnvironment();
		assertNotSame(env1, env2);

		part.addNotify(env1);

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> part.addNotify(env2));
	}
}
