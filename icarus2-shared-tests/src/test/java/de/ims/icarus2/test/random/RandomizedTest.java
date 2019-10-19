/**
 *
 */
package de.ims.icarus2.test.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ims.icarus2.test.annotations.Seed;

/**
 * @author Markus Gärtner
 *
 */
@ExtendWith(Randomized.class)
class RandomizedTest {

	static RandomGenerator sharedRand;

	@Test
	@ExtendWith(Randomized.class)
	void resolveParameter(RandomGenerator rs) {
		assertNotNull(rs);
	}

	@Test
	@ExtendWith(Randomized.class)
	void staticRand() {
		assertNotNull(sharedRand);
	}

	@Test
	@ExtendWith(Randomized.class)
	void resolveParameterWithLongSeed(@Seed(123456789) RandomGenerator rs) {
		assertEquals(123456789, rs.getSeed());
	}

	@Test
	@ExtendWith(Randomized.class)
	void resolveParameterWithStringSeed(
			@Seed(seedSource = "de.ims.icarus2.test.random.Randomized") RandomGenerator rs) {
		assertEquals(Randomized.TEST_SEED, rs.getSeed());
	}

	@Test
	@ExtendWith(Randomized.class)
	void resolveParameterWithDuplicateSeed(
			@Seed(value = 123456789, seedSource = "de.ims.icarus2.test.random.Randomized") RandomGenerator rs) {
		assertEquals(123456789, rs.getSeed());
	}

	@Test
	@ExtendWith(Randomized.class)
	@Seed(987654321)
	void resolveParameterWithConcurrentLongSeed(@Seed(123456789) RandomGenerator rs) {
		assertEquals(123456789, rs.getSeed());
	}

	@Test
	@ExtendWith(Randomized.class)
	@Seed(123456789)
	void resolveParameterWithLongSeedOnMethod(RandomGenerator rs) {
		assertEquals(123456789, rs.getSeed());
	}

	@Test
	@ExtendWith(Randomized.class)
	@Seed(seedSource = "de.ims.icarus2.test.random.Randomized")
	void resolveParameterWithStringSeedOnMethod(RandomGenerator rs) {
		assertEquals(Randomized.TEST_SEED, rs.getSeed());
	}

	@Nested
	@ExtendWith(Randomized.class)
	class ForFieldInjection {
		RandomGenerator rs;

		@Test
		void verifyFieldInjections() {
			assertNotNull(rs);
		}
	}

	@Nested
	@ExtendWith(Randomized.class)
	class ForFieldInjectionWithSeedOnField {
		@Seed(123456789)
		RandomGenerator rs;

		@Test
		void verifyFieldInjections() {
			assertEquals(123456789, rs.getSeed());
		}
	}

	@Nested
	@ExtendWith(Randomized.class)
	@Seed(123456789)
	class ForFieldInjectionWithSeedOnClass {
		RandomGenerator rs;

		@Test
		void verifyFieldInjections() {
			assertEquals(123456789, rs.getSeed());
		}
	}
}
