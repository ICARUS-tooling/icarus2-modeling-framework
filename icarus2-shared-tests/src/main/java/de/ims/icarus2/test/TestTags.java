/**
 *
 */
package de.ims.icarus2.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Markus Gärtner
 *
 */
public final class TestTags {

	/**
	 * Marks tests that could potentially take a very long time.
	 */
	public static final String SLOW = "slow";

	/**
	 * Marks tests that rely on randomly generated input
	 */
	public static final String RANDOMIZED = "ranodmized";


	/**
	 * Marks tests that intend not to participate in the regular
	 * test lifecycle. The associated {@link BeforeEach} and
	 * {@link AfterEach} methods are expected to honor this and
	 * to ignore the marked tests for initialization or cleanup.
	 */
	public static final String STANDALONE = "standalone";
}
