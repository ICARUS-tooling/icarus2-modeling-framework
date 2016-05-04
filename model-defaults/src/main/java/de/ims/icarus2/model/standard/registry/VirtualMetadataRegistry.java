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
package de.ims.icarus2.model.standard.registry;

import java.util.TreeMap;
import java.util.function.BiConsumer;

import de.ims.icarus2.model.api.registry.MetadataRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class VirtualMetadataRegistry implements MetadataRegistry {

	private final TreeMap<String, String> entries = new TreeMap<>();


	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#getValue(java.lang.String)
	 */
	@Override
	public synchronized String getValue(String key) {
		return entries.get(key);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#setValue(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void setValue(String key, String value) {
		if(value==null) {
			entries.remove(key);
		} else {
			entries.put(key, value);
		}
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#beginUpdate()
	 */
	@Override
	public void beginUpdate() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#endUpdate()
	 */
	@Override
	public void endUpdate() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#delete()
	 */
	@Override
	public synchronized void delete() {
		entries.clear();
	}

	@Override
	public synchronized void forEachEntry(BiConsumer<? super String, ? super String> action) {
		entries.forEach(action);
	}

	/**
	 *
	 *
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#forEachEntry(java.lang.String, java.util.function.BiConsumer)
	 */
	@Override
	public synchronized void forEachEntry(String prefix,
			BiConsumer<? super String, ? super String> action) {

		String lowerBound = prefix+Character.MIN_VALUE;
		String upperBound = prefix+Character.MAX_VALUE;

		entries.subMap(lowerBound, true, upperBound, true).forEach(action);
	}

	/**
	 * @see de.ims.icarus2.model.api.registry.MetadataRegistry#close()
	 */
	@Override
	public void close() {
		// no-op
	}
}
