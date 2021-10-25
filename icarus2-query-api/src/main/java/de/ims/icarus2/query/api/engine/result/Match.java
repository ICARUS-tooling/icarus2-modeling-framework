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
public class Match implements MatchSource {

	private static final int[] EMPTY = {};

	public static Match empty(long index) {
		return new Match(index, EMPTY, EMPTY);
	}

	public static Match of(long index, int[] m_node, int[] m_index) {
		return new Match(index, m_node, m_index);
	}

	public static Match of(long index, int size, int[] m_node, int[] m_index) {
		return new Match(index, Arrays.copyOf(m_node, size), Arrays.copyOf(m_index, size));
	}

	private final long index;
	private final int[] m_node;
	private final int[] m_index;

	private int hash = UNSET_INT;

	private Match(long index, int[] m_node, int[] m_index) {
		this.index = index;
		this.m_node = m_node;
		this.m_index = m_index;
	}

	/** Get the global index of the container this match refers to. */
	public long getIndex() { return index; }
	/** Returns the number of mappings inside this match */
	public int getMapCount() { return m_node.length; }
	/** Fetch the node id for the mapping at given index */
	public int getNode(int index) { return m_node[index]; }
	/** Fetch the positional index for the mapping at given index */
	public int getIndex(int index) { return m_index[index]; }

	@Override
	public Match toMatch() { return this; }

	@Override
	public void drainTo(MatchSink sink) {
		sink.consume(index, m_node.length, m_node, m_index);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof Match) {
			Match other = (Match) obj;
			return index==other.index
					&& Arrays.equals(m_node, other.m_node)
					&& Arrays.equals(m_index, other.m_index);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(hash==UNSET_INT) {
			hash = (int) index * Arrays.hashCode(m_node) * Arrays.hashCode(m_index);
		}
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(index);
		sb.append(": ");
		for (int i = 0; i < m_node.length; i++) {
			if(i>0) {
				sb.append(", ");
			}
			sb.append(m_node[i]).append("->").append(m_index[i]);
		}
		sb.append('}');
		return sb.toString();
	}
}