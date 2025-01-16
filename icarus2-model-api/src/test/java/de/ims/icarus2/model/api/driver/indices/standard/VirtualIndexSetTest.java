/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.model.api.driver.indices.IndexSetTest;
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
class VirtualIndexSetTest implements RandomAccessIndexSetTest<VirtualIndexSet> {

	static RandomGenerator rand;

	private Stream<Config> variate(Config source) {
		List<Config> configs = new ArrayList<>();
		IndexValueType type = source.getValueType();

		long[] indices1 = IndexSetTest.sortedIndices(type, randomSize(), 0);
		configs.add(source.clone().label(type+" identity")
				.indices(indices1)
				.sorted(true)
				.set(c -> new VirtualIndexSet(indices1[0], indices1.length, type,
						(x0, i) -> x0+i, true)));

		long[] indices2 = IndexSetTest.sortedIndices(type, randomSize(), 10);
		configs.add(source.clone().label(type+" identity+10")
				.indices(indices2)
				.sorted(true)
				.set(c -> new VirtualIndexSet(indices2[0], indices2.length, type,
						(x0, i) -> x0+i, true)));

		if(type.isValidSubstitute(IndexValueType.INTEGER)) {
			long step = randomSize();
			long[] indices3 = LongStream.iterate(type.maxValue()/2, x->x+step)
					.limit(randomSize())
					.toArray();
			configs.add(source.clone().label(type+" incremental "+step)
					.indices(indices3)
					.sorted(true)
					.set(c -> new VirtualIndexSet(indices3[0], indices3.length, type,
							(x0, i) -> x0+i*step, true)));
		}

		return configs.stream();
	}

	private static int randomSize() {
		return rand.random(10, 100);
	}

	@Override
	public Stream<Config> configurations() {
		return Stream.of(IndexValueType.values())
				.map(type -> new Config()
						.valueType(type)
						.defaultFeatures()
						.rand(rand))
				.flatMap(this::variate);
	}

	@Override
	public Class<?> getTestTargetClass() {
		return VirtualIndexSet.class;
	}

	@Override
	public VirtualIndexSet createTestInstance(TestSettings settings) {
		return settings.process(new VirtualIndexSet(0, 10,
				IndexValueType.INTEGER, (x, i) -> i, true));
	}

	class Constructors {

		/**
		 * Test method for {@link de.ims.icarus2.model.api.driver.indices.standard.VirtualIndexSet#VirtualIndexSet(long, int, de.ims.icarus2.model.api.driver.indices.IndexValueType, java.util.function.LongBinaryOperator, boolean)}.
		 */
		@Test
		void testVirtualIndexSetLongIntIndexValueTypeLongBinaryOperatorBoolean() {
			fail("Not yet implemented"); // TODO
		}

	}

}
