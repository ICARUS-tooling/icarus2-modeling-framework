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
package de.ims.icarus2.model.api.driver.indices.standard;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import javax.annotation.concurrent.ThreadSafe;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.util.function.IntBiConsumer;
import de.ims.icarus2.util.function.IntLongConsumer;

/**
 * Wrapper to make a given {@link IndexSet} thread-safe.
 *
 * @author Markus Gärtner
 *
 */
@ThreadSafe
public class SynchronizedIndexSet implements IndexSet {


	private final IndexSet source;

	private final Set<Feature> features;

	public SynchronizedIndexSet(IndexSet source) {
		requireNonNull(source);
		this.source = source;

		Set<Feature> rawFeatures = EnumSet.copyOf(source.getFeatures());
		rawFeatures.add(Feature.THREAD_SAFE);
		features = Collections.unmodifiableSet(rawFeatures);
	}

	/**
	 * Grabs the wrapped index set's features set and adds the
	 * {@link Feature#THREAD_SAFE} flag.
	 *
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getFeatures()
	 */
	@Override
	public synchronized Set<Feature> getFeatures() {
		return features;
	}

	@Override
	public synchronized int size() {
		return source.size();
	}

	@Override
	public synchronized long indexAt(int index) {
		return source.indexAt(index);
	}

	@Override
	public synchronized long firstIndex() {
		return source.firstIndex();
	}

	@Override
	public synchronized long lastIndex() {
		return source.lastIndex();
	}

	@Override
	public synchronized IndexValueType getIndexValueType() {
		return source.getIndexValueType();
	}

	@Override
	public synchronized boolean isSorted() {
		return source.isSorted();
	}

	@Override
	public synchronized boolean sort() {
		return source.sort();
	}

	@Override
	public synchronized int export(byte[] buffer, int offset) {
		return source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, byte[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized int export(short[] buffer, int offset) {
		return source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, short[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized int export(int[] buffer, int offset) {
		return source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, int[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized int export(long[] buffer, int offset) {
		return source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, long[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized void forEachIndex(LongConsumer action) {
		source.forEachIndex(action);
	}

	@Override
	public synchronized void forEachIndex(LongConsumer action, int beginIndex,
			int endIndex) {
		source.forEachIndex(action, beginIndex, endIndex);
	}

	@Override
	public synchronized void forEachIndex(IntConsumer action) {
		source.forEachIndex(action);
	}

	@Override
	public synchronized void forEachIndex(IntConsumer action, int beginIndex,
			int endIndex) {
		source.forEachIndex(action, beginIndex, endIndex);
	}

	@Override
	public synchronized void forEachEntry(IntLongConsumer action) {
		source.forEachEntry(action);
	}

	@Override
	public synchronized void forEachEntry(IntLongConsumer action, int beginIndex,
			int endIndex) {
		source.forEachEntry(action, beginIndex, endIndex);
	}

	@Override
	public synchronized void forEachEntry(IntBiConsumer action) {
		source.forEachEntry(action);
	}

	@Override
	public synchronized void forEachEntry(IntBiConsumer action, int beginIndex,
			int endIndex) {
		source.forEachEntry(action, beginIndex, endIndex);
	}

	@Override
	public synchronized IndexSet[] split(int chunkSize) {
		return source.split(chunkSize);
	}

	@Override
	public synchronized IndexSet subSet(int fromIndex, int toIndex) {
		return source.subSet(fromIndex, toIndex);
	}

	@Override
	public synchronized IndexSet externalize() {
		return source.externalize();
	}

	@Override
	public synchronized OfLong iterator() {
		return source.iterator();
	}

	@Override
	public synchronized OfLong iterator(int start) {
		return source.iterator(start);
	}

	@Override
	public synchronized OfLong iterator(int start, int end) {
		return source.iterator(start, end);
	}

}
