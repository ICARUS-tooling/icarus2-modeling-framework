/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.collections;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;

/**
 * @author Markus Gärtner
 *
 */
public class BlockingLongBatchQueueAssert extends AbstractAssert<BlockingLongBatchQueueAssert, BlockingLongBatchQueue> {

	public BlockingLongBatchQueueAssert(BlockingLongBatchQueue actual) {
		super(actual, BlockingLongBatchQueueAssert.class);
	}

	@SuppressWarnings("boxing")
	public BlockingLongBatchQueueAssert hasCount(int expected) {
		isNotNull();
		if(actual.count!=expected)
			throw failureWithActualExpected(actual.count, expected, "Expected count of %d - got %d", expected, actual.count);
		return myself;
	}

	@SuppressWarnings("boxing")
	public BlockingLongBatchQueueAssert hasPutIndex(int expected) {
		isNotNull();
		if(actual.putIndex!=expected)
			throw failureWithActualExpected(actual.putIndex, expected, "Expected putIndex of %d - got %d", expected, actual.putIndex);
		return myself;
	}

	@SuppressWarnings("boxing")
	public BlockingLongBatchQueueAssert hasTakeIndex(int expected) {
		isNotNull();
		if(actual.takeIndex!=expected)
			throw failureWithActualExpected(actual.takeIndex, expected, "Expected takeIndex of %d - got %d", expected, actual.takeIndex);
		return myself;
	}

	public BlockingLongBatchQueueAssert hasItems(long...expected) {
		isNotNull();
		assertThat(actual.items).as("items").containsExactly(expected);
		return myself;
	}

	public BlockingLongBatchQueueAssert itemsStartWith(long...expected) {
		isNotNull();
		assertThat(actual.items).as("items").startsWith(expected);
		return myself;
	}
}
