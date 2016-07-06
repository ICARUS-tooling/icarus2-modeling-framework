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
package de.ims.icarus2.model.standard.members.layers.annotation.fixed;

import java.util.Arrays;
import java.util.Set;

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;

/**
 * Implements the actual key storage and provides a sorted view
 * on the available keys. In addition the implementation specific
 * {@link #indexOf(String)} method provides efficient reverse mapping
 * from a key string to its respective index in the sorted keys
 * storage.
 *
 * @author Markus Gärtner
 *
 */
public abstract class IndexLookup {


	public static IndexLookup defaultCreateIndexLookup(AnnotationLayer layer) {
		AnnotationLayerManifest manifest = layer.getManifest();
		Set<String> keySet = manifest.getAvailableKeys();

		String[] keys = new String[keySet.size()];
		keySet.toArray(keys);

		IndexLookup indexLookup = null;

		if(keys.length<=8) {
			indexLookup = new BinarySearchLookup(keys);
		} else {
			indexLookup = new MappedLookup(keys);
		}

		return indexLookup;
	}

	protected final String[] keys;

	protected IndexLookup(String[] keys) {
		if (keys == null)
			throw new NullPointerException("Invalid keys");

		Arrays.sort(keys);

		this.keys = keys;
	}

	public abstract int indexOf(String key);

	public int keyCount() {
		return keys.length;
	}

	public String keyAt(int index) {
		return keys[index];
	}

	public String getAvailableKeysString() {
		String[] keys = new String[keyCount()];
		for(int i=0; i<keys.length; i++) {
			keys[i] = keyAt(i);
		}

		return Arrays.toString(keys);
	}
}