/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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