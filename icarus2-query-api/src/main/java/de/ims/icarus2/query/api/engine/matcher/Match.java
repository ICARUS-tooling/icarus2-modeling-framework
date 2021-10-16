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
package de.ims.icarus2.query.api.engine.matcher;

/**
 * @author Markus Gärtner
 *
 */
public class Match {

	private static final int[] EMPTY = {};

	public static Match empty(long index) {
		return new Match(index, EMPTY, EMPTY);
	}

	public static Match of(long index, int[] m_node, int[] m_index) {
		return new Match(index, m_node, m_index);
	}

	private final long index;
	private final int[] m_node;
	private final int[] m_index;

	Match(long index, int[] m_node, int[] m_index) {
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
}
