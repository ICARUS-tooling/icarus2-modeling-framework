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
