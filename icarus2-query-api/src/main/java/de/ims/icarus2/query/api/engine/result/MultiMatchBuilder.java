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

import de.ims.icarus2.query.api.engine.result.Match.MultiMatch;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * @author Markus Gärtner
 *
 */
public class MultiMatchBuilder implements MatchSink {

	/** Total number of mappings for individual matches. */
	private final IntList sizes = new IntArrayList();
	/** Position of first mapping in the shared arrays. */
	private final IntList offsets = new IntArrayList();
	/** Individual container indices for each match. */
	private final LongList indices = new LongArrayList();
	/** Combined shared mappingId list. */
	private final IntList m_node = new IntArrayList();
	/** Combined shared target indices list. */
	private final IntList m_index = new IntArrayList();

	private int offset = 0;

	@Override
	public void consume(long index, int offset, int size, int[] m_node, int[] m_index) {
		offsets.add(this.offset);
		sizes.add(size);
		indices.add(index);
		this.m_node.addElements(this.offset, m_node, offset, size);
		this.m_index.addElements(this.offset, m_index, offset, size);

		this.offset += size;
	}

	public void reset() {
		sizes.clear();
		offsets.clear();
		indices.clear();
		m_node.clear();
		m_index.clear();
		offset = 0;
	}

	/** Creates and returns a {@link MultiMatch} representing the current aggregation
	 * of {@link Match} data buffered in this builder. {@link #reset() Resets} the
	 * builder afterwards to . */
	public MultiMatch build() {
		MultiMatch match = new DefaultMultiMatch(sizes.toIntArray(), offsets.toIntArray(),
				indices.toLongArray(), m_node.toIntArray(), m_index.toIntArray());
		reset();
		return match;
	}
}
