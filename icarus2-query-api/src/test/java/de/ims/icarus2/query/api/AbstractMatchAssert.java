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
package de.ims.icarus2.query.api;

import org.assertj.core.api.AbstractAssert;

import de.ims.icarus2.query.api.engine.result.Match;

/**
 * @author Markus Gärtner
 *
 */
public class AbstractMatchAssert<A extends AbstractMatchAssert<A,M>, M extends Match> extends AbstractAssert<A, M> {

	protected AbstractMatchAssert(M actual, Class<A> selfType) {
		super(actual, selfType);
	}

	@SuppressWarnings("boxing")
	public A hasIndex(long expected) {
		isNotNull();
		long actual = this.actual.getIndex();
		if(actual!=expected)
			throw failureWithActualExpected(actual, expected, "Expected %d as index - got %d", expected, actual);
		return myself;
	}

	@SuppressWarnings("boxing")
	public A hasMapCount(int expected) {
		isNotNull();
		long actual = this.actual.getMapCount();
		if(actual!=expected)
			throw failureWithActualExpected(actual, expected, "Expected %d as map count - got %d", expected, actual);
		return myself;
	}

	@SuppressWarnings("boxing")
	public A hasMappingAt(int index, long expectedNode, long expectedIndex) {
		isNotNull();
		if(index>=actual.getMapCount())
			throw failure("Not enough mappings for index %d, only got %d", index, actual.getMapCount());

		long actualNode = this.actual.getNode(index);
		if(actualNode!=expectedNode)
			throw failureWithActualExpected(actualNode, expectedNode, "Expected %d as node id for mapping %d - got %d", expectedNode, index, actualNode);

		long actualIndex = this.actual.getIndex(index);
		if(actualIndex!=expectedIndex)
			throw failureWithActualExpected(actualIndex, expectedIndex, "Expected %d as target index for mapping %d - got %d", expectedIndex, index, actualIndex);

		return myself;
	}

	@SuppressWarnings("boxing")
	public A hasOnlyMapping(long expectedNode, long expectedIndex) {
		isNotNull();
		if(actual.getMapCount()!=1)
			throw failure("Expected match to have exactly 1 mapping - got %d", actual.getMapCount());

		long actualNode = this.actual.getNode(0);
		if(actualNode!=expectedNode)
			throw failureWithActualExpected(actualNode, expectedNode, "Expected %d as node id - got %d", expectedNode, actualNode);

		long actualIndex = this.actual.getIndex(0);
		if(actualIndex!=expectedIndex)
			throw failureWithActualExpected(actualIndex, expectedIndex, "Expected %d as target index- got %d", expectedIndex, actualIndex);

		return myself;
	}

	//TODO
}
