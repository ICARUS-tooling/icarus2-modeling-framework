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
 */
package de.ims.icarus2.util;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Markus Gärtner
 *
 */
public class BitFlags {
	private final AtomicInteger flagPower;
	private final int limit;

	BitFlags(int startPow, int limit) {
		checkArgument("Start power negative", startPow>=0);
		checkArgument("Limit must be greater than start power", limit>startPow);
		checkArgument("Limit conditions: -1<limit<64", limit<64);

		flagPower = new AtomicInteger(startPow);
		this.limit = limit;
	}

	public int newIntFlag() {
		checkState("Designed to create long flags", limit<32);
		int exp = flagPower.getAndIncrement();
		checkState("Value space for flags exhausted", exp<=limit);
		return (1<<exp);
	}

	public long newLongFlag() {
		int exp = flagPower.getAndIncrement();
		checkState("Value space for flags exhausted", exp<=limit);
		return (1L<<exp);
	}
}
