/*
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

import java.util.function.BiConsumer;

/**
 * Primitive specialization of {@link BiConsumer} for {@code long} values.
 *
 * @author Markus Gärtner
 *
 */
@FunctionalInterface
public interface LongBiConsumer extends BiConsumer<Long, Long> {

	void accept(long v1, long v2);

	/**
	 * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
	 */
	@Override
	default void accept(Long t, Long u) {
		accept(t.longValue(), u.longValue());
	}
}
