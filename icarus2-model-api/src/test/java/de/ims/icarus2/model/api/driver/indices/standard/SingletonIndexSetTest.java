/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.model.api.ModelTestUtils.assertModelException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class SingletonIndexSetTest implements RandomAccessIndexSetTest<SingletonIndexSet> {

	static RandomGenerator rand;

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet#SingletonIndexSet(long)}.
		 */
		@Test
		void testSingletonIndexSet() {
			assertNotNull(SingletonIndexSet.of(rand.random(0, Long.MAX_VALUE)));
		}

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.SingletonIndexSet#SingletonIndexSet(long)}.
		 */
		@ParameterizedTest
		@ValueSource(longs = {-1, Long.MIN_VALUE})
		void testSingletonIndexSetInvalidIndex(long index) {
			assertModelException(GlobalErrorCode.INVALID_INPUT,
					() -> new SingletonIndexSet(index));
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSetTest#configurations()
	 */
	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.map(type -> new Config()
						.rand(rand)
						.valueType(type)
						.label(type.name())
						.sorted(true)
						.indices(type.maxValue()-1)
						.set(config -> new SingletonIndexSet(config.getIndices()[0]))
						.features(SingletonIndexSet.features));
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonIndexSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public SingletonIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new SingletonIndexSet(rand.random(0, Long.MAX_VALUE)));
	}

}
