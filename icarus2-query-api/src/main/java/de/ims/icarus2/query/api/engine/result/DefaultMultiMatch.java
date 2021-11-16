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

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.Arrays;

import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public final class DefaultMultiMatch implements MultiMatch {

	/** Total number of mappings for individual matches. */
	private final int[] sizes;
	/** Position of first mapping in the shared arrays. */
	private final int[] offsets;
	/** Individual container indices for each match. */
	private final long[] indices;
	/** Combined shared mappingId list. */
	private final int[] m_node;
	/** Combined shared target indices list. */
	private final int[] m_index;

	/** Movable pointer for the current match. */
	private int cursor = 0;
	/** Lazily constructed global hash. */
	private int hash = UNSET_INT;

	/**
	 * @param sizes array containing the individual mapping counts for each match
	 * @param offsets array containing the pointers into {@code m_node} and {@code m_index}
	 * 			for each individual match
	 * @param indices the container indices for individual matches
	 * @param m_node combined list of all mapping ids
	 * @param m_index combined list of all mapping targets
	 */
	public DefaultMultiMatch(int[] sizes, int[] offsets, long[] indices, int[] m_node, int[] m_index) {
		checkArgument("size mismatch", sizes.length==offsets.length && offsets.length == indices.length);
		checkArgument("shared buffer size mismatch", m_node.length == m_index.length);

		this.sizes = sizes;
		this.offsets = offsets;
		this.indices = indices;
		this.m_node = m_node;
		this.m_index = m_index;
	}

	@Override
	public long getIndex() { return indices[cursor]; }

	@Override
	public int getMapCount() { return sizes[cursor]; }

	@Override
	public int getNode(int index) {
		checkArgument("mapping index out of range", index >= 0 && index < sizes[cursor]);
		return m_node[offsets[cursor]+index];
	}

	@Override
	public int getIndex(int index) {
		checkArgument("mapping index out of range", index >= 0 && index < sizes[cursor]);
		return m_index[offsets[cursor]+index];
	}

	@Override
	public void drainTo(MatchSink sink) {
		sink.consume(indices[cursor], offsets[cursor], sizes[cursor], m_node, m_index);
	}

	@Override
	public int getCurrentLane() { return cursor; }

	@Override
	public int getLaneCount() { return sizes.length; }

	@Override
	public void moveToLane(int index) {
		checkArgument("index out of range", index >= 0 && index < indices.length);
		cursor = index;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof MultiMatch) {
			if(obj instanceof DefaultMultiMatch && obj.hashCode()!=hashCode()) {
				return false;
			}
			//TODO
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(hash==UNSET_INT) {
			hash = Arrays.hashCode(sizes)
					^ Arrays.hashCode(offsets)
					^ Arrays.hashCode(indices)
					^ Arrays.hashCode(m_node)
					^ Arrays.hashCode(m_index);
		}
		return hash;
	}
}
