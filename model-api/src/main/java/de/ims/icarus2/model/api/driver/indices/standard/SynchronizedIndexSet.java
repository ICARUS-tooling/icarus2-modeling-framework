/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 */
package de.ims.icarus2.model.api.driver.indices.standard;

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;

import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;

/**
 * Wrapper to make a given {@link IndexSet} thread-safe.
 *
 * @author Markus Gärtner
 *
 */
public class SynchronizedIndexSet implements IndexSet {

	private final IndexSet source;

	public SynchronizedIndexSet(IndexSet source) {
		checkNotNull(source);
		this.source = source;
	}

	/**
	 * Grabs the wrapped index set's features set and adds the
	 * {@link IndexSet#FEATURE_THREAD_SAFE} flag.
	 *
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getFeatures()
	 */
	@Override
	public synchronized int getFeatures() {
		return source.getFeatures() |  FEATURE_THREAD_SAFE;
	}

	@Override
	public synchronized int size() {
		return source.size();
	}

	@Override
	public long indexAt(int index) {
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
	public synchronized void export(byte[] buffer, int offset) {
		source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, byte[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized void export(short[] buffer, int offset) {
		source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, short[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized void export(int[] buffer, int offset) {
		source.export(buffer, offset);
	}

	@Override
	public synchronized void export(int beginIndex, int endIndex, int[] buffer,
			int offset) {
		source.export(beginIndex, endIndex, buffer, offset);
	}

	@Override
	public synchronized void export(long[] buffer, int offset) {
		source.export(buffer, offset);
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
	public synchronized void forEachEntry(LongBinaryOperator action) {
		source.forEachEntry(action);
	}

	@Override
	public synchronized void forEachEntry(LongBinaryOperator action, int beginIndex,
			int endIndex) {
		source.forEachEntry(action, beginIndex, endIndex);
	}

	@Override
	public synchronized void forEachEntry(IntBinaryOperator action) {
		source.forEachEntry(action);
	}

	@Override
	public synchronized void forEachEntry(IntBinaryOperator action, int beginIndex,
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
