/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.Testable;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface PartTest<O, P extends Part<O>> extends Testable<P> {

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
	 * Create a new part instance that has not yet been added to
	 * an environment.
	 * @return
	 */
	@Provider
	default P createUnadded() {
		return create();
	}

	@Test
	default void testPartConsistency() {
		P part = createUnadded();
		O environment = createEnvironment();

		assertFalse(part.isAdded());
		part.addNotify(environment);
		assertTrue(part.isAdded());
		part.removeNotify(environment);
		assertFalse(part.isAdded());
	}

	public static void assertAddRemoveError(Executable executable) {
		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE, executable);
	}

	@Test
	default void testRepeatedAdd() {
		P part = createUnadded();
		O environment = createEnvironment();

		part.addNotify(environment);

		assertAddRemoveError(() -> part.addNotify(environment));
	}

	@Test
	default void testPrematureRemove() {
		P part = createUnadded();

		assertAddRemoveError(() -> part.removeNotify(createEnvironment()));
	}

	@Test
	default void testForeignRemove() {
		P part = createUnadded();

		O env1 = createEnvironment();
		O env2 = createEnvironment();
		assertNotSame(env1, env2);

		part.addNotify(env1);

		assertAddRemoveError(() -> part.addNotify(env2));
	}
}
