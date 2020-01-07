/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections.seq;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import de.ims.icarus2.test.annotations.RandomizedTest;
import de.ims.icarus2.test.random.RandomGenerator;

/**
 * @author Markus Gärtner
 *
 */
class ArraySequenceTest implements DataSequenceTest<ArraySequence<Object>> {

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.ArraySequence#ArraySequence(E[])}.
	 */
	@Test
	@RandomizedTest
	void testArraySequence(RandomGenerator rand) {
		Object[] items = randomContent(rand);
		ArraySequence<Object> seq = new ArraySequence<>(items);
		for (int i = 0; i < items.length; i++) {
			assertSame(items[i], seq.elementAt(i));
		}
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return ArraySequence.class;
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createEmpty()
	 */
	@Override
	public ArraySequence<Object> createEmpty() {
		return new ArraySequence<>(new Object[0]);
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createFilled(java.lang.Object[])
	 */
	@Override
	public ArraySequence<Object> createFilled(Object... items) {
		return new ArraySequence<>(items);
	}
}
