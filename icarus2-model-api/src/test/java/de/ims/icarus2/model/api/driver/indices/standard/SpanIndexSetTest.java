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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.RandomAccessIndexSetTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;
import de.ims.icarus2.util.collections.ArrayUtils;

/**
 * @author Markus Gärtner
 *
 */
@RandomizedTest
class SpanIndexSetTest implements RandomAccessIndexSetTest<SpanIndexSet> {

	static RandomGenerator rand;

	@Nested
	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.SpanIndexSet#SpanIndexSet(long, long)}.
		 */
		@Test
		void testSpanIndexSet() {
			assertNotNull(new SpanIndexSet(
					rand.random(0, Long.MAX_VALUE/2),
					rand.random(Long.MAX_VALUE/2, Long.MAX_VALUE)));
		}

		//TODO add guard against wrong span values in constructor
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSetTest#configurations()
	 */
	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.flatMap(type -> {
					Config base = new Config()
							.rand(rand)
							.valueType(type)
							.sorted(true)
							.defaultFeatures();

					long singleton = rand.random(type.maxValue()/2, type.maxValue());

					int size = rand.random(10, 50);
					long spanBegin = rand.random(type.maxValue()/2, type.maxValue()-size);
					long spanEnd = spanBegin+size-1;
					long[] indices = new long[size];
					ArrayUtils.fillAscending(indices, spanBegin);

					return Stream.of(
							base.clone()
								.label(type+" singleton")
								.indices(singleton)
								.set(new SpanIndexSet(singleton, singleton)),

							base.clone()
								.label(type+" span")
								.indices(indices)
								.set(new SpanIndexSet(spanBegin, spanEnd))
						);
				});
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SpanIndexSet.class;
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	public SpanIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new SpanIndexSet(
				rand.random(0, Long.MAX_VALUE/2),
				rand.random(Long.MAX_VALUE/2, Long.MAX_VALUE)));
	}

}
