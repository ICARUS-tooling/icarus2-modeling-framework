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
package de.ims.icarus2.model.api.driver.indices.standard;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexUtils;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * @author Markus Gärtner
 *
 */
public class SingletonIndexSet implements IndexSet {

	/** Package private for access by test code */
	static final Set<Feature> features;
	static {
		Set<Feature> set = EnumSet.copyOf(IndexSet.DEFAULT_FEATURES);
		set.add(Feature.THREAD_SAFE);
		features = Collections.unmodifiableSet(set);
	}

	public static SingletonIndexSet of(long index) {
		return new SingletonIndexSet(index);
	}

	private final long index;

	public SingletonIndexSet(long index) {
		if(index<0)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "Negative index: "+index);

		this.index = index;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return IndexUtils.toString(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return 1;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		if(index!=0)
			throw new IndexOutOfBoundsException();

		return this.index;
	}

	/**
	 * This implementation wraps itself into a new array of size {@code 1} and returns
	 * that array.
	 *
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#split(int)
	 */
	@Override
	public IndexSet[] split(int chunkSize) {
		return IndexUtils.wrap(this);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return index;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return index;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		if(fromIndex!=0 || toIndex!=0)
			throw new IllegalArgumentException();

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return IndexValueType.forValue(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return true;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getFeatures()
	 */
	@Override
	public Set<Feature> getFeatures() {
		return features;
	}
}
