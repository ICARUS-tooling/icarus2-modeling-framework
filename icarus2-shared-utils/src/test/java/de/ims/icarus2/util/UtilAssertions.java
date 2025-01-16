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
package de.ims.icarus2.util;

import org.assertj.core.api.InstanceOfAssertFactory;

import de.ims.icarus2.util.collections.BlockingLongBatchQueue;
import de.ims.icarus2.util.collections.BlockingLongBatchQueueAssert;

/**
 * @author Markus Gärtner
 *
 */
public class UtilAssertions {

	// ASSERTIONS

	public static BlockingLongBatchQueueAssert assertThat(BlockingLongBatchQueue actual) {
		return new BlockingLongBatchQueueAssert(actual);
	}

	//TODO

	// Factory helpers

	public static final InstanceOfAssertFactory<BlockingLongBatchQueue, BlockingLongBatchQueueAssert> MATCH =
			new InstanceOfAssertFactory<>(BlockingLongBatchQueue.class, UtilAssertions::assertThat);
}
