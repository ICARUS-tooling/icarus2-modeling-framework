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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.Arrays;

/**
 * @author Markus Gärtner
 *
 */
public final class MatchImpl implements Match {

	private static final int[] EMPTY = {};

	public static MatchImpl empty(int lane, long index) {
		return new MatchImpl(lane, index, EMPTY, EMPTY);
	}

	/** Create a new [{@link Match}, using the given data without copying */
	public static MatchImpl of(int lane, long index, int[] m_node, int[] m_index) {
		return new MatchImpl(lane, index, m_node, m_index);
	}

	/** Create a new [{@link Match}, using the given data, while copying the relevant parts */
	public static MatchImpl of(int lane, long index, int size, int[] m_node, int[] m_index) {
		return new MatchImpl(lane, index, Arrays.copyOf(m_node, size), Arrays.copyOf(m_index, size));
	}

	/** Create a new [{@link Match}, using the given data, while copying the relevant sections */
	public static MatchImpl of(int lane, long index, int offset, int size, int[] m_node, int[] m_index) {
		return new MatchImpl(lane, index, Arrays.copyOfRange(m_node, offset, offset+size),
				Arrays.copyOfRange(m_index, offset, offset+size));
	}

	private final int lane;
	private final long index;
	private final int[] m_node;
	private final int[] m_index;

	private int hash = UNSET_INT;

	private MatchImpl(int lane, long index, int[] m_node, int[] m_index) {
		this.lane = lane;
		this.index = index;
		this.m_node = m_node;
		this.m_index = m_index;
	}

	/** Get the global index of the container this match refers to. */
	@Override
	public long getIndex() { return index; }
	/** Returns the number of mappings inside this match */
	@Override
	public int getMapCount() { return m_node.length; }
	/** Fetch the node id for the mapping at given index */
	@Override
	public int getNode(int index) { return m_node[index]; }
	/** Fetch the positional index for the mapping at given index */
	@Override
	public int getIndex(int index) { return m_index[index]; }

	@Override
	public void drainTo(MatchSink sink) {
		sink.consume(lane, index, 0, m_node.length, m_node, m_index);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof MatchImpl) {
			MatchImpl other = (MatchImpl) obj;
			return lane==other.lane
					&& index==other.index
					&& Arrays.equals(m_node, other.m_node)
					&& Arrays.equals(m_index, other.m_index);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(hash==UNSET_INT) {
			hash = (lane+1) * (int) (index+1) * Arrays.hashCode(m_node) * Arrays.hashCode(m_index);
		}
		return hash;
	}

	/**
	 * {lane index: m_node->m_index, }
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Match.toString(this);
	}
}
