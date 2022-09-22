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
package de.ims.icarus2.model.api;

import java.util.Arrays;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

import org.assertj.core.api.AbstractObjectArrayAssert;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
public class IndexSetArrayAssert extends AbstractObjectArrayAssert<IndexSetArrayAssert, IndexSet> {

	public IndexSetArrayAssert(IndexSet...actual) {
		super(actual, IndexSetArrayAssert.class);
	}

	@Override
	protected IndexSetArrayAssert newObjectArrayAssert(IndexSet[] array) {
		return new IndexSetArrayAssert(array);
	}

	public IndexSetAssert element(int index) {
		hasSizeGreaterThan(index);
		return new IndexSetAssert(actual[index]);
	}

	public IndexSetAssert onlyElement() {
		hasSize(1);
		return new IndexSetAssert(actual[0]);
	}

	@SuppressWarnings("boxing")
	public IndexSetArrayAssert noneAreEmpty() {
		isNotEmpty();
		for (int i = 0; i < actual.length; i++) {
			if(actual[i].isEmpty())
				throw failure("IndexSet at index %d is empty", i);
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetArrayAssert hasCombinedSize(long expected) {
		isNotNull();
		long size = IndexUtils.count(actual);
		if(size!=expected)
			throw failureWithActualExpected(size, expected, "Expected total size of %d - git %d", expected, size);
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetArrayAssert hasCombinedSizeGreaterOrEqual(long expected) {
		isNotNull();
		long size = IndexUtils.count(actual);
		if(size<expected)
			throw failureWithActualExpected(size, expected, "Expected total size of %d or greater - git %d", expected, size);
		return myself;
	}

	public IndexSetArrayAssert containsExactlyIndices(long...indices) {
		hasCombinedSize(indices.length);
		return containsExactlyIndices(LongStream.of(indices).iterator());
	}

	public IndexSetArrayAssert containsExactlyIndices(IndexSet...indices) {
		hasCombinedSize(IndexUtils.count(indices));
		return containsExactlyIndices(IndexUtils.asIterator(indices));
	}

	@SuppressWarnings("boxing")
	public IndexSetArrayAssert containsExactlyIndices(OfLong indices) {
		isNotNull();
		OfLong itExp = indices;
		OfLong itAct = IndexUtils.asIterator(actual);

		int idx = 0;
		while(itExp.hasNext() && itAct.hasNext()) {
			long exp = itExp.nextLong();
			long act = itAct.nextLong();
			if(exp != act)
				throw failureWithActualExpected(act, exp, "Mismatch at index %d - expected %d, got %d", idx, exp, act);
			idx++;
		}

		if(itExp.hasNext())
			failure("Leftover expected values: %s", Arrays.toString(IndexUtils.asArray(itExp)));
		if(itAct.hasNext())
			failure("Leftover actual values: %s", Arrays.toString(IndexUtils.asArray(itAct)));

		return myself;
	}

	public IndexSetArrayAssert containsAllIndices(long...indices) {
		hasCombinedSizeGreaterOrEqual(indices.length);
		return containsAllIndices(LongStream.of(indices).iterator());
	}

	public IndexSetArrayAssert containsAllIndices(IndexSet...indices) {
		hasCombinedSizeGreaterOrEqual(IndexUtils.count(indices));
		return containsAllIndices(IndexUtils.asIterator(indices));
	}

	public IndexSetArrayAssert containsAllIndices(OfLong expected) {
		isNotNull();

		LongSet setExp = new LongArraySet();
		expected.forEachRemaining((LongConsumer)setExp::add);

		IndexUtils.forEachIndex(actual, (LongConsumer)setExp::remove);

		if(!setExp.isEmpty())
			failure("Leftover expected values: %s", Arrays.toString(setExp.toLongArray()));

		return myself;
	}

	//TODO
}
