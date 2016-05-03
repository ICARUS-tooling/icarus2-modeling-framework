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

 * $Revision: 396 $
 * $Date: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/LongToIntIterator.java $
 *
 * $LastChangedDate: 2015-05-20 11:11:11 +0200 (Mi, 20 Mai 2015) $
 * $LastChangedRevision: 396 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import static de.ims.icarus2.model.standard.util.CorpusUtils.ensureIntegerValueRange;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.PrimitiveIterator.OfInt;

/**
 * @author Markus Gärtner
 * @version $Id: LongToIntIterator.java 396 2015-05-20 09:11:11Z mcgaerty $
 *
 */
public class LongToIntIterator implements OfInt {

	private final OfLong source;

	public LongToIntIterator(OfLong source) {
		checkNotNull(source);

		this.source = source;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	/**
	 * @see java.util.PrimitiveIterator.OfInt#nextInt()
	 */
	@Override
	public int nextInt() {
		return ensureIntegerValueRange(source.nextLong());
	}
}
