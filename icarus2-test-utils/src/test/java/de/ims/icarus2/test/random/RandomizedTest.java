/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.test.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
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

	@Nested
	class ForFieldInjectionWithSeedViaProperty {
		private static final long seed = 123456789;

		@BeforeEach
		void setUp() {
			System.setProperty(Randomized.SEED_PROPERTY, String.valueOf(seed));
		}

		@Test
		void verifyFieldInjections(RandomGenerator rs) {
			assertEquals(seed, rs.getSeed());
		}
	}
}
