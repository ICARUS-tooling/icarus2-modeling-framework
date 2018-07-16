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

import static java.util.Objects.requireNonNull;

import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class Long2IntConverter implements LongConsumer {

	private final IntConsumer target;

	public Long2IntConverter(IntConsumer target) {
		requireNonNull(target);

		this.target = target;
	}

	/**
	 * @see java.util.function.LongConsumer#accept(long)
	 */
	@Override
	public void accept(long value) {
		target.accept(IcarusUtils.ensureIntegerValueRange(value));
	}
}
