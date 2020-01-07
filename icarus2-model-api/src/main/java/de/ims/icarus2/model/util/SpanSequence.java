/**
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
package de.ims.icarus2.model.util;

import java.util.ConcurrentModificationException;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.seq.DataSequence;

/**
 *
 * @author Markus Gärtner
 *
 */
public class SpanSequence implements DataSequence<Item> {

	protected final long beginIndex, size;
	protected final Container target;

	public SpanSequence(Container target, long beginIndex, long size) {
		if(size<=0L)
			throw new IcarusRuntimeException(GlobalErrorCode.INVALID_INPUT, "Size must be greater than 0: "+size);

		this.target = target;
		this.beginIndex = beginIndex;
		this.size = size;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return size;
	}

	/**
	 * @see de.ims.icarus2.util.collections.seq.DataSequence#elementAt(int)
	 */
	@Override
	public Item elementAt(long index) throws ConcurrentModificationException {
		if(index<0L || index>=size)
			throw new IndexOutOfBoundsException(String.valueOf(index));

		return target.getItemAt(beginIndex+index);
	}

	public Container getTarget() {
		return target;
	}

	public long getBeginIndex() {
		return beginIndex;
	}

	public long getEndIndex() {
		return beginIndex+size-1;
	}
}