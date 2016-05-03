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

 * $Revision: 449 $
 * $Date: 2016-01-26 16:10:35 +0100 (Di, 26 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/Part.java $
 *
 * $LastChangedDate: 2016-01-26 16:10:35 +0100 (Di, 26 Jan 2016) $
 * $LastChangedRevision: 449 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util;

/**
 * Models an environment-aware object that receives notifications about said environment
 * when it is being added or removed. Each such object can have at most one environment
 * associated with it!
 * <p>
 * Note that no method exists in this interface to actually fetch the environment instance.
 * The decision whether or not to expose the associated environment is left to the respective
 * implementation or derived interface.
 *
 * @author Markus Gärtner
 * @version $Id: Part.java 449 2016-01-26 15:10:35Z mcgaerty $
 *
 */
public interface Part<O extends Object> {

	void addNotify(O owner);

	void removeNotify(O owner);

	boolean isAdded();
}
