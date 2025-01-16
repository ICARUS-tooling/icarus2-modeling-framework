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
/**
 *
 */
package de.ims.icarus2.test.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.assertj.core.api.AbstractIteratorAssert;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
public class LongIteratorAssert extends AbstractIteratorAssert<LongIteratorAssert, Long> {

	public LongIteratorAssert(Iterator<? extends Long> actual) {
		super(actual, LongIteratorAssert.class);
	}

	@SuppressWarnings("boxing")
	private long[] readTail(int n) {
		isNotNull();
		long[] buffer = new long[n];
		int idx = 0;
		while(n>0) {
			hasNext();
			buffer[idx++] = actual.next();
		}
		return buffer;
	}

	private long[] readTail() {
		LongList buffer = new LongArrayList();
		actual.forEachRemaining(v -> buffer.add(v.longValue()));
		return buffer.toLongArray();
	}

	/** Verifies that the iterator contains only the given values in exactly that order. */
	public LongIteratorAssert remainingHasExactly(long...expected) {
		long[] content = readTail(expected.length);
		isExhausted();
		assertThat(content).containsExactly(expected);
		return myself;
	}

	/** Verifies that the iterator contains only the given values in any order. */
	public LongIteratorAssert remainingHasOnly(long...expected) {
		long[] content = readTail(expected.length);
		isExhausted();
		assertThat(content).contains(expected);
		return myself;
	}

	/** Verifies that the iterator contains at least the given values in any order. */
	public LongIteratorAssert remainingContains(long...expected) {
		long[] content = readTail(expected.length);
		assertThat(content).contains(expected);
		return myself;
	}

	public LongIteratorAssert hasSameIndices(Iterator<? extends Long> expected) {
		isNotNull();
		while(expected.hasNext()) {
			hasNext();
			assertThat(actual.next()).isEqualTo(expected.next());
		}
		isExhausted();
		return myself;
	}
}
