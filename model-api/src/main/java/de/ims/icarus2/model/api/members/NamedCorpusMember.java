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
 * $Revision: 380 $
 *
 */
package de.ims.icarus2.model.api.members;

import de.ims.icarus2.util.strings.NamedObject;

/**
 * 
 * @author Markus Gärtner
 *
 */
public interface NamedCorpusMember extends CorpusMember, NamedObject {

	/**
	 * Returns the unique name of the member.
	 * <p>
	 * Needs only to be unique within the scope of a single corpus!
	 * 
	 * @return Unique name of the member.
	 */
	String getName();
}
