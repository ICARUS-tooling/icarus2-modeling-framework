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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class MatchBuffer implements MatchSource, MatchSink {

	private long index = UNSET_LONG;
	private int[] m_node;
	private int[] m_index;
	private int size = 0;

	public MatchBuffer(int initialSize) {
		m_node = new int[initialSize];
		m_index = new int[initialSize];
	}

	/** Reset size back to {@code 0}. */
	public void clear() { size = 0; }
	/** Ensure we have sufficient capacity to store {@code size} mappings and reset cursor to {@code 0}. */
	public void prepare(int size) {
		if(size>m_node.length) {
			int newSize = CollectionUtils.growSize(m_node.length, size);
			m_node = new int[newSize];
			m_index = new int[newSize];
		}
		this.size = 0;
	}
	public void map(int mappingId, int index) {
		m_node[size] = mappingId;
		m_index[size] = index;
		size++;
	}
	public void map(int offset, int size, int[] m_node, int[] m_index) {
		prepare(size);
		System.arraycopy(m_node, offset, this.m_node, 0, size);
		System.arraycopy(m_index, offset, this.m_index, 0, size);
		this.size = size;
	}

	/** Get the global index of the container this match refers to. */
	public long getIndex() { return index; }
	/** Returns the number of mappings inside this match */
	public int getMapCount() { return size; }
	/** Fetch the node id for the mapping at given index */
	public int getNode(int index) { return m_node[index]; }
	/** Fetch the positional index for the mapping at given index */
	public int getIndex(int index) { return m_index[index]; }

	@Override
	public void consume(long index, int offset, int size, int[] m_node, int[] m_index) {
		this.index = index;
		map(offset, size, m_node, m_index);
	}

	@Override
	public Match toMatch() {
		return DefaultMatch.of(index, size, m_node, m_index);
	}

	@Override
	public void drainTo(MatchSink sink) {
		sink.consume(index, 0, size, m_node, m_index);
	}

}
