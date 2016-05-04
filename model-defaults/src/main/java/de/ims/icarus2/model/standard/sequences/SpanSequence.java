/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision: 457 $
 * $Date: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/sequences/SpanSequence.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.sequences;

import java.util.ConcurrentModificationException;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.DataSequence;

/**
 *
 * @author Markus Gärtner
 * @version $Id: SpanSequence.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class SpanSequence implements DataSequence<Item> {

	protected final long beginIndex, size;
	protected final Container target;

	public SpanSequence(Container target, long beginIndex, long size) {
		if(size<=0L)
			throw new ModelException(ModelErrorCode.INVALID_INPUT, "Size must not be greater than 0: "+size);

		this.target = target;
		this.beginIndex = beginIndex;
		this.size = size;
	}

	/**
	 * @see de.ims.icarus2.util.collections.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return size;
	}

	/**
	 * @see de.ims.icarus2.util.collections.DataSequence#elementAt(int)
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