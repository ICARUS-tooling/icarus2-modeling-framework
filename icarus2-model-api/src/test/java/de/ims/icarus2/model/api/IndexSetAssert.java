/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static de.ims.icarus2.util.lang.Primitives.strictToByte;
import static de.ims.icarus2.util.lang.Primitives.strictToInt;
import static de.ims.icarus2.util.lang.Primitives.strictToShort;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.function.LongConsumer;

import org.assertj.core.api.AbstractAssert;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexSet.Feature;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.test.assertions.LongIteratorAssert;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * @author Markus Gärtner
 *
 */
public class IndexSetAssert extends AbstractAssert<IndexSetAssert, IndexSet> {

	public IndexSetAssert(IndexSet actual) {
		super(actual, IndexSetAssert.class);
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert isEmpty() {
		isNotNull();
		if(actual.size()>0)
			throw failure("Expected actual to be empty, but size was %d", actual.size());
		return myself;
	}

	public IndexSetAssert isNotEmpty() {
		isNotNull();
		if(actual.size()==0)
			throw failure("Expected actual not to be empty");
		return myself;
	}

	public IndexSetAssert isSorted() {
		isNotNull();
		if(!actual.isSorted())
			throw failure("Expected actual to be sorted");
		return myself;
	}

	public IndexSetAssert isNotSorted() {
		isNotNull();
		if(actual.isSorted())
			throw failure("Expected actual not to be sorted");
		return myself;
	}

	public IndexSetAssert hasValueType(IndexValueType valueType) {
		isNotNull();
		assertThat(actual.getIndexValueType()).as("index value type").isSameAs(valueType);
		return myself;
	}

	public IndexSetAssert hasSameValueType(IndexSet other) {
		isNotNull();
		assertThat(actual.getIndexValueType()).as("index value type").isSameAs(other.getIndexValueType());
		return myself;
	}

	public IndexSetAssert hasFeature(Feature feature) {
		isNotNull();
		requireNonNull(feature);
		if(!actual.hasFeature(feature))
			throw failure("Feature not set: %s", feature);
		return myself;
	}

	public IndexSetAssert hasExacltyFeatures(Feature...features) {
		isNotNull();
		assertThat(actual.getFeatures()).containsOnly(features);
		return myself;
	}

	public IndexSetAssert hasExacltyFeatures(Set<Feature> features) {
		isNotNull();
		assertThat(actual.getFeatures()).hasSameElementsAs(features);
		return myself;
	}

	public IndexSetAssert hasAllFeatures(Feature...features) {
		isNotNull();
		assertThat(actual.getFeatures()).contains(features);
		return myself;
	}

	public IndexSetAssert hasAllFeatures(Set<Feature> features) {
		isNotNull();
		assertThat(actual.getFeatures()).containsAll(features);
		return myself;
	}

	public IndexSetAssert hasAnyFeature(Feature...features) {
		isNotNull();
		assertThat(actual.getFeatures()).containsAnyOf(features);
		return myself;
	}

	public IndexSetAssert hasAnyFeature(Set<Feature> features) {
		isNotNull();
		assertThat(actual.getFeatures()).containsAnyElementsOf(features);
		return myself;
	}

	public IndexSetAssert hasNoFeatures(Feature...features) {
		isNotNull();
		assertThat(actual.getFeatures()).doesNotContain(features);
		return myself;
	}

	public IndexSetAssert hasNoFeatures(Set<Feature> features) {
		isNotNull();
		assertThat(actual.getFeatures()).doesNotContainAnyElementsOf(features);
		return myself;
	}

	public IndexSetAssert hasNotFeature(Feature feature) {
		isNotNull();
		requireNonNull(feature);
		if(actual.hasFeature(feature))
			throw failure("Feature not expected to be set: %s", feature);
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert hasUndefinedSize() {
		isNotNull();
		if(actual.size()!=UNSET_INT)
			throw failure("Expected size to be undefined, but was %d", actual.size());
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert hasSize(int expected) {
		isNotNull();
		if(actual.size()!=expected)
			throw failureWithActualExpected(actual.size(), expected,
					"Size expected to be %d, but was %d", expected, actual.size());
		return myself;
	}

	public IndexSetAssert hasSameSizeAs(IndexSet other) {
		return hasSize(other.size());
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert hasSameIndicesAs(IndexSet other) {
		hasSameSizeAs(other);
		for (int i = 0; i < actual.size(); i++) {
			if(actual.indexAt(i)!=other.indexAt(i))
				throw failureWithActualExpected(actual.indexAt(i), other.indexAt(i),
						"Mismatch at index %d: expected %d, but got %d", i, other.indexAt(i), actual.indexAt(i));
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert containsExactlyIndices(long...indices) {
		hasSize(indices.length);
		for (int i = 0; i < indices.length; i++) {
			if(actual.indexAt(i)!=indices[i])
				throw failureWithActualExpected(actual.indexAt(i), indices[i],
						"Mismatch at index %d: expected %d, but got %d", i, indices[i], actual.indexAt(i));
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert containsExactlyIndices(int...indices) {
		hasSize(indices.length);
		for (int i = 0; i < indices.length; i++) {
			if(strictToInt(actual.indexAt(i))!=indices[i])
				throw failureWithActualExpected(actual.indexAt(i), indices[i],
						"Mismatch at index %d: expected %d, but got %d", i, indices[i], actual.indexAt(i));
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert containsExactlyIndices(short...indices) {
		hasSize(indices.length);
		for (int i = 0; i < indices.length; i++) {
			if(strictToShort(actual.indexAt(i))!=indices[i])
				throw failureWithActualExpected(actual.indexAt(i), indices[i],
						"Mismatch at index %d: expected %d, but got %d", i, indices[i], actual.indexAt(i));
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert containsExactlyIndices(byte...indices) {
		hasSize(indices.length);
		for (int i = 0; i < indices.length; i++) {
			if(strictToByte(actual.indexAt(i))!=indices[i])
				throw failureWithActualExpected(actual.indexAt(i), indices[i],
						"Mismatch at index %d: expected %d, but got %d", i, indices[i], actual.indexAt(i));
		}
		return myself;
	}

	@SuppressWarnings("boxing")
	public IndexSetAssert containsExactlyIndices(OfLong indices) {
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

	public IndexSetAssert containsAllIndices(OfLong expected) {
		isNotNull();

		LongSet setExp = new LongArraySet();
		expected.forEachRemaining((LongConsumer)setExp::add);

		actual.forEachIndex((LongConsumer)setExp::remove);

		if(!setExp.isEmpty())
			failure("Leftover expected values: %s", Arrays.toString(setExp.toLongArray()));

		return myself;
	}

	public LongIteratorAssert asIterator() {
		isNotNull();
		return new LongIteratorAssert(actual.iterator());
	}

	//TODO
}
