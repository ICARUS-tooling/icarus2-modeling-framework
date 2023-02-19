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

import de.ims.icarus2.test.util.Triple;

/**
 * @author Markus Gärtner
 *
 */
public class TripleAssert<FIRST, SECOND, THIRD> extends AbstractAssert<TripleAssert<FIRST, SECOND, THIRD>, Triple<FIRST, SECOND, THIRD>> {

	public TripleAssert(Triple<FIRST, SECOND, THIRD> actual) {
		super(actual, TripleAssert.class);
	}

	public void isEmpty() {
		firstIsNull();
		secondIsNull();
		thirdIsNull();
	}

	// FIRST

	public TripleAssert<FIRST, SECOND, THIRD> firstIsNull() {
		isNotNull();
		if(actual.first!=null)
			throw failure("Expected first to be null, but was %s", actual.first);
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> firstIsNotNull() {
		isNotNull();
		if(actual.first==null)
			throw failure("Expected first not to be null");
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> firstIsEqualTo(FIRST expected) {
		firstIsNotNull();
		if(!actual.first.equals(expected))
			throw failureWithActualExpected(actual.first, expected, "Expected first to be %s, but got %s", expected, actual.first);
		return myself;
	}

	// SECOND

	public TripleAssert<FIRST, SECOND, THIRD> secondIsNull() {
		isNotNull();
		if(actual.second!=null)
			throw failure("Expected second to be null, but was %s", actual.second);
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> secondIsNotNull() {
		isNotNull();
		if(actual.second==null)
			throw failure("Expected second not to be null");
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> secondIsEqualTo(SECOND expected) {
		secondIsNotNull();
		if(!actual.second.equals(expected))
			throw failureWithActualExpected(actual.second, expected, "Expected second to be %s, but got %s", expected, actual.second);
		return myself;
	}

	// THIRD

	public TripleAssert<FIRST, SECOND, THIRD> thirdIsNull() {
		isNotNull();
		if(actual.third!=null)
			throw failure("Expected third to be null, but was %s", actual.third);
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> thirdIsNotNull() {
		isNotNull();
		if(actual.third==null)
			throw failure("Expected third not to be null");
		return myself;
	}

	public TripleAssert<FIRST, SECOND, THIRD> thirdIsEqualTo(THIRD expected) {
		thirdIsNotNull();
		if(!actual.third.equals(expected))
			throw failureWithActualExpected(actual.third, expected, "Expected third to be %s, but got %s", expected, actual.third);
		return myself;
	}
}
