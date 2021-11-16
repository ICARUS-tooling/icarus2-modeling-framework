/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine.matcher;

/**
 * @author Markus Gärtner
 *
 */
public interface Matcher<E> extends AutoCloseable {

	boolean matches(long index, E target);

	int id();

	@Override
	default void close() {
		// no-op
	}

	public static final int MATCH_ALL_ID = -2;

	public static final Matcher<Object> MATCH_ALL = new Matcher<Object>() {

		@Override
		public boolean matches(long index, Object target) { return true; }

		@Override
		public int id() { return MATCH_ALL_ID; }
	};

	@SuppressWarnings("unchecked")
	public static <E> Matcher<E> matchAll() { return (Matcher<E>) MATCH_ALL; }
}
