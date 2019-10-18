/**
 *
 */
package de.ims.icarus2.test.random;

import java.util.Random;

/**
 * @author Markus Gärtner
 *
 */
public class RandomSource {

	private final Random random;
	private final long seed;

	public RandomSource(long seed) {
		this.seed = seed;
		random = new Random(seed);
	}

	public Random random() {
		return random;
	}

	public long getSeed() {
		return seed;
	}

	public void reset() {
		random.setSeed(seed);
	}
}
