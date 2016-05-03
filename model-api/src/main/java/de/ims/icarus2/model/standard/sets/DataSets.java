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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/sets/DataSets.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $
 * $LastChangedRevision: 457 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.sets;

import java.util.List;

import de.ims.icarus2.model.util.DataSet;

/**
 * @author Markus Gärtner
 * @version $Id: DataSets.java 457 2016-04-20 13:08:11Z mcgaerty $
 *
 */
public class DataSets {

	public static final int ARRAY_SET_THRESHOLD = 7;

	public static <E extends Object> DataSet<E> createDataSet(List<E> items) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		int size = items.size();

		if(size==0) {
			return DataSet.emptySet();
		} else if(size==1) {
			return new SingletonSet<>(items.get(0));
		} else if(size<=ARRAY_SET_THRESHOLD) {
			return new ArraySet<>(items);
		} else {
			return new CachedSet<>(items);
		}
	}
}
