/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.util.collections.seq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * @author Markus Gärtner
 *
 */
class SingletonSequenceTest implements DataSequenceTest<SingletonSequence<Object>> {

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequenceTest#randomContent()
	 */
	@Override
	public Object[] randomContent() {
		return new Object[] {new Object()};
	}

	/**
	 * Test method for {@link de.ims.icarus2.util.collections.seq.SingletonSequence#SingletonSequence(java.lang.Object)}.
	 */
	@Test
	void testSingletonSequence() {
		Object item = new Object();
		SingletonSequence<Object> seq = new SingletonSequence<Object>(item);
		assertEquals(1, seq.entryCount());
		assertSame(item, seq.elementAt(0));
}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<?> getTestTargetClass() {
		return SingletonSequence.class;
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createEmpty()
	 */
	@Override
	public SingletonSequence<Object> createEmpty() {
		return new SingletonSequence<>();
	}

	/**
	 * @see de.ims.icarus2.util.collections.IterableTest#createFilled(java.lang.Object[])
	 */
	@Override
	public SingletonSequence<Object> createFilled(Object... items) {
		assertEquals(1, items.length);
		return new SingletonSequence<Object>(items[0]);
	}

}
