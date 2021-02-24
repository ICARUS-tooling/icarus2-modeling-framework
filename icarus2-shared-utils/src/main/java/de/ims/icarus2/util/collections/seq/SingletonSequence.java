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
package de.ims.icarus2.util.collections.seq;

import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public class SingletonSequence<E extends Object> implements DataSequence<E> {

	private final E element;

	public SingletonSequence(E element) {
		this.element = requireNonNull(element);
	}

	public SingletonSequence() {
		element = null;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return element==null ? 0 : 1;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) {
		if(index!=0L || element==null)
			throw new IndexOutOfBoundsException("Invalid index for singleton sequence (only 0 allowed): "+index);

		return element;
	}

}
