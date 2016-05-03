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
 *
 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/id/Identifiable.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.id;

import java.util.Comparator;

/**
 * @author Markus Gärtner
 * @version $Id: Identifiable.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
public interface Identifiable extends Comparable<Identifiable> {

	Identity getIdentity();

	@Override
	default int compareTo(Identifiable other) {
		return COMPARATOR.compare(this, other);
	}

	public static final Comparator<Identifiable> COMPARATOR = (i1, i2) ->
			Identity.COMPARATOR.compare(i1.getIdentity(), i2.getIdentity());
}
