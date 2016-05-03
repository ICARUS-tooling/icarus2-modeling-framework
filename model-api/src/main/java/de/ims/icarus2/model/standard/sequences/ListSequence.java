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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/sequences/ListSequence.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.sequences;

import java.util.Iterator;
import java.util.List;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.standard.util.CorpusUtils;
import de.ims.icarus2.util.collections.DataSequence;

/**
 *
 * @author Markus Gärtner
 * @version $Id: ListSequence.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class ListSequence<E extends Object> implements DataSequence<E>, Iterable<E> {

	protected final List<E> list;

	public ListSequence(List<E> list) {
		if (list == null)
			throw new NullPointerException("Invalid list");
		if (list.isEmpty())
			throw new ModelException(ModelErrorCode.INVALID_INPUT, "List of elements must not be empty");

		this.list = list;
	}

	/**
	 * @see de.ims.icarus2.util.collections.DataSequence#entryCount()
	 */
	@Override
	public long entryCount() {
		return list.size();
	}

	/**
	 * @see de.ims.icarus2.util.collections.DataSequence#elementAt(long)
	 */
	@Override
	public E elementAt(long index) throws ModelException {
		return list.get(CorpusUtils.ensureIntegerValueRange(index));
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}
}