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

 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/driver/indices/func/DuplicateFilter.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $LastChangedRevision: 380 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.driver.indices.func;

import java.util.function.LongConsumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 *
 * @author Markus Gärtner
 * @version $Id: DuplicateFilter.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class DuplicateFilter implements LongConsumer {
	private final LongConsumer consumer;
	private long lastReturnedValue = IndexSetMerger.NO_INDEX;

	public DuplicateFilter(LongConsumer consumer) {
		this.consumer = consumer;
	}

	/**
	 * Forwards the given {@code value} in case it is a new
	 * value (meaning greater than the last one returned).
	 *
	 * @see java.util.function.LongConsumer#accept(long)
	 */
	@Override
	public void accept(long value) {
		if(value>lastReturnedValue) {
			lastReturnedValue = value;
			consumer.accept(lastReturnedValue);
		} else if(value<lastReturnedValue)
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Index stream is not sorted");
	}
}