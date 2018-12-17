/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Iterator;
import java.util.List;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.IcarusUtils;

/**
 *
 * @author Markus Gärtner
 *
 */
public class ListSequence<E extends Object> implements DataSequence<E>, Iterable<E> {

	protected final List<E> list;

	public ListSequence(List<E> list) {
		if (list == null)
			throw new NullPointerException("Invalid list");
		if (list.isEmpty())
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "List of elements must not be empty");

		this.list = list;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return list.size();
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) {
		return list.get(IcarusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}
}