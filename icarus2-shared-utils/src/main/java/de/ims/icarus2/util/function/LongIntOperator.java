/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.function;

import java.util.function.BiFunction;

/**
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface LongIntOperator extends BiFunction<Long, Integer, Long> {

	long applyAsLong(long v0, int v1);

	/**
	 * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
	 */
	@Override
	default Long apply(Long t, Integer u) {
		return Long.valueOf(applyAsLong(t.longValue(), u.intValue()));
	}
}
