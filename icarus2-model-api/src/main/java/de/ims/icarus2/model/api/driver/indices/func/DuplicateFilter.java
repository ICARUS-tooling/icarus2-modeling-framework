/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.function.LongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.util.IcarusUtils;

/**
 * Implements a stream consumer that filters out duplicates.
 * The input stream is required to provide {@code long} values
 * in ascending order! Violation of this contract will result in
 * a {@link ModelException} being thrown in the {@link #accept(long)}
 * method.
 *
 * @author Markus Gärtner
 *
 */
public class DuplicateFilter implements LongConsumer {
	private final LongConsumer consumer;
	private long lastReturnedValue = IcarusUtils.UNSET_LONG;

	public DuplicateFilter(LongConsumer consumer) {
		this.consumer = consumer;
	}

	/**
	 * Forwards the given {@code value} in case it is a new
	 * value (meaning greater than the last one returned).
	 *
	 * @see java.util.function.LongConsumer#accept(long)
	 */
	@Override
	public void accept(long value) {
		if(value>lastReturnedValue) {
			lastReturnedValue = value;
			consumer.accept(lastReturnedValue);
		} else if(value<lastReturnedValue)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Index stream is not sorted");
	}
}