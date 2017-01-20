/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 *
 */
package de.ims.icarus2.util;

/**
 * Models an environment-aware object that receives notifications about said environment
 * when it is being added or removed. Each such object can have at most one environment
 * associated with it!
 * <p>
 * Note that no method exists in this interface to actually fetch the environment instance.
 * The decision whether or not to expose the associated environment is left to the respective
 * implementation or derived interfaces.
 *
 * @author Markus Gärtner
 *
 */
public interface Part<O extends Object> {

	void addNotify(O owner);

	void removeNotify(O owner);

	boolean isAdded();
}
