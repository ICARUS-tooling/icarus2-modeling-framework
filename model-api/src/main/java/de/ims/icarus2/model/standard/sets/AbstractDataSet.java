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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/sets/AbstractDataSet.java $
 *
 * $LastChangedDate: 2016-04-20 15:08:11 +0200 (Mi, 20 Apr 2016) $ 
 * $LastChangedRevision: 457 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.sets;

import de.ims.icarus2.model.util.DataSet;
import de.ims.icarus2.model.util.Recyclable;
import de.ims.icarus2.util.mem.HeapMember;

@HeapMember
public abstract class AbstractDataSet<E extends Object> implements DataSet<E>, Recyclable {

	public abstract void add(E element);
}