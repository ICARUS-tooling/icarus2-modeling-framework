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
/**
 *
 */
package de.ims.icarus2.test.assertions;

import org.assertj.core.api.AbstractAssert;

import de.ims.icarus2.test.util.Pair;

/**
 * @author Markus Gärtner
 *
 */
public class PairAssert<FIRST, SECOND> extends AbstractAssert<PairAssert<FIRST, SECOND>, Pair<FIRST, SECOND>> {

	public PairAssert(Pair<FIRST, SECOND> actual) {
		super(actual, PairAssert.class);
	}

	public void isEmpty() {
		firstIsNull();
		secondIsNull();
	}

	// FIRST

	public PairAssert<FIRST, SECOND> firstIsNull() {
		isNotNull();
		if(actual.first!=null)
			throw failure("Expected first to be null, but was %s", actual.first);
		return myself;
	}

	public PairAssert<FIRST, SECOND> firstIsNotNull() {
		isNotNull();
		if(actual.first==null)
			throw failure("Expected first not to be null");
		return myself;
	}

	public PairAssert<FIRST, SECOND> hasFirst(FIRST expected) {
		firstIsNotNull();
		if(!actual.first.equals(expected))
			throw failureWithActualExpected(actual.first, expected, "Expected first to be %s, but got %s", expected, actual.first);
		return myself;
	}

	// SECOND

	public PairAssert<FIRST, SECOND> secondIsNull() {
		isNotNull();
		if(actual.second!=null)
			throw failure("Expected second to be null, but was %s", actual.second);
		return myself;
	}

	public PairAssert<FIRST, SECOND> secondIsNotNull() {
		isNotNull();
		if(actual.second==null)
			throw failure("Expected second not to be null");
		return myself;
	}

	public PairAssert<FIRST, SECOND> hasSecond(SECOND expected) {
		secondIsNotNull();
		if(!actual.second.equals(expected))
			throw failureWithActualExpected(actual.second, expected, "Expected second to be %s, but got %s", expected, actual.second);
		return myself;
	}
}
