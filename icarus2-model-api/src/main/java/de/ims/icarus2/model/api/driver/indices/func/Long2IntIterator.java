/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static java.util.Objects.requireNonNull;

import java.util.PrimitiveIterator.OfInt;

/**
 * @author Markus Gärtner
 *
 */
public class Long2IntIterator implements OfInt {

	private final OfLong source;

	public Long2IntIterator(OfLong source) {
		requireNonNull(source);

		this.source = source;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	/**
	 * @see java.util.PrimitiveIterator.OfInt#nextInt()
	 */
	@Override
	public int nextInt() {
		return strictToInt(source.nextLong());
	}
}
