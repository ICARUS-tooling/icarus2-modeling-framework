/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.AbstractCollection;
import java.util.Iterator;

import de.ims.icarus2.util.IcarusUtils;

/**
 * @author Markus Gärtner
 *
 */
public class DataSequenceCollectionWrapper<E extends Object> extends AbstractCollection<E> {

	private final DataSequence<E> sequence;

	public DataSequenceCollectionWrapper(DataSequence<E> sequence) {
		if (sequence == null)
			throw new NullPointerException("Invalid sequence");

		this.sequence = sequence;
	}

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new SequenceIterator<>(sequence);
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return IcarusUtils.ensureIntegerValueRange(sequence.entryCount());
	}

}
