/**
 *
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.SharedTestUtils.assertIcarusException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.test.annotations.Provider;

/**
 * @author Markus Gärtner
 *
 */
public interface PartTest<O, P extends Part<O>> {

	@Provider
	O createEnvironment();

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
				() -> part.addNotify(createEnvironment()));
	}

	@Test
	default void testForeignRemove() {
		P part = createPart();

		O env1 = createEnvironment();
		O env2 = createEnvironment();
		assumeTrue(env1!=env2);

		part.addNotify(env1);

		assertIcarusException(GlobalErrorCode.ILLEGAL_STATE,
				() -> part.addNotify(env2));
	}
}