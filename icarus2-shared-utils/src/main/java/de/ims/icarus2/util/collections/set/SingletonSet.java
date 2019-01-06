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
package de.ims.icarus2.util.collections.set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.util.mem.Assessable;
import de.ims.icarus2.util.mem.Reference;
import de.ims.icarus2.util.mem.ReferenceType;

@Assessable
public class SingletonSet<E extends Object> extends AbstractDataSet<E> {

	@Reference(ReferenceType.DOWNLINK)
	private E item;

	public SingletonSet() {
		// no-op
	}

	public SingletonSet(E item) {
		reset(item);
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryCount()
	 */
	@Override
	public int entryCount() {
		if(item==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");

		return 1;
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#entryAt(int)
	 */
	@Override
	public E entryAt(int index) {
		if(item==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");
		if(index!=0)
			throw new IndexOutOfBoundsException();

		return item;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		item = null;
	}

	/**
	 * @see de.ims.icarus2.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return item!=null;
	}

	/**
	 *
	 * @see de.ims.icarus2.util.collections.set.DataSet#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(E member) {
		if(item==null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Missing item");
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
		return item==member;
	}

	public void reset(E member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$
		item = member;
	}

	/**
	 * @see de.ims.icarus2.util.collections.set.AbstractDataSet#add(java.lang.Object)
	 */
	@Override
	public void add(E element) {
		if (element == null)
			throw new NullPointerException("Invalid element"); //$NON-NLS-1$
		if(item!=null)
			throw new IcarusRuntimeException(GlobalErrorCode.ILLEGAL_STATE, "Element already set"); //$NON-NLS-1$
		item = element;
	}
}