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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/members/layers/annotation/fixed/MappedLookup.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 *
 * @author Markus Gärtner
 * @version $Id: MappedLookup.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public class MappedLookup extends IndexLookup {

	public static final int NO_KEY_VALUE = -1;

	protected TObjectIntMap<String> map;

	/**
	 * @param keys
	 */
	public MappedLookup(String[] keys) {
		super(keys);

		map = new TObjectIntHashMap<>(keys.length<<1, 0.5F, NO_KEY_VALUE);

		for(int i=0; i<keyCount(); i++) {
			map.put(keyAt(i), i);
		}
	}

	/**
	 * @see de.ims.icarus2.model.standard.members.layers.annotation.fixed.IndexLookup#indexOf(java.lang.String)
	 */
	@Override
	public int indexOf(String key) {
		return map.get(key);
	}

}