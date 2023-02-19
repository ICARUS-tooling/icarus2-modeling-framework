/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.test.asserter;

/**
 * @author Markus Gärtner
 *
 */
public abstract class Asserter<A extends Asserter<A>> {

	/**
	 * Prevents public constructor from leaking
	 */
	protected Asserter() {
		// no-op
	}

	@SuppressWarnings("unchecked")
	protected final A thisAsCast() {
		return (A) this;
	}

	/**
	 *
	 */
	public abstract void test();
}
