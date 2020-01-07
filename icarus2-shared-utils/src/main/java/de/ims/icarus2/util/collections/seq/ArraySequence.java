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
package de.ims.icarus2.util.collections.seq;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class ArraySequence<E extends Object> implements DataSequence<E> {

	private final E[] elements;

	public ArraySequence(@SuppressWarnings("unchecked") E... elements) {
		this.elements = requireNonNull(elements);
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return elements.length;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) {
		return elements[IcarusUtils.ensureIntegerValueRange(index)];
	}

}
