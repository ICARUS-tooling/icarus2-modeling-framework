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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.api.driver.mapping;

import java.util.EnumSet;

import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RequestSettings extends Options {

	private static final long serialVersionUID = -5373096737649271204L;

	private EnumSet<RequestHint> hints;

	public static final RequestSettings emptySettings = new RequestSettings() {

		private static final long serialVersionUID = -929348039219943621L;

		@Override
		public Object put(String key, Object value) {
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public void setHint(RequestHint hint) {
			// no-op
		}

		@Override
		public void removeHint(RequestHint hint) {
			// no-op
		}
	};

	public void setHint(RequestHint hint) {
		if(hints==null) {
			hints = EnumSet.noneOf(RequestHint.class);
		}

		hints.add(hint);
	}

	public void removeHint(RequestHint hint) {
		if(hints!=null) {
			hints.remove(hint);
		}
	}

	public boolean isHintSet(RequestHint hint) {
		return hints!=null && hints.contains(hint);
	}
}
